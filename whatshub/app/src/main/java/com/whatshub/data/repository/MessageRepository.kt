package com.whatshub.data.repository

import com.whatshub.core.SupabaseClientProvider
import com.whatshub.data.model.Message
import com.whatshub.data.model.MessageReceipt
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Order
import io.github.jan.supabase.realtime.PostgresAction
import io.github.jan.supabase.realtime.RealtimeChannel
import io.github.jan.supabase.realtime.channel
import io.github.jan.supabase.realtime.postgresChangeFlow
import io.github.jan.supabase.realtime.realtime
import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.time.Instant

class MessageRepository {
    private val client get() = SupabaseClientProvider.client

    suspend fun getMessages(chatId: String, limit: Int = 50): List<Message> {
        return client.postgrest.from("messages")
            .select {
                filter { eq("chat_id", chatId) }
                order("created_at", Order.DESCENDING)
                limit(limit.toLong())
            }
            .decodeList<Message>()
            .reversed()
    }

    suspend fun getReceipts(messageIds: List<String>): List<MessageReceipt> {
        if (messageIds.isEmpty()) return emptyList()
        return client.postgrest.from("message_receipts")
            .select { filter { isIn("message_id", messageIds) } }
            .decodeList<MessageReceipt>()
    }

    suspend fun sendMessage(chatId: String, body: String): Message {
        val myId = client.auth.currentUserOrNull()?.id ?: error("No authenticated user")
        val payload = NewMessagePayload(
            chatId = chatId,
            senderId = myId,
            type = "text",
            body = body
        )
        return client.postgrest.from("messages")
            .insert(payload) { select() }
            .decodeSingle<Message>()
    }

    suspend fun markDelivered(messageId: String) {
        val myId = client.auth.currentUserOrNull()?.id ?: return
        client.postgrest.from("message_receipts")
            .update({ set("delivered_at", Instant.now().toString()) }) {
                filter {
                    eq("message_id", messageId)
                    eq("user_id", myId)
                }
            }
    }

    suspend fun markMessagesRead(chatId: String, upToMessageId: String) {
        val myId = client.auth.currentUserOrNull()?.id ?: return

        client.postgrest.from("chat_members")
            .update({ set("last_read_message_id", upToMessageId) }) {
                filter {
                    eq("chat_id", chatId)
                    eq("user_id", myId)
                }
            }

        val messageIds = client.postgrest.from("messages")
            .select { filter { eq("chat_id", chatId) } }
            .decodeList<Message>()
            .map { it.id }

        if (messageIds.isEmpty()) return

        client.postgrest.from("message_receipts")
            .update({ set("read_at", Instant.now().toString()) }) {
                filter {
                    eq("user_id", myId)
                    isIn("message_id", messageIds)
                }
            }
    }

    fun messageChannel(chatId: String): RealtimeChannel = client.realtime.channel("chat:$chatId")

    fun observeMessages(channel: RealtimeChannel): Flow<PostgresAction.Insert> {
        return channel.postgresChangeFlow(schema = "public") {
            table = "messages"
        }
    }

    suspend fun subscribeChannel(channel: RealtimeChannel) {
        channel.subscribe(blockUntilSubscribed = true)
    }

    suspend fun unsubscribeChannel(channel: RealtimeChannel) {
        client.realtime.removeChannel(channel)
    }

    fun typingChannel(chatId: String): RealtimeChannel = client.realtime.channel("presence:chat:$chatId")

    @Serializable
    private data class NewMessagePayload(
        @SerialName("chat_id") val chatId: String,
        @SerialName("sender_id") val senderId: String,
        val type: String,
        val body: String
    )
}
