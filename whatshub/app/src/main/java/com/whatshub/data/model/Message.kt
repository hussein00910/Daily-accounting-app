package com.whatshub.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Message(
    val id: String,
    @SerialName("chat_id") val chatId: String,
    @SerialName("sender_id") val senderId: String,
    val type: String,
    val body: String? = null,
    @SerialName("media_path") val mediaPath: String? = null,
    @SerialName("media_mime_type") val mediaMimeType: String? = null,
    @SerialName("media_duration_ms") val mediaDurationMs: Long? = null,
    @SerialName("reply_to_message_id") val replyToMessageId: String? = null,
    @SerialName("created_at") val createdAt: String,
    @SerialName("deleted_at") val deletedAt: String? = null
)
