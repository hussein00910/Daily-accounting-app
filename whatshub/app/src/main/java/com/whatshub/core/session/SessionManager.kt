package com.whatshub.core.session

import com.whatshub.core.SupabaseClientProvider
import io.github.jan.supabase.auth.status.SessionStatus
import io.github.jan.supabase.auth.auth
import kotlinx.coroutines.flow.StateFlow

object SessionManager {
    val sessionStatus: StateFlow<SessionStatus>
        get() = SupabaseClientProvider.client.auth.sessionStatus
}
