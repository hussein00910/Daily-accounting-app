package com.whatshub.data.repository

import com.whatshub.core.SupabaseClientProvider
import io.github.jan.supabase.auth.OtpType
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.OTP

class AuthRepository {

    private val auth get() = SupabaseClientProvider.client.auth

    suspend fun sendOtp(phoneNumber: String) {
        auth.signInWith(OTP) {
            phone = phoneNumber
        }
    }

    suspend fun verifyOtp(phoneNumber: String, code: String) {
        auth.verifyPhoneOtp(
            type = OtpType.Phone.SMS,
            phone = phoneNumber,
            token = code
        )
    }

    suspend fun signOut() {
        auth.signOut()
    }

    fun currentUserId(): String? = auth.currentUserOrNull()?.id

    fun hasValidSession(): Boolean = auth.currentSessionOrNull() != null
}
