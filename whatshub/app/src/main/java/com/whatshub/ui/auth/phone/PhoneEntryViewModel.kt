package com.whatshub.ui.auth.phone

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.whatshub.data.repository.AuthRepository
import kotlinx.coroutines.launch

class PhoneEntryViewModel : ViewModel() {

    private val authRepository = AuthRepository()

    private val _loading = MutableLiveData(false)
    val loading: LiveData<Boolean> = _loading

    private val _otpSent = MutableLiveData<Result<String>>()
    val otpSent: LiveData<Result<String>> = _otpSent

    fun requestOtp(fullPhoneNumber: String) {
        _loading.value = true
        viewModelScope.launch {
            val result = runCatching {
                authRepository.sendOtp(fullPhoneNumber)
                fullPhoneNumber
            }
            _loading.value = false
            _otpSent.value = result
        }
    }
}
