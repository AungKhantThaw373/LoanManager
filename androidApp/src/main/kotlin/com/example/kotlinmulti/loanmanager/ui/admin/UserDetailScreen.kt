package com.example.kotlinmulti.loanmanager.ui.admin

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.filled.AlternateEmail
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.json.JSONObject
import retrofit2.HttpException
import com.example.kotlinmulti.loanmanager.data.remote.ChangePasswordRequest
import com.example.kotlinmulti.loanmanager.data.remote.DeleteUsersRequest
import com.example.kotlinmulti.loanmanager.data.remote.LoanApiService
import com.example.kotlinmulti.loanmanager.data.remote.UserDto
import com.example.kotlinmulti.loanmanager.ui.demo.OfflineDemoMode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserDetailScreen(
    userId: String,
    authToken: String,
    isOwner: Boolean,
    isOfflinePreview: Boolean,
    onBackClick: () -> Unit,
    onUserDeleted: () -> Unit
) {
    var user by remember(userId, authToken) { mutableStateOf<UserDto?>(null) }
    var isLoading by remember(userId, authToken) { mutableStateOf(true) }
    var errorMessage by remember(userId, authToken) { mutableStateOf<String?>(null) }
    var retryKey by remember { mutableStateOf(0) }
    var showPasswordDialog by remember(userId) { mutableStateOf(false) }
    var showDeleteConfirmation by remember(userId) { mutableStateOf(false) }
    var isActionInProgress by remember(userId) { mutableStateOf(false) }
    var actionMessage by remember(userId) { mutableStateOf<String?>(null) }
    var actionError by remember(userId) { mutableStateOf<String?>(null) }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(userId, authToken, isOfflinePreview, retryKey) {
        isLoading = true
        errorMessage = null
        if (isOfflinePreview) {
            user = OfflineDemoMode.visibleUsers().firstOrNull { it.id == userId }
            if (user == null) errorMessage = "This demo user could not be found."
        } else {
            runCatching {
                withContext(Dispatchers.IO) {
                    LoanApiService.instance.getUser(userId, "Bearer $authToken")
                }
            }.onSuccess { response ->
                user = response.user
                if (user == null) errorMessage = response.message ?: "User details were not returned."
            }.onFailure { failure ->
                errorMessage = failure.localizedMessage ?: "Could not load this user."
            }
        }
        isLoading = false
    }

    fun changePassword(newPassword: String, targetCurrentPassword: String) {
        if (newPassword.isBlank() || targetCurrentPassword.isBlank()) {
            actionError = "Enter both the new password and the user's current password."
            return
        }
        coroutineScope.launch {
            isActionInProgress = true
            actionError = null
            actionMessage = null
            runCatching {
                if (isOfflinePreview) {
                    check(OfflineDemoMode.changeDemoUserPassword(userId, targetCurrentPassword, newPassword)) {
                        "The current password is incorrect. Offline preview users use Preview123! initially."
                    }
                    "Password changed in this offline preview."
                } else {
                    withContext(Dispatchers.IO) {
                        LoanApiService.instance.changeUserPassword(
                            userId = userId,
                            token = "Bearer $authToken",
                            request = ChangePasswordRequest(
                                password = newPassword,
                                confirmPassword = targetCurrentPassword
                            )
                        ).message ?: "Password changed successfully."
                    }
                }
            }.onSuccess { message ->
                actionMessage = message
                showPasswordDialog = false
            }.onFailure { failure ->
                actionError = failure.apiMessageOr("Could not change this user's password.")
            }
            isActionInProgress = false
        }
    }

    fun deleteUser() {
        coroutineScope.launch {
            isActionInProgress = true
            actionError = null
            actionMessage = null
            runCatching {
                if (isOfflinePreview) {
                    check(OfflineDemoMode.deleteDemoUser(userId)) { "This demo account cannot be deleted." }
                    "Demo account deleted from this offline preview."
                } else {
                    withContext(Dispatchers.IO) {
                        val response = LoanApiService.instance.deleteUsers(
                            token = "Bearer $authToken",
                            request = DeleteUsersRequest(ids = listOf(userId))
                        )
                        check(response.permanentlyDeletedUsers?.count != 0) {
                            response.message ?: "The account was not deleted."
                        }
                        response.message ?: "User deleted successfully."
                    }
                }
            }.onSuccess {
                showDeleteConfirmation = false
                onUserDeleted()
            }.onFailure { failure ->
                actionError = failure.apiMessageOr("Could not delete this user.")
            }
            isActionInProgress = false
        }
    }

    val canManageUser = isOwner && user?.role?.equals("OWNER", ignoreCase = true) == false

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("User details", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        when {
            isLoading -> Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
            user == null -> Column(
                Modifier.fillMaxSize().padding(padding).padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(errorMessage ?: "User unavailable", color = MaterialTheme.colorScheme.error)
                Spacer(Modifier.height(12.dp))
                OutlinedButton(onClick = { retryKey++ }) { Text("Retry") }
            }
            else -> UserDetailContent(
                user = user!!,
                canManageUser = canManageUser,
                actionMessage = actionMessage,
                actionError = actionError,
                isActionInProgress = isActionInProgress,
                onChangePasswordClick = { showPasswordDialog = true; actionError = null },
                onDeleteClick = { showDeleteConfirmation = true; actionError = null },
                modifier = Modifier.fillMaxSize().padding(padding)
            )
        }
    }

    if (showPasswordDialog && canManageUser) {
        ChangeUserPasswordDialog(
            isSaving = isActionInProgress,
            isOfflinePreview = isOfflinePreview,
            errorMessage = actionError,
            onDismiss = { if (!isActionInProgress) showPasswordDialog = false },
            onSave = ::changePassword
        )
    }

    if (showDeleteConfirmation && canManageUser) {
        val target = user
        if (target != null) {
            AlertDialog(
                onDismissRequest = { if (!isActionInProgress) showDeleteConfirmation = false },
                title = { Text("Delete user?") },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Permanently delete ${target.name}'s account? This cannot be undone.")
                        if (actionError != null) {
                            Text(actionError!!, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                },
                confirmButton = {
                    TextButton(
                        onClick = ::deleteUser,
                        enabled = !isActionInProgress,
                        colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                    ) { Text(if (isActionInProgress) "Deleting…" else "Delete permanently") }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteConfirmation = false }, enabled = !isActionInProgress) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}

@Composable
private fun UserDetailContent(
    user: UserDto,
    canManageUser: Boolean,
    actionMessage: String?,
    actionError: String?,
    isActionInProgress: Boolean,
    onChangePasswordClick: () -> Unit,
    onDeleteClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.verticalScroll(rememberScrollState()).padding(18.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(22.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Surface(shape = CircleShape, color = MaterialTheme.colorScheme.primary, modifier = Modifier.size(82.dp)) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(user.name.initials(), color = MaterialTheme.colorScheme.onPrimary, fontSize = 26.sp, fontWeight = FontWeight.Bold)
                    }
                }
                Spacer(Modifier.height(14.dp))
                Text(user.name, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer)
                Spacer(Modifier.height(6.dp))
                Surface(shape = RoundedCornerShape(50), color = MaterialTheme.colorScheme.primary.copy(alpha = .12f)) {
                    Text(user.role.toRoleLabel(), modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp), color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.labelMedium)
                }
                if (user.isDeleted == true) {
                    Spacer(Modifier.height(8.dp))
                    Text("Deactivated account", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.labelMedium)
                }
            }
        }

        DetailInfoCard(title = "Profile information") {
            UserInfoRow(Icons.Default.Person, "Full name", user.name)
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            UserInfoRow(Icons.Default.AlternateEmail, "Email address", user.email)
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            UserInfoRow(Icons.Default.Badge, "Role", user.role.toRoleLabel())
        }

        DetailInfoCard(title = "Account") {
            UserInfoRow(Icons.Default.Fingerprint, "User ID", user.id)
            user.createdAt?.let {
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                UserInfoRow(Icons.Default.CalendarMonth, "Joined", it.toDisplayDate())
            }
            user.updatedAt?.let {
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                UserInfoRow(Icons.Default.CalendarMonth, "Last updated", it.toDisplayDate())
            }
        }

        if (canManageUser) {
            DetailInfoCard(title = "Manage account") {
                Spacer(Modifier.height(8.dp))
                OutlinedButton(
                    onClick = onChangePasswordClick,
                    enabled = !isActionInProgress,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Lock, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Change password")
                }
                Spacer(Modifier.height(8.dp))
                OutlinedButton(
                    onClick = onDeleteClick,
                    enabled = !isActionInProgress,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Icon(Icons.Default.Delete, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Delete user")
                }
                if (actionError != null) {
                    Spacer(Modifier.height(8.dp))
                    Text(actionError, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                }
                if (actionMessage != null) {
                    Spacer(Modifier.height(8.dp))
                    Text(actionMessage, color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.bodySmall)
                }
            }
        }
        Text(
            if (canManageUser) "OWNER account controls" else "Read-only account details",
            modifier = Modifier.align(Alignment.CenterHorizontally),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun ChangeUserPasswordDialog(
    isSaving: Boolean,
    isOfflinePreview: Boolean,
    errorMessage: String?,
    onDismiss: () -> Unit,
    onSave: (newPassword: String, targetCurrentPassword: String) -> Unit
) {
    var newPassword by remember { mutableStateOf("") }
    var targetCurrentPassword by remember { mutableStateOf("") }
    val valid = newPassword.isNotBlank() && targetCurrentPassword.isNotBlank()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Change password") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    "Enter the new password and the user's current password for API verification.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (isOfflinePreview) {
                    Text(
                        "Offline preview accounts use Preview123! until changed.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                OutlinedTextField(
                    value = newPassword,
                    onValueChange = { newPassword = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("New password") },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation()
                )
                OutlinedTextField(
                    value = targetCurrentPassword,
                    onValueChange = { targetCurrentPassword = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("User's current password") },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation()
                )
                if (errorMessage != null) {
                    Text(errorMessage, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onSave(newPassword, targetCurrentPassword) }, enabled = valid && !isSaving) {
                Text(if (isSaving) "Saving…" else "Save password")
            }
        },
        dismissButton = { TextButton(onClick = onDismiss, enabled = !isSaving) { Text("Cancel") } }
    )
}

@Composable
private fun DetailInfoCard(title: String, content: @Composable () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(Modifier.fillMaxWidth().padding(horizontal = 17.dp, vertical = 12.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, modifier = Modifier.padding(vertical = 5.dp))
            content()
        }
    }
}

@Composable
private fun UserInfoRow(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, value: String) {
    Row(
        Modifier.fillMaxWidth().padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(12.dp))
        Column {
            Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(2.dp))
            Text(value, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurface, maxLines = 2, overflow = TextOverflow.Ellipsis)
        }
    }
}

private fun String.toDisplayDate(): String = take(10)

private fun Throwable.apiMessageOr(fallback: String): String {
    val responseBody = (this as? HttpException)?.response()?.errorBody()?.string()
    val serverMessage = runCatching { JSONObject(responseBody.orEmpty()).optString("message") }.getOrNull()
    return serverMessage?.takeIf(String::isNotBlank) ?: localizedMessage ?: fallback
}
