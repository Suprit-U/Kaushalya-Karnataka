package com.kaushalyakarnataka.app.ui.screens.worker

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.graphics.Brush
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
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Brush.verticalGradient(listOf(Color(0xFF0F2055), PrimaryLight)))
            .padding(top = 20.dp, start = 20.dp, end = 20.dp, bottom = 32.dp)
    ) {
        Column {
            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Column {
                    Text("Worker Dashboard", style = MaterialTheme.typography.bodyMedium, color = Color.White.copy(0.8f))
                    val name = (workerState as? UiState.Success)?.data?.name?.split(" ")?.firstOrNull().orEmpty().ifBlank { "Kaushal" }
                    Text(name, style = MaterialTheme.typography.headlineMedium, color = Color.White, fontWeight = FontWeight.Bold)
                }
                Box(
                    modifier = Modifier.size(40.dp).clip(CircleShape).background(Color.White.copy(0.15f)).clickable(onClick = onNotificationsClick),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Notifications, null, tint = Color.White, modifier = Modifier.size(22.dp))
                    if (unreadCount > 0) {
                        Surface(modifier = Modifier.align(Alignment.TopEnd).size(16.dp), shape = CircleShape, color = Error) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(if (unreadCount > 9) "9+" else unreadCount.toString(), style = MaterialTheme.typography.labelSmall, color = Color.White, fontSize = 8.sp)
                            }
                        }
                    }
                }
            }
            Spacer(Modifier.height(14.dp))
            if (earningsData is UiState.Success) {
                val e = earningsData.data
                Surface(shape = RoundedCornerShape(18.dp), color = Color.White.copy(alpha = 0.12f), modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("This Month's Earnings", style = MaterialTheme.typography.labelMedium, color = Color.White.copy(0.75f))
                        Spacer(Modifier.height(4.dp))
                        Text(CurrencyUtils.formatRupees(e.thisMonthTotal), style = MaterialTheme.typography.displaySmall, color = Color.White, fontWeight = FontWeight.ExtraBold)
                        Spacer(Modifier.height(10.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
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
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(topStart = 22.dp, topEnd = 22.dp),
        color = MaterialTheme.colorScheme.background
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            StatCard(Icons.AutoMirrored.Filled.Assignment, "Requests", (pendingJobs as? UiState.Success)?.data?.count { it.status == BookingStatus.PENDING }?.toString() ?: "-", Error, Modifier.weight(1f))
            StatCard(Icons.Default.EmojiEvents, "Jobs", (earningsData as? UiState.Success)?.data?.completedJobs?.toString() ?: "-", Success, Modifier.weight(1f))
            val rating = (earningsData as? UiState.Success)?.data?.averageRating ?: 0.0
            if (rating > 0) {
                StatCard(Icons.Default.Star, "Rating", String.format("%.1f", rating), Warning, Modifier.weight(1f))
            } else {
                StatCard(Icons.Default.Star, "Rating", "—", Warning.copy(alpha = 0.5f), Modifier.weight(1f))
            }
        }
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
private fun StatCard(icon: ImageVector, label: String, value: String, color: Color, modifier: Modifier = Modifier) {
    Surface(modifier = modifier, shape = RoundedCornerShape(14.dp), color = MaterialTheme.colorScheme.surface, shadowElevation = 1.dp) {
        Column(modifier = Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(icon, null, tint = color, modifier = Modifier.size(22.dp))
            Spacer(Modifier.height(4.dp))
            Text(value, style = MaterialTheme.typography.titleMedium, color = color, fontWeight = FontWeight.ExtraBold, maxLines = 1)
            Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
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

    Surface(modifier = modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), color = MaterialTheme.colorScheme.surface, shadowElevation = 2.dp) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                AvatarComponent(imageUrl = "", name = booking.customerName, size = 44.dp)
                Spacer(Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(booking.customerName, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                    Text(booking.service, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Surface(shape = RoundedCornerShape(8.dp), color = statusBg) {
                    Text(statusLabel, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), style = MaterialTheme.typography.labelSmall, color = statusColor, fontWeight = FontWeight.Bold)
                }
            }
            Spacer(Modifier.height(10.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(0.5f))
            Spacer(Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                InfoChip(Icons.Default.Schedule, booking.timeSlot)
                InfoChip(Icons.Default.LocationOn, booking.address.shortAddress())
            }
            Spacer(Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(booking.dashboardAmountText(), style = MaterialTheme.typography.titleSmall, color = Primary, fontWeight = FontWeight.ExtraBold)
                Text(booking.bookingCode, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }

            when {
                booking.status == BookingStatus.PENDING && onAccept != null && onDecline != null -> {
                    Spacer(Modifier.height(12.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedButton(onClick = onDecline, modifier = Modifier.weight(1f), colors = ButtonDefaults.outlinedButtonColors(contentColor = Error)) {
                            Text("Decline", fontWeight = FontWeight.Bold)
                        }
                        Button(onClick = onAccept, modifier = Modifier.weight(1f), colors = ButtonDefaults.buttonColors(containerColor = Primary)) {
                            Text("Accept", fontWeight = FontWeight.Bold)
                        }
                    }
                }
                booking.status == BookingStatus.CONFIRMED && onSendFinalAmount != null -> {
                    Spacer(Modifier.height(12.dp))
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
                Spacer(Modifier.height(10.dp))
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

    Column(modifier = modifier.fillMaxSize()) {
        ScrollableTabRow(selectedTabIndex = selectedTab, containerColor = MaterialTheme.colorScheme.surface, contentColor = Primary, edgePadding = 16.dp) {
            tabs.forEachIndexed { i, tab -> Tab(selected = selectedTab == i, onClick = { selectedTab = i }, text = { Text(tab, style = MaterialTheme.typography.labelLarge) }) }
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
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.EventBusy, null, tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(0.4f), modifier = Modifier.size(64.dp))
                    Spacer(Modifier.height(10.dp))
                    Text("No ${tabs[selectedTab].lowercase()} bookings", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        } else {
            LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(bookingsToShow) { booking ->
                    EnhancedJobCard(
                        booking = booking,
                        onAccept = if (booking.status == BookingStatus.PENDING) ({ onAccept(booking.id) }) else null,
                        onDecline = if (booking.status == BookingStatus.PENDING) ({ onDecline(booking.id) }) else null,
                        onSendFinalAmount = if (booking.status == BookingStatus.CONFIRMED) ({ amount -> onSendFinalAmount(booking.id, amount) }) else null
                    )
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
private fun InfoChip(icon: ImageVector, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(14.dp))
        Spacer(Modifier.width(4.dp))
        Text(text, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
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
