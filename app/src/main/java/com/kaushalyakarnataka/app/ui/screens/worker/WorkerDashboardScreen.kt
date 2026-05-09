package com.kaushalyakarnataka.app.ui.screens.worker

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.EventAvailable
import androidx.compose.material.icons.filled.EventBusy
import androidx.compose.material.icons.filled.Handyman
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.outlined.NotificationsOff
import androidx.compose.material.icons.filled.Pending
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.kaushalyakarnataka.app.data.model.AppNotification
import com.kaushalyakarnataka.app.data.model.Booking
import com.kaushalyakarnataka.app.data.model.BookingStatus
import com.kaushalyakarnataka.app.data.model.EarningsData
import com.kaushalyakarnataka.app.data.model.NegotiationStatus
import com.kaushalyakarnataka.app.data.model.Review
import com.kaushalyakarnataka.app.data.model.Service
import com.kaushalyakarnataka.app.data.model.Worker
import com.kaushalyakarnataka.app.ui.components.AvatarComponent
import com.kaushalyakarnataka.app.ui.components.NavDestination
import com.kaushalyakarnataka.app.ui.components.WorkerBottomNav
import com.kaushalyakarnataka.app.ui.components.WorkerNavDestination
import com.kaushalyakarnataka.app.ui.theme.Border
import com.kaushalyakarnataka.app.ui.theme.Error
import com.kaushalyakarnataka.app.ui.theme.ErrorTint
import com.kaushalyakarnataka.app.ui.theme.Primary
import com.kaushalyakarnataka.app.ui.theme.PrimaryLight
import com.kaushalyakarnataka.app.ui.theme.PrimaryTint
import com.kaushalyakarnataka.app.ui.theme.Secondary
import com.kaushalyakarnataka.app.ui.theme.SecondaryDark
import com.kaushalyakarnataka.app.ui.theme.SecondaryTint
import com.kaushalyakarnataka.app.ui.theme.Success
import com.kaushalyakarnataka.app.ui.theme.SuccessTint
import com.kaushalyakarnataka.app.ui.theme.Text1
import com.kaushalyakarnataka.app.ui.theme.Text3
import com.kaushalyakarnataka.app.ui.theme.Text4
import com.kaushalyakarnataka.app.ui.theme.Warning
import com.kaushalyakarnataka.app.ui.theme.WarningTint
import com.kaushalyakarnataka.app.utils.CurrencyUtils
import com.kaushalyakarnataka.app.utils.DateUtils
import com.kaushalyakarnataka.app.utils.UiState
import com.kaushalyakarnataka.app.viewmodel.NotificationViewModel
import com.kaushalyakarnataka.app.viewmodel.WorkerDashboardViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkerDashboardScreen(
    onNavigateToAddService: () -> Unit,
    onNavigateToPortfolio: () -> Unit,
    onNavigateBottomBar: (NavDestination) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: WorkerDashboardViewModel = hiltViewModel(),
    notifViewModel: NotificationViewModel = hiltViewModel()
) {
    val workerState by viewModel.workerState.collectAsState()
    val pendingJobs by viewModel.pendingJobs.collectAsState()
    val upcomingJobs by viewModel.upcomingJobs.collectAsState()
    val allBookings by viewModel.allBookings.collectAsState()
    val earningsData by viewModel.earningsData.collectAsState()
    val servicesState by viewModel.servicesState.collectAsState()
    val recentReviews by viewModel.recentReviews.collectAsState()
    val unreadCount by notifViewModel.unreadCount.collectAsState()
    val notifications by notifViewModel.notifications.collectAsState()
    var currentWorkerTab by remember { mutableStateOf(WorkerNavDestination.DASHBOARD) }
    var showNotifications by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    LaunchedEffect(currentWorkerTab) {
        if (currentWorkerTab == WorkerNavDestination.SERVICES) viewModel.refreshServices()
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            WorkerBottomNav(
                currentDestination = currentWorkerTab,
                onNavigate = { dest ->
                    currentWorkerTab = dest
                    if (dest == WorkerNavDestination.PROFILE) {
                        onNavigateBottomBar(NavDestination.PROFILE)
                    }
                }
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        when (currentWorkerTab) {
            WorkerNavDestination.DASHBOARD -> DashboardTab(
                workerState = workerState,
                pendingJobs = pendingJobs,
                earningsData = earningsData,
                recentReviews = recentReviews,
                unreadCount = unreadCount,
                onAccept = { id -> viewModel.acceptJob(id); scope.launch { snackbarHostState.showSnackbar("Booking accepted") } },
                onDecline = { id -> viewModel.declineJob(id); scope.launch { snackbarHostState.showSnackbar("Booking declined") } },
                onSendFinalAmount = { id, amount -> viewModel.workerSendFinalAmount(id, amount); scope.launch { snackbarHostState.showSnackbar("Final amount sent to customer") } },
                onNotificationsClick = { showNotifications = true },
                modifier = modifier.padding(paddingValues)
            )
            WorkerNavDestination.SERVICES -> ServicesTab(
                servicesState = servicesState,
                onAddService = { onNavigateToAddService(); currentWorkerTab = WorkerNavDestination.DASHBOARD },
                onPortfolio = onNavigateToPortfolio,
                onDeleteService = { viewModel.deleteService(it) },
                modifier = modifier.padding(paddingValues)
            )
            WorkerNavDestination.BOOKINGS -> BookingsTab(
                pendingJobs = pendingJobs,
                upcomingJobs = upcomingJobs,
                allBookings = allBookings,
                onAccept = { id -> viewModel.acceptJob(id); scope.launch { snackbarHostState.showSnackbar("Booking accepted") } },
                onDecline = { id -> viewModel.declineJob(id); scope.launch { snackbarHostState.showSnackbar("Booking declined") } },
                onSendFinalAmount = { id, amount -> viewModel.workerSendFinalAmount(id, amount); scope.launch { snackbarHostState.showSnackbar("Final amount sent to customer") } },
                modifier = modifier.padding(paddingValues)
            )
            WorkerNavDestination.PROFILE -> Box(Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Primary)
            }
        }
    }

    if (showNotifications) {
        DashboardNotificationsDialog(
            notificationsState = notifications,
            onDismiss = { showNotifications = false },
            onMarkAllRead = { notifViewModel.markAllRead() }
        )
    }
}

@Composable
private fun DashboardTab(
    workerState: UiState<Worker>,
    pendingJobs: UiState<List<Booking>>,
    earningsData: UiState<EarningsData>,
    recentReviews: UiState<List<Review>>,
    unreadCount: Int,
    onAccept: (String) -> Unit,
    onDecline: (String) -> Unit,
    onSendFinalAmount: (String, Int) -> Unit,
    onNotificationsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(modifier = modifier.fillMaxSize(), contentPadding = PaddingValues(bottom = 16.dp)) {
        item {
            DashboardHeader(
                workerState = workerState,
                earningsData = earningsData,
                unreadCount = unreadCount,
                onNotificationsClick = onNotificationsClick
            )
        }
        item {
            StatsStrip(pendingJobs = pendingJobs, earningsData = earningsData)
        }
        val pending = (pendingJobs as? UiState.Success)?.data?.filter { it.status == BookingStatus.PENDING } ?: emptyList()
        if (pending.isNotEmpty()) {
            item {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("New Requests", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Surface(shape = RoundedCornerShape(20.dp), color = ErrorTint) {
                        Text("${pending.size} new", modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp), style = MaterialTheme.typography.labelSmall, color = Error, fontWeight = FontWeight.Bold)
                    }
                }
            }
            items(pending) { booking ->
                EnhancedJobCard(
                    booking = booking,
                    onAccept = { onAccept(booking.id) },
                    onDecline = { onDecline(booking.id) },
                    onSendFinalAmount = { amount -> onSendFinalAmount(booking.id, amount) },
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                )
            }
        } else if (pendingJobs is UiState.Loading) {
            item { Box(Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = Primary) } }
        } else {
            item {
                Surface(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp), shape = RoundedCornerShape(14.dp), color = MaterialTheme.colorScheme.surface) {
                    Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Icon(Icons.Default.EventAvailable, null, tint = Success, modifier = Modifier.size(28.dp))
                        Text("No pending requests right now.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
        // Recent Reviews Section
        item {
            RecentReviewsSection(recentReviews = recentReviews)
        }
    }
}

@Composable
private fun DashboardHeader(
    workerState: UiState<Worker>,
    earningsData: UiState<EarningsData>,
    unreadCount: Int,
    onNotificationsClick: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "dash_orbs")
    val orb1 by infiniteTransition.animateFloat(
        initialValue = -6f, targetValue = 6f,
        animationSpec = infiniteRepeatable(tween(5000, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "dash_orb1"
    )
    val orb2 by infiniteTransition.animateFloat(
        initialValue = 5f, targetValue = -5f,
        animationSpec = infiniteRepeatable(tween(4000, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "dash_orb2"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFF0F2055), Primary, PrimaryLight)
                ),
                RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp)
            )
            .padding(top = 24.dp, start = 20.dp, end = 20.dp, bottom = 36.dp)
    ) {
        // Floating orbs
        Box(
            modifier = Modifier
                .size(80.dp)
                .offset(x = (-10).dp, y = (20 + orb1).dp)
                .clip(CircleShape)
                .background(Color.White.copy(0.05f))
                .align(Alignment.TopStart)
        )
        Box(
            modifier = Modifier
                .size(50.dp)
                .offset(x = (10).dp, y = (80 + orb2).dp)
                .clip(CircleShape)
                .background(Color.White.copy(0.04f))
                .align(Alignment.TopEnd)
        )

        Column {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    val waveOffset by rememberInfiniteTransition(label = "dash_wave").animateFloat(
                        initialValue = -2f, targetValue = 2f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(1800, easing = FastOutSlowInEasing),
                            repeatMode = RepeatMode.Reverse
                        ), label = "dash_wave"
                    )
                    Text(
                        "Worker Dashboard",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(0.75f)
                    )
                    val name = (workerState as? UiState.Success)?.data?.name?.split(" ")?.firstOrNull().orEmpty().ifBlank { "Kaushal" }
                    Text(
                        "Hi, $name",
                        style = MaterialTheme.typography.headlineMedium,
                        color = Color.White,
                        fontWeight = FontWeight.ExtraBold,
                        modifier = Modifier.graphicsLayer { translationY = waveOffset }
                    )
                }
                Surface(
                    onClick = onNotificationsClick,
                    modifier = Modifier.size(44.dp),
                    shape = CircleShape,
                    color = Color.White.copy(0.12f),
                    shadowElevation = 0.dp
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.Notifications, null, tint = Color.White, modifier = Modifier.size(22.dp))
                        if (unreadCount > 0) {
                            val pulse by rememberInfiniteTransition(label = "dash_pulse").animateFloat(
                                initialValue = 0.7f, targetValue = 1.15f,
                                animationSpec = infiniteRepeatable(
                                    animation = tween(800, easing = FastOutSlowInEasing),
                                    repeatMode = RepeatMode.Reverse
                                ), label = "dash_pulse"
                            )
                            Box(
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .offset((-2).dp, 2.dp)
                                    .size(18.dp)
                                    .graphicsLayer { scaleX = pulse; scaleY = pulse }
                                    .clip(CircleShape)
                                    .background(Error),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    if (unreadCount > 9) "9+" else unreadCount.toString(),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.White,
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
            Spacer(Modifier.height(16.dp))
            if (earningsData is UiState.Success) {
                val e = earningsData.data
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = Color.White.copy(alpha = 0.1f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Text(
                            "This Month's Earnings",
                            style = MaterialTheme.typography.labelMedium,
                            color = Color.White.copy(0.75f)
                        )
                        Spacer(Modifier.height(6.dp))
                        Text(
                            CurrencyUtils.formatRupees(e.thisMonthTotal),
                            style = MaterialTheme.typography.displaySmall,
                            color = Color.White,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Spacer(Modifier.height(12.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            EarningChip(Icons.Default.CheckCircle, "${e.completedJobs} Done")
                            EarningChip(Icons.Default.Pending, "${e.pendingJobs} Pending")
                            if (e.averageRating > 0) {
                                EarningChip(Icons.Default.Star, String.format("%.1f", e.averageRating))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StatsStrip(
    pendingJobs: UiState<List<Booking>>,
    earningsData: UiState<EarningsData>
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .offset(y = (-16).dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        val requests = (pendingJobs as? UiState.Success)?.data?.count { it.status == BookingStatus.PENDING }?.toString() ?: "0"
        val jobs = (earningsData as? UiState.Success)?.data?.completedJobs?.toString() ?: "0"
        val rating = (earningsData as? UiState.Success)?.data?.averageRating ?: 0.0
        val ratingText = if (rating > 0) String.format("%.1f", rating) else "—"

        PremiumStatCard(
            icon = Icons.AutoMirrored.Filled.Assignment,
            label = "Requests",
            value = requests,
            gradient = Brush.linearGradient(listOf(ErrorTint, Error.copy(0.08f))),
            iconTint = Error,
            modifier = Modifier.weight(1f)
        )
        PremiumStatCard(
            icon = Icons.Default.EmojiEvents,
            label = "Jobs Done",
            value = jobs,
            gradient = Brush.linearGradient(listOf(SuccessTint, Success.copy(0.08f))),
            iconTint = Success,
            modifier = Modifier.weight(1f)
        )
        PremiumStatCard(
            icon = Icons.Default.Star,
            label = "Rating",
            value = ratingText,
            gradient = Brush.linearGradient(listOf(WarningTint, Warning.copy(0.08f))),
            iconTint = if (rating > 0) Warning else Text4,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun EarningChip(icon: ImageVector, text: String) {
    Surface(shape = RoundedCornerShape(20.dp), color = Color.White.copy(0.15f)) {
        Row(modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            Icon(icon, null, tint = Color.White, modifier = Modifier.size(12.dp))
            Text(text, style = MaterialTheme.typography.labelSmall, color = Color.White)
        }
    }
}

@Composable
private fun PremiumStatCard(
    icon: ImageVector,
    label: String,
    value: String,
    gradient: Brush,
    iconTint: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .shadow(4.dp, RoundedCornerShape(16.dp), spotColor = iconTint.copy(0.1f)),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 0.dp,
        tonalElevation = 1.dp
    ) {
        Column(
            modifier = Modifier
                .background(gradient)
                .padding(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(iconTint.copy(0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, null, tint = iconTint, modifier = Modifier.size(20.dp))
            }
            Spacer(Modifier.height(8.dp))
            Text(
                value,
                style = MaterialTheme.typography.titleLarge,
                color = Text1,
                fontWeight = FontWeight.ExtraBold,
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

@Composable
fun EnhancedJobCard(
    booking: Booking,
    onAccept: (() -> Unit)? = null,
    onDecline: (() -> Unit)? = null,
    onSendFinalAmount: ((Int) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val (statusColor, statusBg, statusLabel) = when (booking.status) {
        BookingStatus.PENDING -> Triple(Warning, WarningTint, "New Request")
        BookingStatus.CONFIRMED -> Triple(Primary, PrimaryTint, "Confirmed")
        BookingStatus.IN_PROGRESS -> Triple(Success, SuccessTint, "In Progress")
        BookingStatus.COMPLETED -> Triple(Success, SuccessTint, "Completed")
        BookingStatus.CANCELLED -> Triple(Error, ErrorTint, "Declined")
        BookingStatus.AWAITING_PAYMENT_CONFIRMATION -> Triple(Secondary, SecondaryTint, "Awaiting Customer")
    }

    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.98f else 1f,
        animationSpec = tween(120),
        label = "job_card_scale"
    )

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .scale(scale)
            .shadow(4.dp, RoundedCornerShape(20.dp), spotColor = statusColor.copy(0.08f))
            .clip(RoundedCornerShape(20.dp))
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = {}
            ),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 1.dp,
        border = BorderStroke(1.dp, statusColor.copy(0.08f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(statusBg),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        booking.customerName.take(1).uppercase(),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = statusColor
                    )
                }
                Spacer(Modifier.width(14.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        booking.customerName,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = Text1
                    )
                    Text(
                        booking.service,
                        style = MaterialTheme.typography.bodySmall,
                        color = Text3
                    )
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(statusBg)
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                ) {
                    Text(
                        statusLabel,
                        style = MaterialTheme.typography.labelSmall,
                        color = statusColor,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            Spacer(Modifier.height(14.dp))
            HorizontalDivider(color = Border.copy(0.4f))
            Spacer(Modifier.height(14.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                JobInfoChip(Icons.Default.Schedule, booking.timeSlot)
                JobInfoChip(Icons.Default.LocationOn, booking.address.shortAddress())
            }
            Spacer(Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(PrimaryTint)
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        booking.dashboardAmountText(),
                        style = MaterialTheme.typography.labelLarge,
                        color = Primary,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
                Text(
                    booking.bookingCode,
                    style = MaterialTheme.typography.labelSmall,
                    color = Text4
                )
            }

            when {
                booking.status == BookingStatus.PENDING && onAccept != null && onDecline != null -> {
                    Spacer(Modifier.height(14.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(42.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .border(1.5.dp, Error.copy(0.3f), RoundedCornerShape(12.dp))
                                .clickable(onClick = onDecline),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("Decline", fontWeight = FontWeight.SemiBold, color = Error)
                        }
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(42.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Brush.horizontalGradient(listOf(Primary, PrimaryLight)))
                                .clickable(onClick = onAccept),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("Accept", fontWeight = FontWeight.SemiBold, color = Color.White)
                        }
                    }
                }
                booking.status == BookingStatus.CONFIRMED && onSendFinalAmount != null -> {
                    Spacer(Modifier.height(14.dp))
                    WorkerSendFinalAmountCard(
                        booking = booking,
                        onSendFinalAmount = onSendFinalAmount
                    )
                }
                booking.status == BookingStatus.AWAITING_PAYMENT_CONFIRMATION &&
                    (booking.negotiationStatus == NegotiationStatus.WORKER_PROPOSED || booking.negotiationStatus == NegotiationStatus.WORKER_COUNTERED) -> {
                    Spacer(Modifier.height(12.dp))
                    StatusMessage(
                        icon = Icons.Default.HourglassTop,
                        text = "Awaiting customer approval for ${CurrencyUtils.formatRupees(booking.counterAmount())}",
                        color = SecondaryDark,
                        background = SecondaryTint
                    )
                }
                booking.status == BookingStatus.AWAITING_PAYMENT_CONFIRMATION &&
                    booking.negotiationStatus == NegotiationStatus.CUSTOMER_PROPOSED -> {
                    Spacer(Modifier.height(12.dp))
                    StatusMessage(
                        icon = Icons.Default.HourglassTop,
                        text = "Customer proposed ${CurrencyUtils.formatRupees(booking.customerProposedAmount)}. Awaiting your action.",
                        color = SecondaryDark,
                        background = SecondaryTint
                    )
                }
            }

            if (booking.status == BookingStatus.COMPLETED && booking.finalAmount > 0) {
                Spacer(Modifier.height(12.dp))
                StatusMessage(
                    icon = Icons.Default.Payments,
                    text = "Paid ${CurrencyUtils.formatRupees(booking.finalAmount)}. Initial estimate ${booking.initialAmountText()}.",
                    color = Success,
                    background = SuccessTint
                )
            }
        }
    }
}

@Composable
private fun WorkerSendFinalAmountCard(
    booking: Booking,
    onSendFinalAmount: (Int) -> Unit
) {
    var showDialog by remember { mutableStateOf(false) }
    if (showDialog) {
        var amountText by remember { mutableStateOf(booking.estimatedCostMax.takeIf { it > 0 }?.toString() ?: "") }
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Send Final Amount") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        "Service: ${booking.service}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        "Initial estimate: ${booking.initialAmountText()}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    OutlinedTextField(
                        value = amountText,
                        onValueChange = { amountText = it.filter { c -> c.isDigit() } },
                        label = { Text("Final amount") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        prefix = { Text("Rs. ") }
                    )
                }
            },
            confirmButton = {
                Button(onClick = {
                    val amount = amountText.toIntOrNull() ?: 0
                    if (amount > 0) {
                        onSendFinalAmount(amount)
                        showDialog = false
                    }
                }) { Text("Send to Customer") }
            },
            dismissButton = { TextButton(onClick = { showDialog = false }) { Text("Cancel") } }
        )
    }

    Button(
        onClick = { showDialog = true },
        modifier = Modifier.fillMaxWidth(),
        colors = ButtonDefaults.buttonColors(containerColor = Primary)
    ) {
        Icon(Icons.Default.Payments, null, modifier = Modifier.size(18.dp))
        Spacer(Modifier.width(8.dp))
        Text("Send Final Amount", fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun RecentReviewsSection(
    recentReviews: UiState<List<Review>>
) {
    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
        Text("Recent Reviews", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(10.dp))
        when (recentReviews) {
            is UiState.Loading -> Box(Modifier.fillMaxWidth().height(60.dp), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Primary, modifier = Modifier.size(24.dp))
            }
            is UiState.Error -> Text(recentReviews.message, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
            is UiState.Success -> {
                val reviews = recentReviews.data.take(3)
                if (reviews.isEmpty()) {
                    Surface(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)) {
                        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(Icons.Default.Star, null, tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f), modifier = Modifier.size(20.dp))
                            Text("No reviews yet. Complete jobs to get rated.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f))
                        }
                    }
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        reviews.forEach { review ->
                            CompactReviewCard(review = review)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CompactReviewCard(review: Review) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 1.dp
    ) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.Top, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            AvatarComponent(imageUrl = review.customerAvatarUrl, name = review.customerName, size = 36.dp)
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(review.customerName, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.width(8.dp))
                    Surface(shape = RoundedCornerShape(6.dp), color = WarningTint) {
                        Row(modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                            Icon(Icons.Default.Star, null, tint = Warning, modifier = Modifier.size(10.dp))
                            Text("${review.rating}", style = MaterialTheme.typography.labelSmall, color = Warning, fontWeight = FontWeight.Bold)
                        }
                    }
                }
                Spacer(Modifier.height(2.dp))
                Text(review.comment, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 2)
            }
        }
    }
}

@Composable
private fun StatusMessage(icon: ImageVector, text: String, color: Color, background: Color) {
    Surface(shape = RoundedCornerShape(8.dp), color = background, modifier = Modifier.fillMaxWidth()) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Icon(icon, null, tint = color, modifier = Modifier.size(18.dp))
            Text(text, style = MaterialTheme.typography.bodySmall, color = color, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
private fun ServicesTab(
    servicesState: UiState<List<Service>>,
    onAddService: () -> Unit,
    onPortfolio: () -> Unit,
    onDeleteService: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(modifier = modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item { Text("Manage Services", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold) }
        item { ActionCard(Icons.Default.Add, "Add New Service", "Set individual service pricing", Primary, PrimaryTint, onAddService) }
        item { ActionCard(Icons.Default.PhotoLibrary, "Upload Portfolio", "Show customers your best work", Secondary, MaterialTheme.colorScheme.surface, onPortfolio) }

        when (servicesState) {
            is UiState.Loading -> item { Box(Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = Primary) } }
            is UiState.Success -> {
                if (servicesState.data.isEmpty()) {
                    item {
                        Surface(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp), color = MaterialTheme.colorScheme.surfaceVariant) {
                            Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                Icon(Icons.Default.Handyman, null, tint = Primary.copy(0.6f), modifier = Modifier.size(32.dp))
                                Column {
                                    Text("No services added yet", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text("Add services like switch repair or wall painting.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(0.7f))
                                }
                            }
                        }
                    }
                } else {
                    item { Text("Your Services (${servicesState.data.size})", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold) }
                    items(servicesState.data) { service -> ServiceManageCard(service = service, onDelete = { onDeleteService(service.id) }) }
                }
            }
            is UiState.Error -> item { Text(servicesState.message, color = MaterialTheme.colorScheme.error) }
        }
    }
}

@Composable
private fun ActionCard(icon: ImageVector, title: String, subtitle: String, color: Color, background: Color, onClick: () -> Unit) {
    Surface(modifier = Modifier.fillMaxWidth().clickable(onClick = onClick), shape = RoundedCornerShape(14.dp), color = background, shadowElevation = 1.dp) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            Box(Modifier.size(46.dp).clip(RoundedCornerShape(12.dp)).background(color), contentAlignment = Alignment.Center) {
                Icon(icon, null, tint = Color.White, modifier = Modifier.size(24.dp))
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = if (background == PrimaryTint) Primary else MaterialTheme.colorScheme.onSurface)
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Icon(Icons.Default.ChevronRight, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun ServiceManageCard(service: Service, onDelete: () -> Unit) {
    var showDeleteDialog by remember { mutableStateOf(false) }
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Service?") },
            text = { Text("Delete \"${service.name}\" from your active services?") },
            confirmButton = { Button(onClick = { onDelete(); showDeleteDialog = false }, colors = ButtonDefaults.buttonColors(containerColor = Error)) { Text("Delete") } },
            dismissButton = { TextButton(onClick = { showDeleteDialog = false }) { Text("Cancel") } }
        )
    }

    Surface(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp), color = MaterialTheme.colorScheme.surface, shadowElevation = 1.dp) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(service.name, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                if (service.description.isNotBlank()) {
                    Spacer(Modifier.height(2.dp))
                    Text(service.description.shortAddress(70), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Spacer(Modifier.height(6.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Surface(shape = RoundedCornerShape(8.dp), color = PrimaryTint) {
                        Text(CurrencyUtils.formatRupees(service.startingPrice), modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp), style = MaterialTheme.typography.labelSmall, color = Primary, fontWeight = FontWeight.Bold)
                    }
                    Surface(shape = RoundedCornerShape(8.dp), color = MaterialTheme.colorScheme.surfaceVariant) {
                        Text(service.pricingType.displayLabel, modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Surface(shape = RoundedCornerShape(8.dp), color = MaterialTheme.colorScheme.surfaceVariant) {
                        Text(service.estimatedDuration.displayLabel, modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
            IconButton(onClick = { showDeleteDialog = true }) {
                Icon(Icons.Default.DeleteOutline, null, tint = Error)
            }
        }
    }
}

@Composable
private fun BookingsTab(
    pendingJobs: UiState<List<Booking>>,
    upcomingJobs: UiState<List<Booking>>,
    allBookings: UiState<List<Booking>>,
    onAccept: (String) -> Unit,
    onDecline: (String) -> Unit,
    onSendFinalAmount: (String, Int) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("Pending", "Active", "Completed", "All")
    val tabCounts = listOf(
        (pendingJobs as? UiState.Success)?.data?.count { it.status == BookingStatus.PENDING } ?: 0,
        (allBookings as? UiState.Success)?.data?.count { it.status == BookingStatus.CONFIRMED || it.status == BookingStatus.AWAITING_PAYMENT_CONFIRMATION || it.status == BookingStatus.IN_PROGRESS } ?: 0,
        (allBookings as? UiState.Success)?.data?.count { it.status == BookingStatus.COMPLETED } ?: 0,
        (allBookings as? UiState.Success)?.data?.size ?: 0
    )

    Column(modifier = modifier.fillMaxSize()) {
        // Premium tab row
        ScrollableTabRow(
            selectedTabIndex = selectedTab,
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = Primary,
            edgePadding = 16.dp
        ) {
            tabs.forEachIndexed { i, tab ->
                Tab(
                    selected = selectedTab == i,
                    onClick = { selectedTab = i },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(tab, style = MaterialTheme.typography.labelLarge, fontWeight = if (selectedTab == i) FontWeight.Bold else FontWeight.Medium)
                            if (tabCounts[i] > 0) {
                                Spacer(Modifier.width(6.dp))
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (selectedTab == i) Primary else Primary.copy(0.1f))
                                        .padding(horizontal = 6.dp, vertical = 1.dp)
                                ) {
                                    Text(
                                        "${tabCounts[i]}",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = if (selectedTab == i) Color.White else Primary
                                    )
                                }
                            }
                        }
                    }
                )
            }
        }

        val all = (allBookings as? UiState.Success)?.data ?: emptyList()
        val bookingsToShow = when (selectedTab) {
            0 -> (pendingJobs as? UiState.Success)?.data?.filter { it.status == BookingStatus.PENDING } ?: emptyList()
            1 -> all.filter { it.status == BookingStatus.CONFIRMED || it.status == BookingStatus.AWAITING_PAYMENT_CONFIRMATION || it.status == BookingStatus.IN_PROGRESS }
            2 -> all.filter { it.status == BookingStatus.COMPLETED }
            else -> all
        }

        val isLoading = pendingJobs is UiState.Loading || upcomingJobs is UiState.Loading
        if (isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = Primary) }
        } else if (bookingsToShow.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .size(100.dp)
                            .clip(RoundedCornerShape(30.dp))
                            .background(Primary.copy(0.06f))
                            .border(1.dp, Primary.copy(0.1f), RoundedCornerShape(30.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.EventBusy, null, tint = Primary.copy(0.35f), modifier = Modifier.size(44.dp))
                    }
                    Spacer(Modifier.height(24.dp))
                    Text(
                        "No ${tabs[selectedTab].lowercase()} bookings",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Text1
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "Jobs in this category will appear here",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Text3
                    )
                }
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(0.dp)
            ) {
                items(bookingsToShow, key = { it.id }) { booking ->
                    val isFirst = bookingsToShow.firstOrNull()?.id == booking.id
                    val isLast = bookingsToShow.lastOrNull()?.id == booking.id
                    val statusColor = when (booking.status) {
                        BookingStatus.PENDING -> Warning
                        BookingStatus.CONFIRMED, BookingStatus.IN_PROGRESS -> Primary
                        BookingStatus.COMPLETED -> Success
                        BookingStatus.CANCELLED -> Error
                        BookingStatus.AWAITING_PAYMENT_CONFIRMATION -> Secondary
                    }

                    Row(modifier = Modifier.fillMaxWidth()) {
                        // Timeline column
                        Column(
                            modifier = Modifier.width(28.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            if (!isFirst) {
                                Box(
                                    modifier = Modifier
                                        .width(2.dp)
                                        .height(16.dp)
                                        .background(statusColor.copy(0.3f))
                                )
                            } else {
                                Spacer(Modifier.height(16.dp))
                            }
                            Box(
                                modifier = Modifier
                                    .size(12.dp)
                                    .clip(CircleShape)
                                    .background(statusColor)
                                    .border(2.dp, MaterialTheme.colorScheme.surface, CircleShape)
                            )
                            if (!isLast) {
                                Box(
                                    modifier = Modifier
                                        .width(2.dp)
                                        .height(16.dp)
                                        .background(statusColor.copy(0.3f))
                                )
                            } else {
                                Spacer(Modifier.height(16.dp))
                            }
                        }
                        Spacer(Modifier.width(10.dp))
                        EnhancedJobCard(
                            booking = booking,
                            onAccept = if (booking.status == BookingStatus.PENDING) ({ onAccept(booking.id) }) else null,
                            onDecline = if (booking.status == BookingStatus.PENDING) ({ onDecline(booking.id) }) else null,
                            onSendFinalAmount = if (booking.status == BookingStatus.CONFIRMED) ({ amount -> onSendFinalAmount(booking.id, amount) }) else null,
                            modifier = Modifier.weight(1f).padding(bottom = 14.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DashboardNotificationsDialog(
    notificationsState: UiState<List<AppNotification>>,
    onDismiss: () -> Unit,
    onMarkAllRead: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Notifications") },
        text = {
            when (notificationsState) {
                is UiState.Loading -> Box(Modifier.fillMaxWidth().height(80.dp), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = Primary) }
                is UiState.Error -> Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp)) {
                    Icon(Icons.Outlined.NotificationsOff, null, tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(0.4f), modifier = Modifier.size(40.dp))
                    Spacer(Modifier.height(8.dp))
                    Text("Could not load notifications", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                is UiState.Success -> {
                    if (notificationsState.data.isEmpty()) {
                        Text("No notifications yet", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            notificationsState.data.take(5).forEach { notification ->
                                NotificationPreviewRow(notification)
                            }
                        }
                    }
                }
            }
        },
        confirmButton = { Button(onClick = { onMarkAllRead(); onDismiss() }) { Text("Mark Read") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Close") } }
    )
}

@Composable
private fun NotificationPreviewRow(notification: AppNotification) {
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        Box(Modifier.size(32.dp).clip(CircleShape).background(if (notification.isRead) MaterialTheme.colorScheme.surfaceVariant else PrimaryTint), contentAlignment = Alignment.Center) {
            Icon(if (notification.isRead) Icons.Default.Info else Icons.Default.Notifications, null, tint = Primary, modifier = Modifier.size(16.dp))
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(notification.title, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
            Text(notification.message, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 2)
            Text(DateUtils.getRelativeTimeSpan(notification.createdAt), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(0.7f))
        }
    }
}

@Composable
private fun JobInfoChip(icon: ImageVector, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(26.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Primary.copy(0.06f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, null, tint = Primary.copy(0.7f), modifier = Modifier.size(14.dp))
        }
        Spacer(Modifier.width(8.dp))
        Text(text, style = MaterialTheme.typography.bodySmall, color = Text3)
    }
}

private fun Booking.initialAmountText(): String {
    return when {
        estimatedCostMin > 0 && estimatedCostMax > estimatedCostMin -> "${CurrencyUtils.formatRupees(estimatedCostMin)} - ${CurrencyUtils.formatRupees(estimatedCostMax)}"
        estimatedCostMin > 0 -> CurrencyUtils.formatRupees(estimatedCostMin)
        estimatedCostMax > 0 -> CurrencyUtils.formatRupees(estimatedCostMax)
        else -> "Amount pending"
    }
}

private fun Booking.dashboardAmountText(): String {
    return when {
        finalAmount > 0 -> "Paid: ${CurrencyUtils.formatRupees(finalAmount)}"
        customerProposedAmount > 0 && negotiationStatus == NegotiationStatus.CUSTOMER_PROPOSED -> "Requested: ${CurrencyUtils.formatRupees(customerProposedAmount)}"
        counterAmount() > 0 -> "Revised: ${CurrencyUtils.formatRupees(counterAmount())}"
        else -> initialAmountText()
    }
}

private fun Booking.counterAmount(): Int {
    return if (workerCounterAmount > 0) workerCounterAmount else negotiatedAmount
}

private fun String.shortAddress(max: Int = 25): String {
    return if (length > max) "${take(max)}..." else this
}
