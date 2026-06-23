package com.whatshub.data.repository

import com.whatshub.core.SupabaseClientProvider
import com.whatshub.data.model.Profile
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.storage.storage
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

class ProfileRepository {

    private val client get() = SupabaseClientProvider.client

    suspend fun getCurrentProfile(): Profile? {
        val userId = client.auth.currentUserOrNull()?.id ?: return null
        return client.postgrest.from("profiles")
            .select {
                filter { eq("id", userId) }
            }
            .decodeSingleOrNull<Profile>()
    }

    suspend fun updateProfile(displayName: String, avatarPath: String?) {
        val userId = client.auth.currentUserOrNull()?.id ?: return
        client.postgrest.from("profiles")
            .update(ProfileUpdate(displayName, avatarPath)) {
                filter { eq("id", userId) }
            }
    }

    suspend fun uploadAvatar(bytes: ByteArray): String {
        val userId = client.auth.currentUserOrNull()?.id ?: error("No authenticated user")
        val path = "$userId.jpg"
        client.storage.from("avatars").upload(path, bytes) {
            upsert = true
        }
        return path
    }

    @Serializable
    private data class ProfileUpdate(
        @SerialName("display_name") val displayName: String,
        @SerialName("avatar_url") val avatarUrl: String?
    )
}
