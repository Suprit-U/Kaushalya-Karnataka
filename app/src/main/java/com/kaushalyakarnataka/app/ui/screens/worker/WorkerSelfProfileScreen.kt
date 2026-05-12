package com.kaushalyakarnataka.app.ui.screens.worker

import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.EventNote
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
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
                title = { Text("My Profile", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = Color.White) },
                actions = {
                    IconButton(onClick = { showEditDialog = true }) {
                        Icon(Icons.Default.Edit, "Edit Profile", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    scrolledContainerColor = Color.Transparent
                )
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
                        val orbTrans = rememberInfiniteTransition(label = "prof_orbs")
                        val profOrb1 by orbTrans.animateFloat(
                            initialValue = -6f, targetValue = 6f,
                            animationSpec = infiniteRepeatable(tween(6000, easing = LinearEasing), RepeatMode.Reverse),
                            label = "prof_orb1"
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Brush.verticalGradient(listOf(Color(0xFF0F2055), PrimaryLight)))
                                .padding(top = 20.dp, bottom = 32.dp)
                                .padding(horizontal = 20.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(80.dp)
                                    .offset(x = (-20).dp, y = profOrb1.dp)
                                    .clip(CircleShape)
                                    .background(Color.White.copy(0.05f))
                                    .align(Alignment.TopStart)
                            )
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                // Avatar with glass ring
                                Box(contentAlignment = Alignment.BottomEnd) {
                                    Box(
                                        modifier = Modifier
                                            .size(108.dp)
                                            .clip(CircleShape)
                                            .background(Color.White.copy(0.15f))
                                            .padding(4.dp)
                                    ) {
                                        AvatarComponent(imageUrl = worker.avatarUrl, name = worker.name, size = 100.dp)
                                    }
                                    if (worker.isVerified) {
                                        Box(
                                            modifier = Modifier
                                                .size(30.dp)
                                                .clip(CircleShape)
                                                .background(Primary)
                                                .border(2.dp, Color.White, CircleShape),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(Icons.Default.Verified, null, tint = Color.White, modifier = Modifier.size(16.dp))
                                        }
                                    }
                                }
                                Spacer(Modifier.height(16.dp))
                                Text(
                                    worker.name,
                                    style = MaterialTheme.typography.headlineSmall,
                                    color = Color.White,
                                    fontWeight = FontWeight.ExtraBold
                                )
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    if (worker.role.isNotBlank()) worker.role else worker.category.displayName,
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = Color.White.copy(alpha = 0.85f),
                                    fontWeight = FontWeight.Medium
                                )
                                Spacer(Modifier.height(14.dp))
                                // Premium chips row
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Surface(
                                        shape = RoundedCornerShape(20.dp),
                                        color = Color.White.copy(alpha = 0.12f),
                                        border = BorderStroke(1.dp, Color.White.copy(0.2f))
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Icon(Icons.Default.Star, null, tint = Warning, modifier = Modifier.size(14.dp))
                                            if (worker.rating > 0) {
                                                Text(
                                                    String.format("%.1f", worker.rating),
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = Color.White,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                        }
                                    }
                                    if (worker.isAvailable) {
                                        Surface(
                                            shape = RoundedCornerShape(20.dp),
                                            color = Color(0xFF22C55E).copy(alpha = 0.25f),
                                            border = BorderStroke(1.dp, Color(0xFF22C55E).copy(0.4f))
                                        ) {
                                            Text(
                                                "Available",
                                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                                style = MaterialTheme.typography.labelSmall,
                                                color = Color.White,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                    if (worker.location.isNotBlank()) {
                                        Surface(
                                            shape = RoundedCornerShape(20.dp),
                                            color = Color.White.copy(alpha = 0.12f),
                                            border = BorderStroke(1.dp, Color.White.copy(0.2f))
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
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
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp)
                                .offset(y = (-16).dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            WorkerStatCard(
                                icon = Icons.AutoMirrored.Filled.EventNote,
                                label = "Jobs",
                                value = "${worker.reviewCount}",
                                modifier = Modifier.weight(1f)
                            )
                            WorkerStatCard(
                                icon = Icons.Default.Work,
                                label = "Role",
                                value = worker.category.displayName.take(8),
                                modifier = Modifier.weight(1f)
                            )
                            WorkerStatCard(
                                icon = Icons.Default.EventAvailable,
                                label = "Status",
                                value = if (worker.isAvailable) "Active" else "Busy",
                                modifier = Modifier.weight(1f)
                            )
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
                        }
                    }

                    // Logout
                    item {
                        Spacer(Modifier.height(8.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp)
                                .height(52.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(Brush.horizontalGradient(listOf(Error.copy(0.9f), Error.copy(0.7f))))
                                .clickable {
                                    authViewModel.logout()
                                    onLogout()
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.AutoMirrored.Filled.Logout, null, tint = Color.White, modifier = Modifier.size(20.dp))
                                Spacer(Modifier.width(10.dp))
                                Text("Log Out", style = MaterialTheme.typography.titleMedium, color = Color.White, fontWeight = FontWeight.Bold)
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
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 8.dp)
            .shadow(3.dp, RoundedCornerShape(16.dp), spotColor = Primary.copy(0.04f)),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 1.dp,
        border = BorderStroke(1.dp, Border.copy(0.3f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = Text1)
                action?.invoke()
            }
            Spacer(Modifier.height(12.dp))
            content()
        }
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
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(Primary.copy(0.08f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, null, tint = Primary.copy(0.8f), modifier = Modifier.size(20.dp))
        }
        Spacer(Modifier.width(14.dp))
        Text(label, style = MaterialTheme.typography.bodyMedium, color = Text1, modifier = Modifier.weight(1f))
        Icon(Icons.Default.ChevronRight, null, tint = Text4, modifier = Modifier.size(20.dp))
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

@Composable
private fun WorkerStatCard(
    icon: ImageVector,
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .shadow(3.dp, RoundedCornerShape(16.dp), spotColor = Primary.copy(0.05f)),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 1.dp,
        border = BorderStroke(1.dp, Border.copy(0.3f))
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Primary.copy(0.08f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, null, tint = Primary.copy(0.8f), modifier = Modifier.size(20.dp))
            }
            Spacer(Modifier.height(8.dp))
            Text(
                value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.ExtraBold,
                color = Text1,
                maxLines = 1
            )
            Text(
                label,
                style = MaterialTheme.typography.labelSmall,
                color = Text3,
                maxLines = 1
            )
        }
    }
}
