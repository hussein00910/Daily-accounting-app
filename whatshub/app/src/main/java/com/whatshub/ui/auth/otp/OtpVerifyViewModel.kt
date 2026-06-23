package com.whatshub.ui.auth.otp

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.whatshub.data.repository.AuthRepository
import com.whatshub.data.repository.ProfileRepository
import kotlinx.coroutines.launch

class OtpVerifyViewModel : ViewModel() {

    private val authRepository = AuthRepository()
    private val profileRepository = ProfileRepository()

    private val _loading = MutableLiveData(false)
    val loading: LiveData<Boolean> = _loading

    private val _result = MutableLiveData<Result<Boolean>>()
    val result: LiveData<Result<Boolean>> = _result

    private var resendJob: kotlinx.coroutines.Job? = null

    private val _resendSecondsLeft = MutableLiveData(0)
    val resendSecondsLeft: LiveData<Int> = _resendSecondsLeft

    fun verify(phoneNumber: String, code: String) {
        _loading.value = true
        viewModelScope.launch {
            val outcome = runCatching {
                authRepository.verifyOtp(phoneNumber, code)
                val profile = profileRepository.getCurrentProfile()
                profile?.displayName?.isNotBlank() == true
            }
            _loading.value = false
            _result.value = outcome
        }
    }

    fun resend(phoneNumber: String) {
        viewModelScope.launch {
            runCatching { authRepository.sendOtp(phoneNumber) }
        }
        startResendCooldown()
    }

    fun startResendCooldown(seconds: Int = 30) {
        resendJob?.cancel()
        resendJob = viewModelScope.launch {
            for (s in seconds downTo 0) {
                _resendSecondsLeft.value = s
                kotlinx.coroutines.delay(1000)
            }
        }
    }
}
