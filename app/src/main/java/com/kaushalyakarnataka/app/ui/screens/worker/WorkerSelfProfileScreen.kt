package com.kaushalyakarnataka.app.ui.screens.worker

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import androidx.hilt.navigation.compose.hiltViewModel
import com.kaushalyakarnataka.app.data.model.ServiceCategory
import com.kaushalyakarnataka.app.ui.components.*
import com.kaushalyakarnataka.app.ui.theme.*
import com.kaushalyakarnataka.app.utils.UiState
import com.kaushalyakarnataka.app.viewmodel.AuthViewModel
import com.kaushalyakarnataka.app.viewmodel.WorkerProfileViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun WorkerSelfProfileScreen(
    onNavigateBottomBar: (NavDestination) -> Unit,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier,
    authViewModel: AuthViewModel = hiltViewModel(),
    viewModel: WorkerProfileViewModel = hiltViewModel()
) {
    LaunchedEffect(Unit) { viewModel.loadWorkerProfile() }

    val profileState by viewModel.workerState.collectAsState()
    val themeState = LocalThemeState.current
    var showEditDialog by remember { mutableStateOf(false) }
    var showBioDialog by remember { mutableStateOf(false) }
    var showNotifDialog by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }

    val updateState by viewModel.updateState.collectAsState()
    LaunchedEffect(updateState) {
        when (updateState) {
            is UiState.Success -> {
                snackbarHostState.showSnackbar("Profile updated successfully")
                viewModel.clearUpdateState()
            }
            is UiState.Error -> {
                snackbarHostState.showSnackbar((updateState as UiState.Error).message)
                viewModel.clearUpdateState()
            }
            else -> {}
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("My Profile", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold) },
                actions = {
                    IconButton(onClick = { showEditDialog = true }) {
                        Icon(Icons.Default.Edit, "Edit Profile")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        },
        bottomBar = {
            WorkerBottomNav(currentDestination = WorkerNavDestination.PROFILE, onNavigate = { dest ->
                when (dest) {
                    WorkerNavDestination.DASHBOARD -> onNavigateBottomBar(NavDestination.HOME)
                    WorkerNavDestination.PROFILE -> { /* already here */ }
                    else -> onNavigateBottomBar(NavDestination.BOOKINGS)
                }
            })
        }
    ) { paddingValues ->
        when (val state = profileState) {
            is UiState.Loading -> {
                Box(Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Primary)
                }
            }
            is UiState.Error -> {
                Box(Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.ErrorOutline, null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(48.dp))
                        Spacer(Modifier.height(8.dp))
                        Text(state.message, color = MaterialTheme.colorScheme.error)
                        Spacer(Modifier.height(16.dp))
                        Button(onClick = { viewModel.loadWorkerProfile() }) { Text("Retry") }
                    }
                }
            }
            is UiState.Success -> {
                val worker = state.data
                LazyColumn(
                    modifier = modifier.fillMaxSize().padding(paddingValues),
                    contentPadding = PaddingValues(bottom = 24.dp)
                ) {
                    // Hero profile card
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Brush.verticalGradient(listOf(Color(0xFF0F2055), PrimaryLight)))
                                .padding(24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                // Avatar
                                Box(contentAlignment = Alignment.BottomEnd) {
                                    AvatarComponent(imageUrl = worker.avatarUrl, name = worker.name, size = 96.dp)
                                    if (worker.isVerified) {
                                        Box(
                                            modifier = Modifier.size(26.dp).clip(CircleShape)
                                                .background(Primary).border(2.dp, Color.White, CircleShape),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(Icons.Default.Verified, null, tint = Color.White, modifier = Modifier.size(15.dp))
                                        }
                                    }
                                }
                                Spacer(Modifier.height(12.dp))
                                Text(worker.name, style = MaterialTheme.typography.titleLarge, color = Color.White, fontWeight = FontWeight.Bold)
                                Text(
                                    if (worker.role.isNotBlank()) worker.role else worker.category.displayName,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color.White.copy(alpha = 0.8f)
                                )
                                Spacer(Modifier.height(12.dp))
                                // Chips row
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Surface(shape = RoundedCornerShape(20.dp), color = Color.White.copy(alpha = 0.15f)) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Icon(Icons.Default.Star, null, tint = Warning, modifier = Modifier.size(14.dp))
                                            if (worker.rating > 0) {
                                                Text(
                                                    String.format("%.1f", worker.rating),
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = Color.White
                                                )
                                            }
                                        }
                                    }
                                    if (worker.isAvailable) {
                                        Surface(shape = RoundedCornerShape(20.dp), color = Color(0xFF16A34A).copy(alpha = 0.3f)) {
                                            Text(
                                                "Available",
                                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                                style = MaterialTheme.typography.labelSmall,
                                                color = Color.White
                                            )
                                        }
                                    }
                                    if (worker.location.isNotBlank()) {
                                        Surface(shape = RoundedCornerShape(20.dp), color = Color.White.copy(alpha = 0.15f)) {
                                            Row(
                                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                                            ) {
                                                Icon(Icons.Default.LocationOn, null, tint = Secondary, modifier = Modifier.size(13.dp))
                                                Text(worker.location.take(16), style = MaterialTheme.typography.labelSmall, color = Color.White)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Stats strip
                    item {
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            color = MaterialTheme.colorScheme.surface,
                            shadowElevation = 4.dp
                        ) {
                            Row(modifier = Modifier.fillMaxWidth()) {
                                listOf(
                                    Triple("${worker.reviewCount}", "Reviews", Icons.Default.Reviews),
                                    Triple(worker.category.displayName, "Role", Icons.Default.Work),
                                    Triple(if (worker.isAvailable) "Available" else "Busy", "Status", Icons.Default.EventAvailable),
                                ).forEachIndexed { i, (val_, label, icon) ->
                                    Column(
                                        modifier = Modifier.weight(1f).padding(vertical = 14.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Icon(icon, null, tint = Primary, modifier = Modifier.size(18.dp))
                                        Spacer(Modifier.height(3.dp))
                                        Text(val_, style = MaterialTheme.typography.labelLarge, color = Primary, fontWeight = FontWeight.Bold)
                                        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                    if (i < 2) HorizontalDivider(
                                        modifier = Modifier.width(1.dp).height(40.dp).padding(vertical = 8.dp),
                                        color = MaterialTheme.colorScheme.outlineVariant
                                    )
                                }
                            }
                        }
                    }

                    // About
                    item {
                        SectionBlock(title = "About Me", action = {
                            TextButton(onClick = { showBioDialog = true }) {
                                Icon(Icons.Default.Edit, null, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("Edit")
                            }
                        }) {
                            if (worker.bio.isNotBlank()) {
                                Text(worker.bio, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            } else {
                                Text(
                                    "No bio added yet. Tap edit to add a short introduction.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                                )
                            }
                        }
                    }

                    // Tags / Skills
                    if (worker.tags.isNotEmpty()) {
                        item {
                            SectionBlock(title = "Skills & Specialties") {
                                androidx.compose.foundation.layout.FlowRow(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    worker.tags.forEach { tag ->
                                        Surface(shape = RoundedCornerShape(20.dp), color = PrimaryTint) {
                                            Text(tag, modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                                style = MaterialTheme.typography.labelSmall, color = Primary)
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Settings
                    item {
                        SectionBlock(title = "Settings") {
                            SettingsRow(Icons.Default.Notifications, "Notifications") { showNotifDialog = true }
                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(0.4f))
                            SettingsRow(
                                if (themeState.isDark) Icons.Default.LightMode else Icons.Default.DarkMode,
                                "Dark Mode"
                            ) { themeState.toggle() }
                        }
                    }

                    // Logout
                    item {
                        Spacer(Modifier.height(8.dp))
                        Surface(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
                            shape = RoundedCornerShape(14.dp),
                            color = ErrorTint
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth().clickable {
                                    authViewModel.logout()
                                    onLogout()
                                }.padding(horizontal = 16.dp, vertical = 16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.AutoMirrored.Filled.Logout, null, tint = Error, modifier = Modifier.size(22.dp))
                                Spacer(Modifier.width(12.dp))
                                Text("Log Out", style = MaterialTheme.typography.bodyMedium, color = Error, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                }
            }
        }
    }

    if (showNotifDialog) {
        WorkerNotifDialog(onDismiss = { showNotifDialog = false })
    }
    if (showEditDialog && profileState is UiState.Success) {
        val w = (profileState as UiState.Success).data
        WorkerEditDialog(
            currentName = w.name,
            currentBio = w.bio,
            currentPhone = w.phone,
            currentRole = w.role,
            currentCategory = w.category,
            currentBaseCharge = w.displayBaseCharge,
            onDismiss = { showEditDialog = false },
            onSave = { name, phone, bio, role, category, baseCharge ->
                viewModel.updateWorkerProfile(name, phone, bio, role, category, baseCharge)
                showEditDialog = false
            }
        )
    }

    if (showBioDialog && profileState is UiState.Success) {
        val w = (profileState as UiState.Success).data
        BioEditDialog(
            currentBio = w.bio,
            onDismiss = { showBioDialog = false },
            onSave = { bio ->
                viewModel.updateBio(bio)
                showBioDialog = false
            }
        )
    }
}

@Composable
private fun SectionBlock(title: String, action: @Composable (() -> Unit)? = null, content: @Composable () -> Unit) {
    Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            action?.invoke()
        }
        Spacer(Modifier.height(10.dp))
        content()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BioEditDialog(
    currentBio: String,
    onDismiss: () -> Unit,
    onSave: (String) -> Unit
) {
    var bio by remember { mutableStateOf(currentBio) }
    val maxChars = 500
    val isValid = bio.length <= maxChars

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = { Icon(Icons.Default.Person, null, tint = Primary) },
        title = { Text("Edit Bio") },
        text = {
            Column {
                OutlinedTextField(
                    value = bio,
                    onValueChange = { if (it.length <= maxChars) bio = it },
                    label = { Text("About Me") },
                    placeholder = { Text("Tell customers about your experience and skills...") },
                    modifier = Modifier.fillMaxWidth().heightIn(min = 120.dp, max = 200.dp),
                    shape = RoundedCornerShape(12.dp),
                    maxLines = 6,
                    isError = !isValid,
                    supportingText = {
                        Text(
                            "${bio.length} / $maxChars",
                            color = if (bio.length > maxChars * 0.9) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onSave(bio.trim()) },
                enabled = isValid
            ) { Text("Save") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@Composable
private fun SettingsRow(icon: ImageVector, label: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick).padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, null, tint = Primary, modifier = Modifier.size(22.dp))
        Spacer(Modifier.width(14.dp))
        Text(label, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
        Icon(Icons.Default.ChevronRight, null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(20.dp))
    }
}

@Composable
private fun WorkerNotifDialog(onDismiss: () -> Unit) {
    var jobRequests by remember { mutableStateOf(true) }
    var bookingUpdates by remember { mutableStateOf(true) }
    var paymentAlerts by remember { mutableStateOf(true) }

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = { Icon(Icons.Default.Notifications, null, tint = Primary) },
        title = { Text("Notifications") },
        text = {
            Column {
                SwitchRow("New Job Requests", jobRequests) { jobRequests = it }
                SwitchRow("Booking Status Updates", bookingUpdates) { bookingUpdates = it }
                SwitchRow("Payment Alerts", paymentAlerts) { paymentAlerts = it }
            }
        },
        confirmButton = { Button(onClick = onDismiss) { Text("Save") } }
    )
}

@Composable
private fun SwitchRow(label: String, checked: Boolean, onToggle: (Boolean) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium)
        Switch(checked = checked, onCheckedChange = onToggle, colors = SwitchDefaults.colors(checkedTrackColor = Primary))
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
private fun WorkerEditDialog(
    currentName: String,
    currentBio: String,
    currentPhone: String,
    currentRole: String,
    currentCategory: ServiceCategory,
    currentBaseCharge: Int,
    onDismiss: () -> Unit,
    onSave: (String, String, String, String, ServiceCategory, Int) -> Unit
) {
    var name by remember { mutableStateOf(currentName) }
    var bio by remember { mutableStateOf(currentBio) }
    var phone by remember { mutableStateOf(currentPhone) }
    var role by remember { mutableStateOf(currentRole) }
    var category by remember { mutableStateOf(currentCategory) }
    var baseCharge by remember { mutableStateOf(if (currentBaseCharge > 0) currentBaseCharge.toString() else "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Profile") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Full Name") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp))
                OutlinedTextField(value = phone, onValueChange = { phone = it }, label = { Text("Phone") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Phone))
                OutlinedTextField(value = role, onValueChange = { role = it }, label = { Text("Role") }, placeholder = { Text("Electrician, Painter") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp))
                OutlinedTextField(
                    value = baseCharge,
                    onValueChange = { baseCharge = it.filter { c -> c.isDigit() } },
                    label = { Text("Base labour charge") },
                    prefix = { Text("Rs. ") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number)
                )
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Category", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        ServiceCategory.values().forEach { cat ->
                            FilterChip(
                                selected = category == cat,
                                onClick = { category = cat },
                                label = { Text(cat.displayName) },
                                leadingIcon = if (category == cat) {
                                    { Icon(Icons.Default.Check, null, modifier = Modifier.size(16.dp)) }
                                } else null
                            )
                        }
                    }
                }
                OutlinedTextField(value = bio, onValueChange = { bio = it }, label = { Text("About Me / Bio") }, modifier = Modifier.fillMaxWidth().height(100.dp), shape = RoundedCornerShape(12.dp), maxLines = 4)
            }
        },
        confirmButton = { Button(onClick = { onSave(name, phone, bio, role, category, baseCharge.toIntOrNull() ?: 0) }, enabled = name.isNotBlank() && (baseCharge.toIntOrNull() ?: 0) > 0) { Text("Save") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}
