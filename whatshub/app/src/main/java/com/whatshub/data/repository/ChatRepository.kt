package com.whatshub.data.repository

import com.whatshub.core.SupabaseClientProvider
import com.whatshub.data.model.Chat
import com.whatshub.data.model.ChatListItem
import com.whatshub.data.model.ChatMember
import com.whatshub.data.model.Message
import com.whatshub.data.model.MessageReceipt
import com.whatshub.data.model.Profile
import com.whatshub.data.model.TickState
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Order
import io.github.jan.supabase.realtime.RealtimeChannel
import io.github.jan.supabase.realtime.channel
import io.github.jan.supabase.realtime.realtime
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

class ChatRepository {
    private val client get() = SupabaseClientProvider.client

    suspend fun getChatListItems(): List<ChatListItem> {
        val myId = client.auth.currentUserOrNull()?.id ?: return emptyList()

        // Fetch all chat_members rows for the current user, then resolve chats separately
        // (simple two-query fallback approach instead of embedded select, see report).
        val myMemberships = client.postgrest.from("chat_members")
            .select { filter { eq("user_id", myId) } }
            .decodeList<ChatMember>()

        if (myMemberships.isEmpty()) return emptyList()

        val chatIds = myMemberships.map { it.chatId }
        val chats = client.postgrest.from("chats")
            .select { filter { isIn("id", chatIds) } }
            .decodeList<Chat>()
        val chatById = chats.associateBy { it.id }
        val membershipByChatId = myMemberships.associateBy { it.chatId }

        // Last message per chat (N+1 query pattern, acceptable for v1).
        val lastMessageByChatId = mutableMapOf<String, Message>()
        for (chatId in chatIds) {
            val lastMessage = client.postgrest.from("messages")
                .select {
                    filter { eq("chat_id", chatId) }
                    order("created_at", Order.DESCENDING)
                    limit(1)
                }
                .decodeList<Message>()
                .firstOrNull()
            if (lastMessage != null) {
                lastMessageByChatId[chatId] = lastMessage
            }
        }

        // For direct chats, find the other member's user id.
        val directChatIds = chats.filter { it.type == "direct" }.map { it.id }
        val otherUserIdByChatId = mutableMapOf<String, String>()
        if (directChatIds.isNotEmpty()) {
            val allMembersOfDirectChats = client.postgrest.from("chat_members")
                .select { filter { isIn("chat_id", directChatIds) } }
                .decodeList<ChatMember>()
            for (chatId in directChatIds) {
                val other = allMembersOfDirectChats.firstOrNull { it.chatId == chatId && it.userId != myId }
                if (other != null) {
                    otherUserIdByChatId[chatId] = other.userId
                }
            }
        }

        // Batch-resolve profiles for all "other" users across direct chats.
        val otherUserIds = otherUserIdByChatId.values.distinct()
        val profilesById = if (otherUserIds.isNotEmpty()) {
            client.postgrest.from("profiles")
                .select { filter { isIn("id", otherUserIds) } }
                .decodeList<Profile>()
                .associateBy { it.id }
        } else {
            emptyMap()
        }

        // Receipts for my own last messages, to compute tick state.
        val myLastMessageIds = lastMessageByChatId.values
            .filter { it.senderId == myId }
            .map { it.id }
        val receiptsByMessageId = if (myLastMessageIds.isNotEmpty()) {
            client.postgrest.from("message_receipts")
                .select { filter { isIn("message_id", myLastMessageIds); neq("user_id", myId) } }
                .decodeList<MessageReceipt>()
                .groupBy { it.messageId }
        } else {
            emptyMap()
        }

        val items = chatIds.mapNotNull { chatId ->
            val chat = chatById[chatId] ?: return@mapNotNull null
            val membership = membershipByChatId[chatId]
            val lastMessage = lastMessageByChatId[chatId]
            val isGroup = chat.type != "direct"
            val otherUserId = otherUserIdByChatId[chatId]
            val otherProfile = otherUserId?.let { profilesById[it] }

            val displayName = if (isGroup) {
                chat.name ?: "Group"
            } else {
                otherProfile?.displayName ?: otherProfile?.username ?: otherProfile?.phone ?: "Unknown"
            }
            val avatarUrl = if (isGroup) chat.avatarUrl else otherProfile?.avatarUrl

            // Unread heuristic: lastMessageId differs from last_read_message_id => unread.
            val unreadCount = if (chat.lastMessageId != null &&
                chat.lastMessageId != membership?.lastReadMessageId &&
                lastMessage?.senderId != myId
            ) {
                1
            } else {
                0
            }

            val tickState = if (lastMessage != null && lastMessage.senderId == myId) {
                if (isGroup) {
                    TickState.SENT
                } else {
                    val receipts = receiptsByMessageId[lastMessage.id].orEmpty()
                    val otherReceipt = receipts.firstOrNull()
                    when {
                        otherReceipt?.readAt != null -> TickState.READ
                        otherReceipt?.deliveredAt != null -> TickState.DELIVERED
                        else -> TickState.SENT
                    }
                }
            } else {
                TickState.NONE
            }

            ChatListItem(
                chatId = chatId,
                otherUserId = otherUserId,
                displayName = displayName,
                avatarUrl = avatarUrl,
                lastMessageBody = lastMessage?.body,
                lastMessageAt = lastMessage?.createdAt ?: chat.lastMessageAt,
                lastMessageSenderId = lastMessage?.senderId,
                unreadCount = unreadCount,
                tickState = tickState,
                isPinned = membership?.isPinned ?: false,
                isFavorite = membership?.isFavorite ?: false,
                isGroup = isGroup
            )
        }

        return items.sortedWith(compareByDescending(nullsLast()) { it.lastMessageAt })
    }

    suspend fun getOrCreateDirectChat(otherUserId: String): String {
        val result = client.postgrest.rpc(
            "get_or_create_direct_chat",
            buildJsonObject { put("other_user_id", otherUserId) }
        )
        return result.decodeAs<String>().trim('"')
    }

    suspend fun searchProfiles(query: String): List<Profile> {
        val myId = client.auth.currentUserOrNull()?.id
        val byUsername = client.postgrest.from("profiles")
            .select { filter { ilike("username", "%$query%") } }
            .decodeList<Profile>()
        val byPhone = client.postgrest.from("profiles")
            .select { filter { ilike("phone", "%$query%") } }
            .decodeList<Profile>()
        return (byUsername + byPhone)
            .distinctBy { it.id }
            .filter { it.id != myId }
            .take(20)
    }

    fun chatListChannel(): RealtimeChannel = client.realtime.channel("chat-list-updates")

    suspend fun subscribeChannel(channel: RealtimeChannel) {
        channel.subscribe(blockUntilSubscribed = true)
    }

    suspend fun unsubscribeChannel(channel: RealtimeChannel) {
        client.realtime.removeChannel(channel)
    }
}
