package com.example.kotlinmulti.loanmanager.ui.profile

import android.graphics.BitmapFactory
import android.net.Uri
import android.webkit.MimeTypeMap
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.AlternateEmail
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
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
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.kotlinmulti.loanmanager.data.remote.ChangePasswordRequest
import com.example.kotlinmulti.loanmanager.data.remote.CurrentUserDto
import com.example.kotlinmulti.loanmanager.data.remote.LoanApiService
import com.example.kotlinmulti.loanmanager.ui.components.CustomBottomNavigationBar
import com.example.kotlinmulti.loanmanager.ui.demo.OfflineDemoMode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.ByteArrayOutputStream
import java.net.URL

private const val MAX_PROFILE_PHOTO_BYTES = 5 * 1024 * 1024
private val AllowedPhotoTypes = setOf("image/jpeg", "image/jpg", "image/png", "image/webp")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    authToken: String,
    onNavigate: (String) -> Unit,
    onLogout: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var user by remember { mutableStateOf<CurrentUserDto?>(null) }
    var loading by remember { mutableStateOf(true) }
    var busy by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var message by remember { mutableStateOf<String?>(null) }
    var showEditDialog by remember { mutableStateOf(false) }
    var showPasswordDialog by remember { mutableStateOf(false) }
    val offlinePreview = OfflineDemoMode.isSession(authToken)

    suspend fun reloadProfile() {
        if (offlinePreview) {
            user = OfflineDemoMode.user
            error = null
            loading = false
            return
        }
        loading = true
        error = null
        runCatching {
            withContext(Dispatchers.IO) {
                LoanApiService.instance.getCurrentUser("Bearer $authToken")
            }
        }.onSuccess { response ->
            user = response.user
            if (user == null) error = response.message ?: "Your profile was not returned by the server."
        }.onFailure { failure ->
            error = failure.localizedMessage ?: "Could not load your profile."
        }
        loading = false
    }

    LaunchedEffect(authToken) { reloadProfile() }

    val photoPicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) {
            scope.launch {
                busy = true
                error = null
                message = null
                runCatching {
                    val mimeType = context.contentResolver.getType(uri)?.lowercase()
                        ?: throw IllegalArgumentException("Choose a JPEG, PNG, or WebP image.")
                    require(mimeType in AllowedPhotoTypes) { "Choose a JPEG, PNG, or WebP image." }
                    val bytes = withContext(Dispatchers.IO) {
                        val input = context.contentResolver.openInputStream(uri)
                            ?: throw IllegalStateException("Could not read the selected image.")
                        input.use {
                            val output = ByteArrayOutputStream()
                            val buffer = ByteArray(8192)
                            var total = 0
                            while (total <= MAX_PROFILE_PHOTO_BYTES) {
                                val count = it.read(buffer, 0, minOf(buffer.size, MAX_PROFILE_PHOTO_BYTES + 1 - total))
                                if (count < 0) break
                                output.write(buffer, 0, count)
                                total += count
                            }
                            output.toByteArray()
                        }
                    }
                    require(bytes.size <= MAX_PROFILE_PHOTO_BYTES) { "The profile photo must be 5 MB or smaller." }
                    val extension = MimeTypeMap.getSingleton().getExtensionFromMimeType(mimeType) ?: "jpg"
                    val photoPart = MultipartBody.Part.createFormData(
                        "photo",
                        "profile-photo.$extension",
                        bytes.toRequestBody(mimeType.toMediaTypeOrNull())
                    )
                    withContext(Dispatchers.IO) {
                        LoanApiService.instance.updateCurrentUser(
                            token = "Bearer $authToken",
                            name = null,
                            email = null,
                            photo = photoPart
                        )
                    }
                }.onSuccess { response ->
                    response.result?.let { user = it }
                    message = response.message ?: "Profile photo updated."
                    reloadProfile()
                }.onFailure { failure ->
                    error = failure.localizedMessage ?: "Could not update your photo."
                }
                busy = false
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Loan Manager", fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
                        Text("Your account", fontSize = 14.sp, color = MaterialTheme.colorScheme.onPrimary.copy(alpha = .8f))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },
        bottomBar = {
            CustomBottomNavigationBar(currentRoute = "profile", onNavigate = onNavigate)
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { insets ->
        Column(
            modifier = Modifier.fillMaxSize().padding(insets).verticalScroll(rememberScrollState()).padding(horizontal = 18.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            when {
                loading -> Box(Modifier.fillMaxWidth().height(220.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
                user == null -> Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(18.dp)
                ) {
                    Column(Modifier.fillMaxWidth().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.AccountCircle, null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(42.dp))
                        Spacer(Modifier.height(12.dp))
                        Text(error ?: "Profile unavailable", color = MaterialTheme.colorScheme.error)
                        Spacer(Modifier.height(12.dp))
                        Button(onClick = { scope.launch { reloadProfile() } }) { Text("Try again") }
                    }
                }
                else -> {
                    val profile = user!!
                    ProfileHeader(
                        user = profile,
                        onPhotoClick = { photoPicker.launch("image/*") },
                        busy = busy,
                        canEditPhoto = !offlinePreview
                    )
                    if (offlinePreview) {
                        Text(
                            "Offline preview — profile changes are disabled.",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                    if (error != null) Text(error!!, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium)
                    if (message != null) Text(message!!, color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.bodyMedium)
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        shape = RoundedCornerShape(18.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Column(Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 8.dp)) {
                            Text("Account details", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, modifier = Modifier.padding(vertical = 10.dp))
                            ProfileValue(Icons.Default.Person, "Full name", profile.name)
                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                            ProfileValue(Icons.Default.AlternateEmail, "Email address", profile.email)
                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                            ProfileValue(Icons.Default.Badge, "Access role", profile.role.replace('_', ' ').lowercase().replaceFirstChar { it.uppercase() })
                        }
                    }
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        shape = RoundedCornerShape(18.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Text("Security & profile", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            OutlinedButton(onClick = { showEditDialog = true }, enabled = !busy && !offlinePreview, modifier = Modifier.fillMaxWidth()) {
                                Icon(Icons.Default.Person, null, modifier = Modifier.size(18.dp)); Spacer(Modifier.size(8.dp)); Text("Edit profile")
                            }
                            OutlinedButton(onClick = { showPasswordDialog = true }, enabled = !busy && !offlinePreview, modifier = Modifier.fillMaxWidth()) {
                                Icon(Icons.Default.Security, null, modifier = Modifier.size(18.dp)); Spacer(Modifier.size(8.dp)); Text("Change password")
                            }
                        }
                    }
                    OutlinedButton(
                        onClick = {
                            scope.launch {
                                busy = true
                                runCatching {
                                    if (!offlinePreview) {
                                        withContext(Dispatchers.IO) { LoanApiService.instance.logout("Bearer $authToken") }
                                    }
                                }
                                onLogout()
                                busy = false
                            }
                        },
                        enabled = !busy,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        if (busy) CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp)
                        else Icon(Icons.AutoMirrored.Filled.Logout, null)
                        Spacer(Modifier.size(8.dp))
                        Text("Sign out")
                    }
                }
            }
        }
    }

    if (showEditDialog && user != null) {
        EditProfileDialog(
            currentUser = user!!,
            saving = busy,
            onDismiss = { showEditDialog = false },
            onSave = { name, email ->
                scope.launch {
                    busy = true
                    error = null
                    message = null
                    runCatching {
                        withContext(Dispatchers.IO) {
                            LoanApiService.instance.updateCurrentUser(
                                token = "Bearer $authToken",
                                name = name.trim().toRequestBody("text/plain".toMediaTypeOrNull()),
                                email = email.trim().toRequestBody("text/plain".toMediaTypeOrNull()),
                                photo = null
                            )
                        }
                    }.onSuccess { response ->
                        response.result?.let { user = it }
                        message = response.message ?: "Profile updated."
                        if (response.result == null) reloadProfile()
                        showEditDialog = false
                    }.onFailure { failure -> error = failure.localizedMessage ?: "Could not update your profile." }
                    busy = false
                }
            }
        )
    }

    if (showPasswordDialog) {
        ChangePasswordDialog(
            saving = busy,
            onDismiss = { showPasswordDialog = false },
            onSave = { newPassword, currentPassword ->
                scope.launch {
                    busy = true
                    error = null
                    message = null
                    runCatching {
                        withContext(Dispatchers.IO) {
                            LoanApiService.instance.changeMyPassword(
                                "Bearer $authToken",
                                ChangePasswordRequest(password = newPassword, confirmPassword = currentPassword)
                            )
                        }
                    }.onSuccess { response ->
                        message = response.message ?: "Password changed successfully."
                        showPasswordDialog = false
                    }.onFailure { failure -> error = failure.localizedMessage ?: "Could not change your password." }
                    busy = false
                }
            }
        )
    }
}

@Composable
private fun ProfileHeader(user: CurrentUserDto, onPhotoClick: () -> Unit, busy: Boolean, canEditPhoto: Boolean) {
    val photoBitmap by produceState<android.graphics.Bitmap?>(initialValue = null, user.photoUrl) {
        value = withContext(Dispatchers.IO) {
            runCatching {
                val path = user.photoUrl ?: return@runCatching null
                val target = if (path.startsWith("http://") || path.startsWith("https://")) path
                    else LoanApiService.BASE_URL.trimEnd('/') + "/" + path.trimStart('/')
                URL(target).openConnection().apply { connectTimeout = 8000; readTimeout = 8000 }
                    .getInputStream().use(BitmapFactory::decodeStream)
            }.getOrNull()
        }
    }
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    ) {
        Row(
            Modifier.fillMaxWidth().padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box {
                Surface(shape = CircleShape, color = MaterialTheme.colorScheme.primary, modifier = Modifier.size(76.dp)) {
                    if (photoBitmap != null) {
                        Image(photoBitmap!!.asImageBitmap(), contentDescription = "Profile photo", modifier = Modifier.fillMaxSize().clip(CircleShape), contentScale = ContentScale.Crop)
                    } else {
                        Box(contentAlignment = Alignment.Center) {
                            Text(user.name.split(' ').mapNotNull { it.firstOrNull() }.take(2).joinToString("").uppercase().ifBlank { "U" }, color = MaterialTheme.colorScheme.onPrimary, fontSize = 26.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
                IconButton(
                    onClick = onPhotoClick,
                    enabled = !busy && canEditPhoto,
                    modifier = Modifier.align(Alignment.BottomEnd).size(28.dp).background(MaterialTheme.colorScheme.surface, CircleShape)
                ) { Icon(Icons.Default.CameraAlt, "Change profile photo", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp)) }
            }
            Column(Modifier.weight(1f)) {
                Text(user.name, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer)
                Spacer(Modifier.height(4.dp))
                Text(user.email, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = .78f))
                Spacer(Modifier.height(9.dp))
                Surface(shape = RoundedCornerShape(50), color = MaterialTheme.colorScheme.primary.copy(alpha = .12f)) {
                    Text(user.role.replace('_', ' '), modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

@Composable
private fun ProfileValue(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, value: String) {
    Row(Modifier.fillMaxWidth().padding(vertical = 12.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Icon(icon, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
        Column {
            Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(value, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurface)
        }
    }
}

@Composable
private fun EditProfileDialog(currentUser: CurrentUserDto, saving: Boolean, onDismiss: () -> Unit, onSave: (String, String) -> Unit) {
    var name by remember(currentUser.id) { mutableStateOf(currentUser.name) }
    var email by remember(currentUser.id) { mutableStateOf(currentUser.email) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit profile") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(name, { name = it }, label = { Text("Full name") }, singleLine = true, leadingIcon = { Icon(Icons.Default.Person, null) })
                OutlinedTextField(email, { email = it }, label = { Text("Email address") }, singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email), leadingIcon = { Icon(Icons.Default.AlternateEmail, null) })
            }
        },
        confirmButton = { TextButton(onClick = { onSave(name, email) }, enabled = !saving && name.isNotBlank() && email.contains('@')) { Text(if (saving) "Saving…" else "Save changes") } },
        dismissButton = { TextButton(onClick = onDismiss, enabled = !saving) { Text("Cancel") } }
    )
}

@Composable
private fun ChangePasswordDialog(saving: Boolean, onDismiss: () -> Unit, onSave: (String, String) -> Unit) {
    var currentPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmNewPassword by remember { mutableStateOf("") }
    val valid = currentPassword.isNotBlank() && newPassword.length >= 8 && newPassword == confirmNewPassword
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Change password") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Use your current password to verify this change.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                OutlinedTextField(currentPassword, { currentPassword = it }, label = { Text("Current password") }, singleLine = true, visualTransformation = PasswordVisualTransformation(), leadingIcon = { Icon(Icons.Default.Lock, null) })
                OutlinedTextField(newPassword, { newPassword = it }, label = { Text("New password") }, singleLine = true, visualTransformation = PasswordVisualTransformation(), leadingIcon = { Icon(Icons.Default.Security, null) })
                OutlinedTextField(confirmNewPassword, { confirmNewPassword = it }, label = { Text("Confirm new password") }, singleLine = true, visualTransformation = PasswordVisualTransformation(), leadingIcon = { Icon(Icons.Default.Lock, null) })
                if (newPassword.isNotEmpty() && newPassword.length < 8) Text("Use at least 8 characters.", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                if (confirmNewPassword.isNotEmpty() && newPassword != confirmNewPassword) Text("Passwords do not match.", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
            }
        },
        confirmButton = { TextButton(onClick = { onSave(newPassword, currentPassword) }, enabled = !saving && valid) { Text(if (saving) "Updating…" else "Update password") } },
        dismissButton = { TextButton(onClick = onDismiss, enabled = !saving) { Text("Cancel") } }
    )
}
