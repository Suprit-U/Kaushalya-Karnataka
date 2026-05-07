package com.kaushalyakarnataka.app.ui.screens.worker

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.kaushalyakarnataka.app.ui.components.AppTopBar
import com.kaushalyakarnataka.app.ui.components.AvatarComponent
import com.kaushalyakarnataka.app.ui.components.KaushalyaBottomNav
import com.kaushalyakarnataka.app.ui.components.NavDestination
import com.kaushalyakarnataka.app.ui.components.VerifiedBadge
import com.kaushalyakarnataka.app.ui.screens.common.LoadingScreen
import com.kaushalyakarnataka.app.ui.theme.LocalThemeState
import com.kaushalyakarnataka.app.ui.theme.Primary
import com.kaushalyakarnataka.app.ui.theme.Warning
import com.kaushalyakarnataka.app.utils.CurrencyUtils
import com.kaushalyakarnataka.app.utils.UiState
import com.kaushalyakarnataka.app.viewmodel.AuthViewModel
import com.kaushalyakarnataka.app.viewmodel.WorkerProfileViewModel

@Composable
fun WorkerSelfProfileScreen(
    onNavigateBottomBar: (NavDestination) -> Unit,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier,
    authViewModel: AuthViewModel = hiltViewModel(),
    viewModel: WorkerProfileViewModel = hiltViewModel()
) {
    LaunchedEffect(Unit) {
        viewModel.loadWorkerProfile()
    }

    val profileState by viewModel.workerState.collectAsState()
    val themeState = LocalThemeState.current

    Scaffold(
        topBar = {
            AppTopBar(title = "Worker Profile")
        },
        bottomBar = {
            KaushalyaBottomNav(
                currentDestination = NavDestination.PROFILE,
                onNavigate = onNavigateBottomBar
            )
        }
    ) { paddingValues ->
        when (val state = profileState) {
            is UiState.Loading -> {
                LoadingScreen(modifier = Modifier.padding(paddingValues))
            }
            is UiState.Error -> {
                Box(
                    modifier = Modifier.fillMaxSize().padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = state.message, color = MaterialTheme.colorScheme.error)
                }
            }
            is UiState.Success -> {
                val worker = state.data

                LazyColumn(
                    modifier = modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            AvatarComponent(
                                imageUrl = worker.avatarUrl,
                                name = worker.name,
                                size = 120.dp
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = worker.name,
                                    style = MaterialTheme.typography.headlineSmall,
                                    fontWeight = FontWeight.Bold
                                )
                                if (worker.isVerified) {
                                    Spacer(modifier = Modifier.width(8.dp))
                                    VerifiedBadge()
                                }
                            }
                            Text(
                                text = worker.category.displayName,
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Spacer(modifier = Modifier.height(24.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                ProfileStatItem(label = "Rating", value = worker.rating.toString(), icon = Icons.Default.Star)
                                ProfileStatItem(label = "Jobs", value = "42", icon = Icons.Default.Work) // Placeholder for now
                                ProfileStatItem(label = "Exp", value = "${worker.experienceYears}y", icon = Icons.Default.History)
                            }
                        }
                    }

                    item {
                        HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp))
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "About Me",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = worker.bio,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    item {
                        Spacer(modifier = Modifier.height(24.dp))
                        Surface(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
                            shape = RoundedCornerShape(14.dp),
                            color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.1f)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth().clickable {
                                    authViewModel.logout()
                                    onLogout()
                                }.padding(horizontal = 16.dp, vertical = 16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Logout, null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(22.dp))
                                Spacer(Modifier.width(12.dp))
                                Text("Log Out", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.SemiBold)
                            }
                        }
                        Spacer(modifier = Modifier.height(32.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun ProfileStatItem(label: String, value: String, icon: ImageVector) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, null, tint = Primary, modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(4.dp))
            Text(text = value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        }
        Text(text = label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}