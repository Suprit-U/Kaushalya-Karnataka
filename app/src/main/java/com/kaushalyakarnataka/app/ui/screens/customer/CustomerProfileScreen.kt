package com.kaushalyakarnataka.app.ui.screens.customer

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.*
import androidx.compose.material.icons.Icons
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
import com.kaushalyakarnataka.app.ui.components.*
import com.kaushalyakarnataka.app.ui.theme.*
import com.kaushalyakarnataka.app.utils.UiState
import com.kaushalyakarnataka.app.viewmodel.AuthViewModel
import com.kaushalyakarnataka.app.viewmodel.ProfileViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomerProfileScreen(
    onNavigateBottomBar: (NavDestination) -> Unit,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier,
    authViewModel: AuthViewModel = hiltViewModel(),
    profileViewModel: ProfileViewModel = hiltViewModel()
) {
    val profileState by profileViewModel.profileState.collectAsState()
    val updateState by profileViewModel.updateState.collectAsState()
    var showEditDialog by remember { mutableStateOf(false) }
    var showAddressDialog by remember { mutableStateOf(false) }
    var showNotifDialog by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val imagePickerLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let { profileViewModel.uploadAvatar(it) }
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
            KaushalyaBottomNav(currentDestination = NavDestination.PROFILE, onNavigate = onNavigateBottomBar)
        }
    ) { paddingValues ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Premium gradient header
            val orbTrans = rememberInfiniteTransition(label = "prof_orb")
            val profOrb by orbTrans.animateFloat(
                initialValue = -6f, targetValue = 6f,
                animationSpec = infiniteRepeatable(tween(6000, easing = LinearEasing), RepeatMode.Reverse),
                label = "prof_orb"
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Brush.verticalGradient(listOf(Primary, PrimaryLight)))
                    .padding(top = 0.dp, bottom = 32.dp)
            ) {
                // Decorative orb
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .offset(x = (-10).dp, y = profOrb.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(0.05f))
                        .align(Alignment.TopStart)
                )

                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                    Spacer(Modifier.height(20.dp))

                    // Avatar with glass ring and upload button
                    Box(contentAlignment = Alignment.BottomEnd) {
                        Box(
                            modifier = Modifier
                                .size(112.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(0.15f))
                                .padding(4.dp)
                        ) {
                            if (profileState is UiState.Success) {
                                val user = (profileState as UiState.Success).data
                                AvatarComponent(imageUrl = user.avatarUrl, name = user.name, size = 104.dp)
                            } else {
                                Box(
                                    Modifier.fillMaxSize().clip(CircleShape).background(PrimarySubtle),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.Person, null, tint = Primary, modifier = Modifier.size(48.dp))
                                }
                            }
                        }
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Color.White)
                                .shadow(2.dp, CircleShape)
                                .clickable { imagePickerLauncher.launch("image/*") },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.CameraAlt, null, tint = Primary, modifier = Modifier.size(18.dp))
                        }
                    }

                    Spacer(Modifier.height(16.dp))

                    if (profileState is UiState.Success) {
                        val user = (profileState as UiState.Success).data
                        Text(
                            user.name,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White
                        )
                        Spacer(Modifier.height(6.dp))
                        Text(
                            user.email,
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White.copy(0.8f)
                        )
                        if (user.phone.isNotBlank()) {
                            Spacer(Modifier.height(2.dp))
                            Text(
                                user.phone,
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.White.copy(0.65f)
                            )
                        }
                        Spacer(Modifier.height(12.dp))
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = Color.White.copy(0.2f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(0.3f))
                        ) {
                            Text(
                                "Customer",
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
                                style = MaterialTheme.typography.labelMedium,
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    } else {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(32.dp))
                    }
                    Spacer(Modifier.height(12.dp))
                }
            }

            // Account section
            ProfileSectionCard(title = "Account") {
                ProfileMenuItem(Icons.Default.Person, "Personal Info") { showEditDialog = true }
                ProfileMenuDivider()
                ProfileMenuItem(Icons.Default.LocationOn, "Saved Addresses") { showAddressDialog = true }
                ProfileMenuDivider()
                ProfileMenuItem(Icons.Default.Notifications, "Notifications") { showNotifDialog = true }
            }

            Spacer(Modifier.height(12.dp))

            // Logout
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

            Spacer(Modifier.height(24.dp))
        }
    }

    // Edit Profile Dialog
    if (showEditDialog && profileState is UiState.Success) {
        val user = (profileState as UiState.Success).data
        EditProfileDialog(
            currentName = user.name,
            currentPhone = user.phone,
            onDismiss = { showEditDialog = false },
            onSave = { n, p ->
                profileViewModel.updateProfile(n, p)
                showEditDialog = false
            }
        )
    }

    // Saved Addresses Dialog
    if (showAddressDialog && profileState is UiState.Success) {
        val user = (profileState as UiState.Success).data
        SavedAddressesSheet(
            currentHomeAddress = user.homeAddress,
            currentWorkAddress = user.workAddress,
            isSaving = updateState is UiState.Loading,
            onDismiss = { showAddressDialog = false },
            onSave = { home, work ->
                profileViewModel.updateSavedAddresses(home, work)
                showAddressDialog = false
            }
        )
    }

    // Notifications Dialog
    if (showNotifDialog) {
        NotificationsDialog(onDismiss = { showNotifDialog = false })
    }

    LaunchedEffect(updateState) {
        when (val state = updateState) {
            is UiState.Success -> {
                scope.launch { snackbarHostState.showSnackbar("Profile saved") }
                profileViewModel.clearUpdateState()
            }
            is UiState.Error -> {
                scope.launch { snackbarHostState.showSnackbar(state.message) }
                profileViewModel.clearUpdateState()
            }
            else -> {}
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SavedAddressesSheet(
    currentHomeAddress: String,
    currentWorkAddress: String,
    isSaving: Boolean,
    onDismiss: () -> Unit,
    onSave: (String, String) -> Unit
) {
    var homeAddress by remember(currentHomeAddress) { mutableStateOf(currentHomeAddress) }
    var workAddress by remember(currentWorkAddress) { mutableStateOf(currentWorkAddress) }
    var error by remember { mutableStateOf<String?>(null) }

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Icon(Icons.Default.LocationOn, null, tint = Primary)
                Text("Saved Addresses", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            }
            OutlinedTextField(
                value = homeAddress,
                onValueChange = { homeAddress = it; error = null },
                label = { Text("Home address") },
                leadingIcon = { Icon(Icons.Default.Home, null) },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2,
                shape = RoundedCornerShape(14.dp)
            )
            OutlinedTextField(
                value = workAddress,
                onValueChange = { workAddress = it; error = null },
                label = { Text("Work address") },
                leadingIcon = { Icon(Icons.Default.Work, null) },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2,
                shape = RoundedCornerShape(14.dp),
                supportingText = { error?.let { Text(it, color = MaterialTheme.colorScheme.error) } },
                isError = error != null
            )
            Button(
                onClick = {
                    if (homeAddress.isBlank() && workAddress.isBlank()) {
                        error = "Add at least one saved address"
                    } else {
                        onSave(homeAddress, workAddress)
                    }
                },
                modifier = Modifier.fillMaxWidth().height(48.dp),
                enabled = !isSaving,
                shape = RoundedCornerShape(14.dp)
            ) {
                if (isSaving) {
                    CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp, color = MaterialTheme.colorScheme.onPrimary)
                } else {
                    Text("Save Addresses", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun NotificationsDialog(onDismiss: () -> Unit) {
    var bookingUpdates by remember { mutableStateOf(true) }
    var newMessages by remember { mutableStateOf(true) }
    var promotions by remember { mutableStateOf(false) }
    var reminders by remember { mutableStateOf(true) }

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = { Icon(Icons.Default.Notifications, null, tint = Primary) },
        title = { Text("Notifications") },
        text = {
            Column {
                NotifSwitchRow("Booking Updates", bookingUpdates) { bookingUpdates = it }
                NotifSwitchRow("New Messages", newMessages) { newMessages = it }
                NotifSwitchRow("Reminders", reminders) { reminders = it }
                NotifSwitchRow("Promotions & Offers", promotions) { promotions = it }
            }
        },
        confirmButton = {
            Button(onClick = onDismiss) { Text("Save") }
        }
    )
}

@Composable
private fun NotifSwitchRow(label: String, checked: Boolean, onToggle: (Boolean) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium)
        Switch(
            checked = checked,
            onCheckedChange = onToggle,
            colors = SwitchDefaults.colors(checkedTrackColor = Primary)
        )
    }
}

@Composable
private fun ProfileSectionCard(title: String, content: @Composable ColumnScope.() -> Unit) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .shadow(3.dp, RoundedCornerShape(16.dp), spotColor = Primary.copy(0.04f)),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 1.dp,
        border = BorderStroke(1.dp, Border.copy(0.3f))
    ) {
        Column(modifier = Modifier.padding(vertical = 10.dp)) {
            Text(
                title,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                style = MaterialTheme.typography.labelMedium,
                color = Text3,
                fontWeight = FontWeight.Bold
            )
            content()
        }
    }
}

@Composable
private fun ProfileMenuDivider() {
    HorizontalDivider(
        modifier = Modifier.padding(start = 52.dp, end = 16.dp),
        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
    )
}

@Composable
private fun ProfileMenuItem(icon: ImageVector, title: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
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
        Text(title, style = MaterialTheme.typography.bodyMedium, color = Text1, modifier = Modifier.weight(1f))
        Icon(Icons.Default.ChevronRight, null, tint = Text4, modifier = Modifier.size(20.dp))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EditProfileDialog(
    currentName: String,
    currentPhone: String,
    onDismiss: () -> Unit,
    onSave: (String, String) -> Unit
) {
    var name by remember { mutableStateOf(currentName) }
    var phone by remember { mutableStateOf(currentPhone) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Profile") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Full Name") },
                    leadingIcon = { Icon(Icons.Default.Person, null) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Phone Number") },
                    leadingIcon = { Icon(Icons.Default.Phone, null) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
            }
        },
        confirmButton = {
            Button(onClick = { onSave(name, phone) }, enabled = name.isNotBlank()) { Text("Save") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

