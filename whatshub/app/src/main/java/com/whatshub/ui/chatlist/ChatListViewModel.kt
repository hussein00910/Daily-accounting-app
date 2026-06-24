package com.whatshub.ui.chatlist

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.whatshub.data.model.ChatListItem
import com.whatshub.data.repository.ChatRepository
import io.github.jan.supabase.realtime.PostgresAction
import io.github.jan.supabase.realtime.RealtimeChannel
import io.github.jan.supabase.realtime.postgresChangeFlow
import kotlinx.coroutines.launch

enum class ChatFilter { ALL, UNREAD, FAVORITES, GROUPS }

class ChatListViewModel : ViewModel() {

    private val chatRepository = ChatRepository()

    private var fullChats: List<ChatListItem> = emptyList()
    private var currentFilter = ChatFilter.ALL
    private var currentQuery = ""

    private val _visibleChats = MutableLiveData<List<ChatListItem>>()
    val visibleChats: LiveData<List<ChatListItem>> = _visibleChats

    private var realtimeChannel: RealtimeChannel? = null

    fun loadChats() {
        viewModelScope.launch {
            runCatching { chatRepository.getChatListItems() }
                .onSuccess {
                    fullChats = it
                    recompute()
                }
            // Silently no-op on failure for v1 - no error LiveData surfaced to the UI.
        }
    }

    fun setFilter(filter: ChatFilter) {
        currentFilter = filter
        recompute()
    }

    fun setSearchQuery(query: String) {
        currentQuery = query
        recompute()
    }

    private fun recompute() {
        var result = fullChats
        result = when (currentFilter) {
            ChatFilter.ALL -> result
            ChatFilter.UNREAD -> result.filter { it.unreadCount > 0 }
            ChatFilter.FAVORITES -> result.filter { it.isFavorite }
            ChatFilter.GROUPS -> result.filter { it.isGroup }
        }
        if (currentQuery.isNotBlank()) {
            result = result.filter { it.displayName.contains(currentQuery, ignoreCase = true) }
        }
        _visibleChats.value = result
    }

    fun startRealtimeUpdates() {
        if (realtimeChannel != null) return
        val channel = chatRepository.chatListChannel()
        realtimeChannel = channel

        val messagesFlow = channel.postgresChangeFlow<PostgresAction>(schema = "public") {
            table = "messages"
        }
        val membersFlow = channel.postgresChangeFlow<PostgresAction>(schema = "public") {
            table = "chat_members"
        }

        viewModelScope.launch {
            chatRepository.subscribeChannel(channel)
        }
        viewModelScope.launch {
            messagesFlow.collect { loadChats() }
        }
        viewModelScope.launch {
            membersFlow.collect { loadChats() }
        }
    }

    fun stopRealtimeUpdates() {
        val channel = realtimeChannel ?: return
        viewModelScope.launch {
            runCatching { chatRepository.unsubscribeChannel(channel) }
        }
        realtimeChannel = null
    }
}
