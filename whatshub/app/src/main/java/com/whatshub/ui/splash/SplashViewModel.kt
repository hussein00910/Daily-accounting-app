package com.whatshub.ui.splash

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.whatshub.data.repository.AuthRepository
import com.whatshub.data.repository.ProfileRepository
import kotlinx.coroutines.launch

sealed class SplashDestination {
    object PhoneEntry : SplashDestination()
    object ProfileSetup : SplashDestination()
    object ChatList : SplashDestination()
}

class SplashViewModel : ViewModel() {

    private val authRepository = AuthRepository()
    private val profileRepository = ProfileRepository()

    private val _destination = MutableLiveData<SplashDestination>()
    val destination: LiveData<SplashDestination> = _destination

    fun resolveDestination() {
        viewModelScope.launch {
            if (!authRepository.hasValidSession()) {
                _destination.value = SplashDestination.PhoneEntry
                return@launch
            }
            val profile = runCatching { profileRepository.getCurrentProfile() }.getOrNull()
            _destination.value = if (profile?.displayName.isNullOrBlank()) {
                SplashDestination.ProfileSetup
            } else {
                SplashDestination.ChatList
            }
        }
    }
}
