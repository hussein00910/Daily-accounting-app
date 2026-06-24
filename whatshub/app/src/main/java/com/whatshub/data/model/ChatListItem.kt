package com.whatshub.data.model

enum class TickState { NONE, SENT, DELIVERED, READ }

data class ChatListItem(
    val chatId: String,
    val otherUserId: String?,
    val displayName: String,
    val avatarUrl: String?,
    val lastMessageBody: String?,
    val lastMessageAt: String?,
    val lastMessageSenderId: String?,
    val unreadCount: Int,
    val tickState: TickState,
    val isPinned: Boolean,
    val isFavorite: Boolean,
    val isGroup: Boolean
)
