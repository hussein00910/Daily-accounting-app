package com.whatshub.ui.newchat

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.whatshub.data.model.Profile
import com.whatshub.data.repository.ChatRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class NewChatViewModel : ViewModel() {

    private val chatRepository = ChatRepository()

    private var searchJob: Job? = null

    private val _results = MutableLiveData<List<Profile>>(emptyList())
    val results: LiveData<List<Profile>> = _results

    private val _navigateToChat = MutableLiveData<Result<Pair<String, Profile>>>()
    val navigateToChat: LiveData<Result<Pair<String, Profile>>> = _navigateToChat

    fun setQuery(query: String) {
        searchJob?.cancel()
        if (query.isBlank()) {
            _results.value = emptyList()
            return
        }
        searchJob = viewModelScope.launch {
            delay(300)
            runCatching { chatRepository.searchProfiles(query) }
                .onSuccess { _results.value = it }
        }
    }

    fun selectProfile(profile: Profile) {
        viewModelScope.launch {
            val result = runCatching { chatRepository.getOrCreateDirectChat(profile.id) }
            _navigateToChat.value = result.map { chatId -> chatId to profile }
        }
    }
}
