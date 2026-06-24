package com.whatshub.data.model

enum class SendState { SENDING, SENT, FAILED }

data class MessageUiModel(
    val message: Message,
    val tickState: TickState,
    val sendState: SendState = SendState.SENT,
    val isOwnMessage: Boolean
)
