package com.kaushalyakarnataka.app.ui.screens.common

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import androidx.hilt.navigation.compose.hiltViewModel
import com.kaushalyakarnataka.app.data.model.AppNotification
import com.kaushalyakarnataka.app.data.model.NotificationType
import com.kaushalyakarnataka.app.ui.components.AppTopBar
import com.kaushalyakarnataka.app.ui.theme.*
import com.kaushalyakarnataka.app.utils.DateUtils
import com.kaushalyakarnataka.app.utils.UiState
import com.kaushalyakarnataka.app.viewmodel.NotificationViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationsScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: NotificationViewModel = hiltViewModel()
) {
    val notifState by viewModel.notifications.collectAsState()

    Scaffold(
        topBar = {
            AppTopBar(
                title = "Notifications",
                onBackClick = onNavigateBack,
                actions = {
                    TextButton(onClick = { viewModel.markAllRead() }) {
                        Text("Mark all read", style = MaterialTheme.typography.labelMedium, color = Primary)
                    }
                }
            )
        }
    ) { paddingValues ->
        when (val state = notifState) {
            is UiState.Loading -> Box(Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Primary)
            }
            is UiState.Success -> {
                if (state.data.isEmpty()) {
                    Box(Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.NotificationsNone, null, tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(0.4f), modifier = Modifier.size(72.dp))
                            Spacer(Modifier.height(12.dp))
                            Text("No notifications yet", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = modifier.fillMaxSize().padding(paddingValues),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(state.data) { notif ->
                            NotificationCard(notif = notif)
                        }
                    }
                }
            }
            is UiState.Error -> Box(Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                Text(state.message, color = MaterialTheme.colorScheme.error)
            }
        }
    }
}

@Composable
private fun NotificationCard(notif: AppNotification) {
    val (icon, iconBg) = when (notif.type) {
        NotificationType.BOOKING_REQUEST -> Icons.Default.Assignment to PrimaryTint
        NotificationType.BOOKING_CONFIRMED -> Icons.Default.CheckCircle to SuccessTint
        NotificationType.BOOKING_DECLINED -> Icons.Default.Cancel to ErrorTint
        NotificationType.BOOKING_COMPLETED -> Icons.Default.EmojiEvents to SuccessTint
        NotificationType.NEW_REVIEW -> Icons.Default.Star to WarningTint
        NotificationType.BOOKING_UPDATE -> Icons.Default.Info to PrimaryTint
    }
    val (iconTint) = when (notif.type) {
        NotificationType.BOOKING_REQUEST -> Primary
        NotificationType.BOOKING_CONFIRMED -> Success
        NotificationType.BOOKING_DECLINED -> Error
        NotificationType.BOOKING_COMPLETED -> Success
        NotificationType.NEW_REVIEW -> Warning
        NotificationType.BOOKING_UPDATE -> Primary
    }.let { listOf(it) }

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = if (notif.isRead) MaterialTheme.colorScheme.surface else PrimaryTint.copy(alpha = 0.4f),
        shadowElevation = if (notif.isRead) 1.dp else 3.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier.size(44.dp).clip(CircleShape).background(iconBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, null, tint = iconTint, modifier = Modifier.size(22.dp))
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(notif.title, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(3.dp))
                Text(notif.message, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(4.dp))
                Text(DateUtils.getRelativeTimeSpan(notif.createdAt), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(0.6f))
            }
            if (!notif.isRead) {
                Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(Primary))
            }
        }
    }
}
