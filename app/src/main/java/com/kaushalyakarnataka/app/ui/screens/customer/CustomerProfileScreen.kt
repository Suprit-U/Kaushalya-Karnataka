package com.kaushalyakarnataka.app.ui.screens.customer

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomerProfileScreen(
    onNavigateBottomBar: (NavDestination) -> Unit,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier,
    authViewModel: AuthViewModel = hiltViewModel(),
    profileViewModel: ProfileViewModel = hiltViewModel()
) {
    val themeState = LocalThemeState.current
    val profileState by profileViewModel.profileState.collectAsState()
    var showEditDialog by remember { mutableStateOf(false) }

    val imagePickerLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let { profileViewModel.uploadAvatar(it) }
    }

    Scaffold(
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
            KaushalyaBottomNav(currentDestination = NavDestination.PROFILE, onNavigate = onNavigateBottomBar)
        }
    ) { paddingValues ->
        Column(
            modifier = modifier.fillMaxSize().padding(paddingValues).verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(24.dp))

            // Avatar
            Box(contentAlignment = Alignment.BottomEnd) {
                if (profileState is UiState.Success) {
                    val user = (profileState as UiState.Success).data
                    AvatarComponent(imageUrl = user.avatarUrl, name = user.name, size = 100.dp)
                } else {
                    Box(Modifier.size(100.dp).clip(CircleShape).background(PrimarySubtle))
                }
                Box(
                    modifier = Modifier.size(32.dp).clip(CircleShape).background(Primary).clickable { imagePickerLauncher.launch("image/*") },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.CameraAlt, null, tint = Color.White, modifier = Modifier.size(16.dp))
                }
            }

            Spacer(Modifier.height(12.dp))

            if (profileState is UiState.Success) {
                val user = (profileState as UiState.Success).data
                Text(user.name, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Text(user.email, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(4.dp))
                Surface(shape = RoundedCornerShape(20.dp), color = PrimaryTint) {
                    Text(
                        user.role.name,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall, color = Primary, fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(Modifier.height(28.dp))

            // Settings sections
            ProfileSectionCard(title = "Account") {
                ProfileMenuItem(Icons.Default.Person, "Personal Info") { showEditDialog = true }
                ProfileMenuItem(Icons.Default.LocationOn, "Saved Addresses") {}
                ProfileMenuItem(Icons.Default.Notifications, "Notifications") {}
            }

            Spacer(Modifier.height(12.dp))

            ProfileSectionCard(title = "Preferences") {
                ProfileMenuSwitchItem(
                    icon = if (themeState.isDark) Icons.Default.DarkMode else Icons.Default.LightMode,
                    title = "Dark Mode",
                    checked = themeState.isDark,
                    onToggle = { themeState.toggle() }
                )
                ProfileMenuItem(Icons.Default.Language, "Language") {}
                ProfileMenuItem(Icons.Default.Security, "Privacy & Security") {}
            }

            Spacer(Modifier.height(12.dp))

            ProfileSectionCard(title = "Support") {
                ProfileMenuItem(Icons.Default.HelpOutline, "Help Center") {}
                ProfileMenuItem(Icons.Default.Info, "About Kaushalya") {}
                ProfileMenuItem(Icons.Default.Star, "Rate the App") {}
            }

            Spacer(Modifier.height(16.dp))

            // Logout
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
                    Icon(Icons.Default.Logout, null, tint = Error, modifier = Modifier.size(22.dp))
                    Spacer(Modifier.width(12.dp))
                    Text("Log Out", style = MaterialTheme.typography.bodyMedium, color = Error, fontWeight = FontWeight.SemiBold)
                }
            }

            Spacer(Modifier.height(24.dp))
        }
    }

    if (showEditDialog && profileState is UiState.Success) {
        val user = (profileState as UiState.Success).data
        EditProfileDialog(
            currentName = user.name,
            currentPhone = user.phone,
            onDismiss = { showEditDialog = false },
            onSave = { name, phone ->
                profileViewModel.updateProfile(name, phone)
                showEditDialog = false
            }
        )
    }
}

@Composable
private fun ProfileSectionCard(title: String, content: @Composable ColumnScope.() -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 1.dp
    ) {
        Column(modifier = Modifier.padding(vertical = 8.dp)) {
            Text(
                title,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Bold
            )
            content()
        }
    }
}

@Composable
private fun ProfileMenuItem(icon: ImageVector, title: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick).padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, null, tint = Primary, modifier = Modifier.size(22.dp))
        Spacer(Modifier.width(14.dp))
        Text(title, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
        Icon(Icons.Default.ChevronRight, null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(20.dp))
    }
}

@Composable
private fun ProfileMenuSwitchItem(icon: ImageVector, title: String, checked: Boolean, onToggle: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, null, tint = Primary, modifier = Modifier.size(22.dp))
        Spacer(Modifier.width(14.dp))
        Text(title, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
        Switch(checked = checked, onCheckedChange = { onToggle() }, colors = SwitchDefaults.colors(checkedTrackColor = Primary))
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
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Full Name") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = phone, onValueChange = { phone = it }, label = { Text("Phone Number") }, modifier = Modifier.fillMaxWidth())
            }
        },
        confirmButton = {
            Button(onClick = { onSave(name, phone) }) { Text("Save") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
