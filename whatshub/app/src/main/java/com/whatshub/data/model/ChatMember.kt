package com.whatshub.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ChatMember(
    @SerialName("chat_id") val chatId: String,
    @SerialName("user_id") val userId: String,
    val role: String,
    @SerialName("is_pinned") val isPinned: Boolean = false,
    @SerialName("is_favorite") val isFavorite: Boolean = false,
    @SerialName("is_archived") val isArchived: Boolean = false,
    @SerialName("last_read_message_id") val lastReadMessageId: String? = null,
    @SerialName("joined_at") val joinedAt: String? = null
)
