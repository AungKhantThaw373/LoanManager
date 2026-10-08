package com.example.kotlinmulti.loanmanager.ui.auth

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.example.kotlinmulti.loanmanager.data.auth.SecureTokenStore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class AuthSessionViewModel(application: Application) : AndroidViewModel(application) {
    private val tokenStore = SecureTokenStore(application)
    private val _token = MutableStateFlow(tokenStore.read())
    val token = _token.asStateFlow()

    fun setSignedIn(token: String) {
        _token.value = token
    }

    fun signOut() {
        tokenStore.clear()
        _token.value = null
    }
}
