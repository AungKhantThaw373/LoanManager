package com.example.kotlinmulti.loanmanager.ui.profile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.kotlinmulti.loanmanager.data.remote.CurrentUserDto
import com.example.kotlinmulti.loanmanager.data.remote.LoanApiService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun ProfileScreen(
    authToken: String,
    onLogout: () -> Unit
) {
    var user by remember { mutableStateOf<CurrentUserDto?>(null) }
    var error by remember { mutableStateOf<String?>(null) }
    var loading by remember { mutableStateOf(true) }
    var loggingOut by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(authToken) {
        loading = true
        error = null
        runCatching {
            withContext(Dispatchers.IO) {
                LoanApiService.instance.getCurrentUser("Bearer $authToken")
            }
        }.onSuccess { response ->
            user = response.user
            if (user == null) error = response.message ?: "Profile information was not returned."
        }.onFailure { exception ->
            error = exception.localizedMessage ?: "Could not load your profile."
        }
        loading = false
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Profile", style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(24.dp))
        when {
            loading -> CircularProgressIndicator()
            user != null -> {
                Text(user!!.name, style = MaterialTheme.typography.titleLarge)
                Spacer(Modifier.height(8.dp))
                Text(user!!.email, style = MaterialTheme.typography.bodyLarge)
                Spacer(Modifier.height(4.dp))
                Text(user!!.role, style = MaterialTheme.typography.bodyMedium)
            }
            else -> Text(error.orEmpty(), color = MaterialTheme.colorScheme.error)
        }
        Spacer(Modifier.height(24.dp))
        if (!loading && user == null) {
            OutlinedButton(onClick = {
                scope.launch {
                    loading = true
                    error = null
                    runCatching {
                        withContext(Dispatchers.IO) {
                            LoanApiService.instance.getCurrentUser("Bearer $authToken")
                        }
                    }.onSuccess { response ->
                        user = response.user
                        if (user == null) error = response.message ?: "Profile information was not returned."
                    }.onFailure { exception -> error = exception.localizedMessage ?: "Could not load your profile." }
                    loading = false
                }
            }) { Text("Retry") }
            Spacer(Modifier.height(12.dp))
        }
        Button(
            enabled = !loggingOut,
            onClick = {
                scope.launch {
                    loggingOut = true
                    runCatching {
                        withContext(Dispatchers.IO) {
                            LoanApiService.instance.logout("Bearer $authToken")
                        }
                    }
                    onLogout()
                    loggingOut = false
                }
            }
        ) {
            Text(if (loggingOut) "Signing out…" else "Sign out")
        }
    }
}
