package com.example.kotlinmulti.loanmanager.ui.auth

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import android.util.Base64
import org.json.JSONObject
import com.example.kotlinmulti.loanmanager.data.auth.SecureTokenStore
import com.example.kotlinmulti.loanmanager.ui.demo.OfflineDemoMode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class AuthSessionViewModel(application: Application) : AndroidViewModel(application) {
    private val tokenStore = SecureTokenStore(application)
    private val _token = MutableStateFlow(tokenStore.read())
    val token = _token.asStateFlow()
    private val _role = MutableStateFlow(resolveRole(_token.value))
    val role = _role.asStateFlow()

    fun setSignedIn(token: String) {
        _token.value = token
        _role.value = resolveRole(token)
    }

    fun setRole(role: String) {
        _role.value = role.uppercase()
    }

    fun signOut() {
        tokenStore.clear()
        _token.value = null
        _role.value = null
    }

    private fun resolveRole(token: String?): String? =
        OfflineDemoMode.roleForToken(token) ?: runCatching {
            val payload = token?.split('.')?.getOrNull(1) ?: return@runCatching null
            val json = String(Base64.decode(payload, Base64.URL_SAFE or Base64.NO_WRAP or Base64.NO_PADDING), Charsets.UTF_8)
            JSONObject(json).optString("role").takeIf(String::isNotBlank)?.uppercase()
        }.getOrNull()
}
