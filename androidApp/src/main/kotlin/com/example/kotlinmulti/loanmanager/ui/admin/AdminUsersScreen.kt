package com.example.kotlinmulti.loanmanager.ui.admin

import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import com.example.kotlinmulti.loanmanager.data.remote.CreateUserRequest
import com.example.kotlinmulti.loanmanager.data.remote.LoanApiService
import com.example.kotlinmulti.loanmanager.data.remote.UserDto
import com.example.kotlinmulti.loanmanager.ui.components.CustomBottomNavigationBar
import com.example.kotlinmulti.loanmanager.ui.demo.OfflineDemoMode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import retrofit2.HttpException

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminUsersScreen(
    authToken: String,
    isOwner: Boolean,
    isOfflinePreview: Boolean,
    onNavigate: (String) -> Unit,
    onUserClick: (String) -> Unit
) {
    var users by remember(authToken) { mutableStateOf<List<UserDto>>(emptyList()) }
    var totalCount by remember(authToken) { mutableStateOf<Int?>(null) }
    var isLoading by remember(authToken) { mutableStateOf(true) }
    var errorMessage by remember(authToken) { mutableStateOf<String?>(null) }
    var query by remember { mutableStateOf("") }
    var retryKey by remember { mutableStateOf(0) }
    var showCreateUserDialog by remember { mutableStateOf(false) }
    var isCreatingUser by remember { mutableStateOf(false) }
    var createUserError by remember { mutableStateOf<String?>(null) }
    var createUserSuccess by remember { mutableStateOf<String?>(null) }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(authToken, isOfflinePreview, retryKey) {
        isLoading = true
        errorMessage = null
        if (isOfflinePreview) {
            users = OfflineDemoMode.visibleUsers()
            totalCount = users.size
        } else {
            runCatching {
                withContext(Dispatchers.IO) {
                    LoanApiService.instance.getUsers("Bearer $authToken")
                }
            }.onSuccess { response ->
                users = response.users.orEmpty()
                totalCount = response.meta?.totalUserCount ?: response.users?.size
                if (response.users == null) errorMessage = response.message ?: "The server did not return a user list."
            }.onFailure { failure ->
                errorMessage = failure.localizedMessage ?: "Could not load users."
            }
        }
        isLoading = false
    }

    fun createUser(name: String, email: String, password: String, role: String) {
        if (!isOwner) return
        val cleanName = name.trim()
        val cleanEmail = email.trim()
        if (cleanName.isBlank() || cleanEmail.isBlank() || password.isBlank()) {
            createUserError = "Enter a name, email, password, and role."
            return
        }
        if (!cleanEmail.contains('@')) {
            createUserError = "Enter a valid email address."
            return
        }
        coroutineScope.launch {
            isCreatingUser = true
            createUserError = null
            createUserSuccess = null
            runCatching {
                if (isOfflinePreview) {
                    check(OfflineDemoMode.createDemoUser(cleanName, cleanEmail, password, role)) {
                        "That email already exists or the user details are invalid."
                    }
                    "User created in this offline preview."
                } else {
                    withContext(Dispatchers.IO) {
                        val response = LoanApiService.instance.createUser(
                            token = "Bearer $authToken",
                            request = CreateUserRequest(
                                name = cleanName,
                                email = cleanEmail,
                                password = password,
                                role = role
                            )
                        )
                        check(response.data != null) { response.message ?: "The server did not return the created user." }
                        response.message ?: "User registered successfully."
                    }
                }
            }.onSuccess { message ->
                showCreateUserDialog = false
                createUserSuccess = message
                retryKey++
            }.onFailure { failure ->
                createUserError = failure.apiMessageOr("Could not create this user.")
            }
            isCreatingUser = false
        }
    }

    val visibleUsers = remember(users, query) {
        users.filter { user ->
            query.isBlank() || user.name.contains(query, ignoreCase = true) ||
                user.email.contains(query, ignoreCase = true) || user.role.contains(query, ignoreCase = true)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Loan Manager", fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
                        Text("Admin console", fontSize = 14.sp, color = MaterialTheme.colorScheme.onPrimary.copy(alpha = .8f))
                    }
                },
                actions = {
                    if (isOwner) {
                        IconButton(
                            onClick = {
                                createUserError = null
                                showCreateUserDialog = true
                            },
                            enabled = !isCreatingUser
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Add user",
                                tint = androidx.compose.ui.graphics.Color.White // Add this line
                            )

                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },
        bottomBar = { CustomBottomNavigationBar(currentRoute = "admin", showAdmin = true, onNavigate = onNavigate) },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            Card(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 14.dp),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            ) {
                Row(
                    Modifier.fillMaxWidth().padding(18.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Surface(shape = RoundedCornerShape(13.dp), color = MaterialTheme.colorScheme.primary, modifier = Modifier.size(48.dp)) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.AdminPanelSettings, null, tint = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(27.dp))
                        }
                    }
                    Column(Modifier.weight(1f)) {
                        Text("User directory", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer)
                        Text("View staff and account access", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = .75f))
                    }
                    Text(totalCount?.toString() ?: "—", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                }
            }

            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                placeholder = { Text("Search users") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                    focusedBorderColor = MaterialTheme.colorScheme.primary
                )
            )
            if (createUserSuccess != null) {
                Text(
                    createUserSuccess!!,
                    modifier = Modifier.padding(start = 18.dp, top = 9.dp),
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.bodySmall
                )
            }
            Text(
                text = if (query.isBlank()) "USERS" else "${visibleUsers.size} MATCHING USERS",
                modifier = Modifier.padding(start = 20.dp, top = 18.dp, bottom = 8.dp),
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                letterSpacing = 1.sp
            )
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            when {
                isLoading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
                errorMessage != null -> Column(
                    Modifier.fillMaxWidth().padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(errorMessage!!, color = MaterialTheme.colorScheme.error)
                    androidx.compose.material3.OutlinedButton(onClick = { retryKey++ }) { Text("Retry") }
                }
                visibleUsers.isEmpty() -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(if (users.isEmpty()) "No user accounts found." else "No users match your search.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                else -> LazyColumn(Modifier.fillMaxSize()) {
                    items(visibleUsers, key = { it.id }) { user ->
                        UserDirectoryRow(user = user, onClick = { onUserClick(user.id) })
                        HorizontalDivider(Modifier.padding(start = 72.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = .65f))
                    }
                }
            }
        }
    }

    if (showCreateUserDialog) {
        CreateUserDialog(
            isSaving = isCreatingUser,
            errorMessage = createUserError,
            onDismiss = { if (!isCreatingUser) showCreateUserDialog = false },
            onCreate = ::createUser
        )
    }
}

@Composable
private fun CreateUserDialog(
    isSaving: Boolean,
    errorMessage: String?,
    onDismiss: () -> Unit,
    onCreate: (name: String, email: String, password: String, role: String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var role by remember { mutableStateOf("STAFF") }
    var roleMenuExpanded by remember { mutableStateOf(false) }
    val valid = name.isNotBlank() && email.contains('@') && password.isNotBlank()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add user") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Name") },
                    singleLine = true
                )
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Email") },
                    singleLine = true
                )
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Password") },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation()
                )
                Box(Modifier.fillMaxWidth()) {
                    OutlinedButton(onClick = { roleMenuExpanded = true }, modifier = Modifier.fillMaxWidth()) {
                        Text("Role: ${role.toRoleLabel()}", modifier = Modifier.weight(1f))
                        Icon(Icons.Default.ArrowDropDown, contentDescription = "Select role")
                    }
                    DropdownMenu(
                        expanded = roleMenuExpanded,
                        onDismissRequest = { roleMenuExpanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Manager") },
                            onClick = { role = "MANAGER"; roleMenuExpanded = false }
                        )
                        DropdownMenuItem(
                            text = { Text("Staff") },
                            onClick = { role = "STAFF"; roleMenuExpanded = false }
                        )
                    }
                }
                if (errorMessage != null) {
                    Text(errorMessage, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onCreate(name, email, password, role) },
                enabled = valid && !isSaving
            ) { Text(if (isSaving) "Creating…" else "Create user") }
        },
        dismissButton = { TextButton(onClick = onDismiss, enabled = !isSaving) { Text("Cancel") } }
    )
}

@Composable
private fun UserDirectoryRow(user: UserDto, onClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick).padding(horizontal = 18.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(shape = CircleShape, color = MaterialTheme.colorScheme.primary.copy(alpha = .12f), modifier = Modifier.size(42.dp)) {
            Box(contentAlignment = Alignment.Center) {
                Text(user.name.initials(), color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
            }
        }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(user.name, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Spacer(Modifier.height(3.dp))
            Text(user.email, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
        Spacer(Modifier.width(8.dp))
        Surface(shape = RoundedCornerShape(50), color = if (user.role.equals("OWNER", true)) MaterialTheme.colorScheme.primary.copy(alpha = .12f) else MaterialTheme.colorScheme.surfaceContainerHigh) {
            Text(user.role.toRoleLabel(), modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp), style = MaterialTheme.typography.labelSmall, color = if (user.role.equals("OWNER", true)) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.SemiBold)
        }
    }
}

internal fun String.initials(): String = trim().split(Regex("\\s+")).filter(String::isNotBlank).take(2).mapNotNull { it.firstOrNull() }.joinToString("").uppercase().ifBlank { "U" }

internal fun String.toRoleLabel(): String = replace('_', ' ').lowercase().replaceFirstChar { it.uppercase() }

private fun Throwable.apiMessageOr(fallback: String): String {
    val responseBody = (this as? HttpException)?.response()?.errorBody()?.string()
    val serverMessage = runCatching { JSONObject(responseBody.orEmpty()).optString("message") }.getOrNull()
    return serverMessage?.takeIf(String::isNotBlank) ?: localizedMessage ?: fallback
}
