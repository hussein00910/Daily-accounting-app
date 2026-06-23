package com.whatshub.ui.auth.profile_setup

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.whatshub.data.repository.ProfileRepository
import kotlinx.coroutines.launch

class ProfileSetupViewModel : ViewModel() {

    private val profileRepository = ProfileRepository()

    private val _loading = MutableLiveData(false)
    val loading: LiveData<Boolean> = _loading

    private val _saved = MutableLiveData<Result<Unit>>()
    val saved: LiveData<Result<Unit>> = _saved

    fun saveProfile(displayName: String, avatarBytes: ByteArray?) {
        _loading.value = true
        viewModelScope.launch {
            val outcome = runCatching {
                val avatarPath = avatarBytes?.let { profileRepository.uploadAvatar(it) }
                profileRepository.updateProfile(displayName, avatarPath)
            }
            _loading.value = false
            _saved.value = outcome
        }
    }
}
