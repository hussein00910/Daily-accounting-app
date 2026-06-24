package com.whatshub.ui.chat

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.whatshub.core.SupabaseClientProvider
import com.whatshub.data.model.MessageUiModel
import com.whatshub.data.model.SendState
import com.whatshub.data.model.TickState
import com.whatshub.data.repository.MessageRepository
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.realtime.PresenceAction
import io.github.jan.supabase.realtime.RealtimeChannel
import io.github.jan.supabase.realtime.decodeRecord
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.serialization.json.JsonObject
import java.util.UUID

class ChatViewModel : ViewModel() {

    private val messageRepository = MessageRepository()
    private val myId: String? get() = SupabaseClientProvider.client.auth.currentUserOrNull()?.id

    private var chatId: String? = null

    private val _messages = MutableLiveData<List<MessageUiModel>>(emptyList())
    val messages: LiveData<List<MessageUiModel>> = _messages

    private val _otherTyping = MutableLiveData(false)
    val otherTyping: LiveData<Boolean> = _otherTyping

    private val _sendFailed = MutableLiveData<String?>()
    val sendFailed: LiveData<String?> = _sendFailed

    private var observing = false
    private var messagesChannel: RealtimeChannel? = null
    private var presenceChannel: RealtimeChannel? = null
    private var typingJob: Job? = null

    fun loadInitial(newChatId: String) {
        if (chatId == newChatId) return
        chatId = newChatId
        viewModelScope.launch {
            runCatching {
                val msgs = messageRepository.getMessages(newChatId)
                val myMessageIds = msgs.filter { it.senderId == myId }.map { it.id }
                val receipts = messageRepository.getReceipts(myMessageIds)
                val receiptsByMessageId = receipts
                    .filter { it.userId != myId }
                    .groupBy { it.messageId }

                msgs.map { message ->
                    val isOwn = message.senderId == myId
                    val tickState = if (isOwn) {
                        val receipt = receiptsByMessageId[message.id]?.firstOrNull()
                        when {
                            receipt?.readAt != null -> TickState.READ
                            receipt?.deliveredAt != null -> TickState.DELIVERED
                            else -> TickState.SENT
                        }
                    } else {
                        TickState.NONE
                    }
                    MessageUiModel(message = message, tickState = tickState, isOwnMessage = isOwn)
                }
            }.onSuccess { uiModels ->
                _messages.value = uiModels
                uiModels.lastOrNull()?.let { markRead(newChatId, it.message.id) }
            }
        }
    }

    fun sendMessage(body: String) {
        val currentChatId = chatId ?: return
        if (body.isBlank()) return
        val tempId = "temp-${UUID.randomUUID()}"
        val tempMessage = com.whatshub.data.model.Message(
            id = tempId,
            chatId = currentChatId,
            senderId = myId.orEmpty(),
            type = "text",
            body = body,
            createdAt = java.time.Instant.now().toString()
        )
        val tempUiModel = MessageUiModel(
            message = tempMessage,
            tickState = TickState.NONE,
            sendState = SendState.SENDING,
            isOwnMessage = true
        )
        _messages.value = (_messages.value.orEmpty()) + tempUiModel

        viewModelScope.launch {
            runCatching { messageRepository.sendMessage(currentChatId, body) }
                .onSuccess { serverMessage ->
                    val confirmed = MessageUiModel(
                        message = serverMessage,
                        tickState = TickState.SENT,
                        sendState = SendState.SENT,
                        isOwnMessage = true
                    )
                    _messages.value = _messages.value.orEmpty().map {
                        if (it.message.id == tempId) confirmed else it
                    }
                }
                .onFailure {
                    _messages.value = _messages.value.orEmpty().map {
                        if (it.message.id == tempId) it.copy(sendState = SendState.FAILED) else it
                    }
                    _sendFailed.value = body
                }
        }
    }

    fun consumeSendFailedEvent() {
        _sendFailed.value = null
    }

    private fun markRead(currentChatId: String, upToMessageId: String) {
        viewModelScope.launch {
            runCatching { messageRepository.markMessagesRead(currentChatId, upToMessageId) }
        }
    }

    fun startObserving() {
        if (observing) return
        val currentChatId = chatId ?: return
        observing = true

        val msgChannel = messageRepository.messageChannel(currentChatId)
        messagesChannel = msgChannel
        val insertFlow = messageRepository.observeMessages(msgChannel)

        viewModelScope.launch {
            messageRepository.subscribeChannel(msgChannel)
        }
        viewModelScope.launch {
            insertFlow.collect { action ->
                val message = action.decodeRecord<com.whatshub.data.model.Message>()
                if (message.chatId != currentChatId) return@collect
                if (message.senderId == myId) return@collect

                val uiModel = MessageUiModel(
                    message = message,
                    tickState = TickState.NONE,
                    isOwnMessage = false
                )
                _messages.value = (_messages.value.orEmpty()) + uiModel
                runCatching { messageRepository.markDelivered(message.id) }
                markRead(currentChatId, message.id)
            }
        }

        val pChannel = messageRepository.typingChannel(currentChatId)
        presenceChannel = pChannel
        viewModelScope.launch {
            messageRepository.subscribeChannel(pChannel)
        }
        viewModelScope.launch {
            pChannel.presenceChangeFlow().collect { action: PresenceAction ->
                // Each track() call replaces the full presence state for that ref, so the
                // most recent join from the other participant reflects their current typing state.
                // A leave (untrack/disconnect) means they are no longer signaling typing.
                val otherJoinedTyping = action.joins.values.any { presence ->
                    val state = presence.state
                    val userId = (state["userId"] as? kotlinx.serialization.json.JsonPrimitive)?.content
                    val isTyping = (state["isTyping"] as? kotlinx.serialization.json.JsonPrimitive)?.content
                    userId != myId && isTyping == "true"
                }
                val otherLeft = action.leaves.values.any { presence ->
                    val userId = (presence.state["userId"] as? kotlinx.serialization.json.JsonPrimitive)?.content
                    userId != myId
                }
                when {
                    otherJoinedTyping -> _otherTyping.value = true
                    otherLeft -> _otherTyping.value = false
                }
            }
        }
    }

    fun setTyping(isTyping: Boolean) {
        val channel = presenceChannel ?: return
        viewModelScope.launch {
            runCatching {
                val payload = JsonObject(
                    mapOf(
                        "userId" to kotlinx.serialization.json.JsonPrimitive(myId.orEmpty()),
                        "isTyping" to kotlinx.serialization.json.JsonPrimitive(isTyping.toString())
                    )
                )
                channel.track(payload)
            }
        }
    }

    fun stopObserving() {
        val msgChannel = messagesChannel
        val pChannel = presenceChannel
        viewModelScope.launch {
            msgChannel?.let { runCatching { messageRepository.unsubscribeChannel(it) } }
            pChannel?.let { runCatching { messageRepository.unsubscribeChannel(it) } }
        }
        messagesChannel = null
        presenceChannel = null
        observing = false
    }
}
