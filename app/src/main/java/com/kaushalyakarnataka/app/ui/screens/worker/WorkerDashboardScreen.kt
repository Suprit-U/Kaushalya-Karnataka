package com.kaushalyakarnataka.app.ui.screens.worker

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.kaushalyakarnataka.app.data.model.Booking
import com.kaushalyakarnataka.app.data.model.BookingStatus
import com.kaushalyakarnataka.app.data.model.Service
import com.kaushalyakarnataka.app.ui.components.*
import com.kaushalyakarnataka.app.ui.theme.*
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
    val unreadCount by notifViewModel.unreadCount.collectAsState()
    var currentWorkerTab by remember { mutableStateOf(WorkerNavDestination.DASHBOARD) }
    var showNotifications by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    // Refresh services when returning to services tab
    LaunchedEffect(currentWorkerTab) {
        if (currentWorkerTab == WorkerNavDestination.SERVICES) {
            viewModel.refreshServices()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            WorkerBottomNav(
                currentDestination = currentWorkerTab,
                onNavigate = { dest ->
                    currentWorkerTab = dest
                    when (dest) {
                        WorkerNavDestination.PROFILE -> onNavigateBottomBar(NavDestination.PROFILE)
                        else -> {}
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
                unreadCount = unreadCount,
                onAccept = { id -> viewModel.acceptJob(id); scope.launch { snackbarHostState.showSnackbar("Booking accepted") } },
                onDecline = { id -> viewModel.declineJob(id); scope.launch { snackbarHostState.showSnackbar("Booking declined") } },
                onMarkComplete = { id -> viewModel.markComplete(id); scope.launch { snackbarHostState.showSnackbar("Marked as completed") } },
                onProposeAmount = { id, amount -> viewModel.proposeNegotiatedAmount(id, amount); scope.launch { snackbarHostState.showSnackbar("Final amount proposed") } },
                onNotificationsClick = { showNotifications = true },
                modifier = Modifier.padding(paddingValues)
            )
            WorkerNavDestination.SERVICES -> ServicesTab(
                servicesState = servicesState,
                onAddService = { onNavigateToAddService(); currentWorkerTab = WorkerNavDestination.DASHBOARD },
                onPortfolio = onNavigateToPortfolio,
                onDeleteService = { viewModel.deleteService(it) },
                modifier = Modifier.padding(paddingValues)
            )
            WorkerNavDestination.BOOKINGS -> BookingsTab(
                pendingJobs = pendingJobs,
                upcomingJobs = upcomingJobs,
                allBookings = allBookings,
                onAccept = { id -> viewModel.acceptJob(id); scope.launch { snackbarHostState.showSnackbar("Booking accepted") } },
                onDecline = { id -> viewModel.declineJob(id); scope.launch { snackbarHostState.showSnackbar("Booking declined") } },
                onMarkComplete = { id -> viewModel.markComplete(id); scope.launch { snackbarHostState.showSnackbar("Marked as completed") } },
                onProposeAmount = { id, amount -> viewModel.proposeNegotiatedAmount(id, amount); scope.launch { snackbarHostState.showSnackbar("Final amount proposed") } },
                modifier = Modifier.padding(paddingValues)
            )
            WorkerNavDestination.PROFILE -> {
                Box(Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Primary)
                }
            }
        }
    }
}

@Composable
private fun DashboardTab(
    workerState: UiState<com.kaushalyakarnataka.app.data.model.Worker>,
    pendingJobs: UiState<List<Booking>>,
    earningsData: UiState<com.kaushalyakarnataka.app.data.model.EarningsData>,
    unreadCount: Int,
    onAccept: (String) -> Unit,
    onDecline: (String) -> Unit,
    onMarkComplete: (String) -> Unit,
    onProposeAmount: (String, Int) -> Unit,
    onNotificationsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(modifier = modifier.fillMaxSize(), contentPadding = PaddingValues(bottom = 16.dp)) {
        // Header
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Brush.verticalGradient(listOf(Color(0xFF0F2055), Color(0xFF1E3A8A))))
                    .padding(top = 20.dp, start = 20.dp, end = 20.dp, bottom = 36.dp)
            ) {
                Column {
                    val greeting = when (java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY)) {
                        in 5..11 -> "Good morning"
                        in 12..16 -> "Good afternoon"
                        else -> "Good evening"
                    }
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("$greeting 👋", style = MaterialTheme.typography.bodyMedium, color = Color.White.copy(0.8f))
                            val name = if (workerState is UiState.Success) workerState.data.name.split(" ").first() else "Kaushal"
                            Text(name, style = MaterialTheme.typography.headlineMedium, color = Color.White, fontWeight = FontWeight.Bold)
                        }
                        // Notification bell with badge
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(0.15f))
                                .clickable(onClick = onNotificationsClick),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Notifications, null, tint = Color.White, modifier = Modifier.size(22.dp))
                            if (unreadCount > 0) {
                                Surface(
                                    modifier = Modifier.align(Alignment.TopEnd).size(16.dp),
                                    shape = CircleShape,
                                    color = Error
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(
                                            if (unreadCount > 9) "9+" else unreadCount.toString(),
                                            style = MaterialTheme.typography.labelSmall,
                                            color = Color.White,
                                            fontSize = 8.sp
                                        )
                                    }
                                }
                            }
                        }
                    }
                    Spacer(Modifier.height(16.dp))
                    if (earningsData is UiState.Success) {
                        val e = earningsData.data
                        Surface(shape = RoundedCornerShape(20.dp), color = Color.White.copy(alpha = 0.12f), modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text("This Month's Earnings", style = MaterialTheme.typography.labelMedium, color = Color.White.copy(0.75f))
                                Spacer(Modifier.height(4.dp))
                                Text(CurrencyUtils.formatRupees(e.thisMonthTotal), style = MaterialTheme.typography.displaySmall, color = Color.White, fontWeight = FontWeight.ExtraBold)
                                val trend = if (e.percentageChange >= 0) "↑ ${e.percentageChange}% vs last month" else "↓ ${-e.percentageChange}% vs last month"
                                Text(trend, style = MaterialTheme.typography.labelSmall, color = Color.White.copy(0.65f))
                                Spacer(Modifier.height(12.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    EarningChip(Icons.Default.CheckCircle, "${e.completedJobs} Done")
                                    EarningChip(Icons.Default.Pending, "${e.pendingJobs} Pending")
                                    EarningChip(Icons.Default.Star, "${e.averageRating} ★")
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
                modifier = Modifier.fillMaxWidth().offset(y = (-20).dp),
                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
                color = MaterialTheme.colorScheme.background
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    StatCard(Icons.Default.Assignment, "Requests",
                        if (pendingJobs is UiState.Success) "${(pendingJobs as UiState.Success).data.filter { it.status == BookingStatus.PENDING }.size}" else "—",
                        Error, modifier = Modifier.weight(1f))
                    StatCard(Icons.Default.EmojiEvents, "Total Jobs",
                        if (earningsData is UiState.Success) "${(earningsData as UiState.Success).data.completedJobs}" else "—",
                        Success, modifier = Modifier.weight(1f))
                    StatCard(Icons.Default.Star, "Rating",
                        if (earningsData is UiState.Success) "${(earningsData as UiState.Success).data.averageRating}" else "—",
                        Warning, modifier = Modifier.weight(1f))
                }
            }
        }

        // Pending requests header
        item {
            val pending = (pendingJobs as? UiState.Success)?.data?.filter { it.status == BookingStatus.PENDING } ?: emptyList()
            if (pending.isNotEmpty()) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("New Requests", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Surface(shape = RoundedCornerShape(20.dp), color = ErrorTint) {
                        Text(
                            "${pending.size} new",
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = Error, fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        if (pendingJobs is UiState.Loading) {
            item {
                Box(Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Primary)
                }
            }
        } else if (pendingJobs is UiState.Success) {
            val pending = (pendingJobs as UiState.Success).data.filter { it.status == BookingStatus.PENDING }
            if (pending.isEmpty()) {
                item {
                    Surface(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp), shape = RoundedCornerShape(16.dp), color = MaterialTheme.colorScheme.surface) {
                        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Icon(Icons.Default.EventAvailable, null, tint = Success, modifier = Modifier.size(32.dp))
                            Text("No pending requests right now.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            } else {
                items(pending) { booking ->
                    EnhancedJobCard(
                        booking = booking,
                        onAccept = { onAccept(booking.id) },
                        onDecline = { onDecline(booking.id) },
                        onMarkComplete = { onMarkComplete(booking.id) },
                        onProposeAmount = { amount -> onProposeAmount(booking.id, amount) },
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                    )
                }
            }
        }

        item { Spacer(Modifier.height(16.dp)) }
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
    Surface(modifier = modifier, shape = RoundedCornerShape(16.dp), color = MaterialTheme.colorScheme.surface, shadowElevation = 2.dp) {
        Column(modifier = Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(icon, null, tint = color, modifier = Modifier.size(22.dp))
            Spacer(Modifier.height(4.dp))
            Text(value, style = MaterialTheme.typography.titleMedium, color = color, fontWeight = FontWeight.ExtraBold)
            Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
        }
    }
}

@Composable
fun EnhancedJobCard(
    booking: Booking,
    onAccept: (() -> Unit)? = null,
    onDecline: (() -> Unit)? = null,
    onMarkComplete: (() -> Unit)? = null,
    onProposeAmount: ((Int) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val (statusColor, statusBg) = when (booking.status) {
        BookingStatus.PENDING -> Warning to WarningTint
        BookingStatus.CONFIRMED -> Primary to PrimaryTint
        BookingStatus.IN_PROGRESS -> Success to SuccessTint
        BookingStatus.COMPLETED -> Success to SuccessTint
        BookingStatus.CANCELLED -> Error to ErrorTint
        BookingStatus.AWAITING_PAYMENT_CONFIRMATION -> Secondary to SecondaryTint
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
                    Text(booking.status.name, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), style = MaterialTheme.typography.labelSmall, color = statusColor, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(Modifier.height(10.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(0.5f))
            Spacer(Modifier.height(10.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Schedule, null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(14.dp))
                    Spacer(Modifier.width(4.dp))
                    Text(booking.timeSlot, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.LocationOn, null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(14.dp))
                    Spacer(Modifier.width(4.dp))
                    Text(booking.address.take(25) + if (booking.address.length > 25) "…" else "", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            Spacer(Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                val priceText = when {
                    booking.finalAmount > 0 -> "Final: ${CurrencyUtils.formatRupees(booking.finalAmount)}"
                    booking.negotiatedAmount > 0 -> "Proposed: ${CurrencyUtils.formatRupees(booking.negotiatedAmount)}"
                    booking.estimatedCostMin > 0 && booking.estimatedCostMax > 0 -> "${CurrencyUtils.formatRupees(booking.estimatedCostMin)}–${CurrencyUtils.formatRupees(booking.estimatedCostMax)}"
                    else -> "Contact for price"
                }
                Text(priceText, style = MaterialTheme.typography.titleSmall, color = Primary, fontWeight = FontWeight.ExtraBold)
                Text(booking.bookingCode, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }

            if (booking.status == BookingStatus.PENDING && onAccept != null && onDecline != null) {
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

            if (booking.status == BookingStatus.CONFIRMED && onProposeAmount != null) {
                Spacer(Modifier.height(12.dp))
                var showProposeDialog by remember { mutableStateOf(false) }
                if (showProposeDialog) {
                    var amountText by remember { mutableStateOf(booking.estimatedCostMax.toString()) }
                    AlertDialog(
                        onDismissRequest = { showProposeDialog = false },
                        title = { Text("Propose Final Amount") },
                        text = {
                            OutlinedTextField(
                                value = amountText,
                                onValueChange = { amountText = it.filter { c -> c.isDigit() } },
                                label = { Text("Final Amount (₹)") },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                prefix = { Text("₹") }
                            )
                        },
                        confirmButton = {
                            Button(
                                onClick = {
                                    val amount = amountText.toIntOrNull() ?: 0
                                    if (amount > 0) {
                                        onProposeAmount(amount)
                                        showProposeDialog = false
                                    }
                                }
                            ) { Text("Propose") }
                        },
                        dismissButton = { TextButton(onClick = { showProposeDialog = false }) { Text("Cancel") } }
                    )
                }
                Button(
                    onClick = { showProposeDialog = true },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Success)
                ) {
                    Icon(Icons.Default.Payments, null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Propose Final Amount", fontWeight = FontWeight.Bold)
                }
            }

            if (booking.status == BookingStatus.AWAITING_PAYMENT_CONFIRMATION) {
                Spacer(Modifier.height(12.dp))
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = SecondaryTint,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.HourglassTop, null, tint = Secondary)
                        Text(
                            "Awaiting customer confirmation for ${CurrencyUtils.formatRupees(booking.negotiatedAmount)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = SecondaryDark,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
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
        item {
            Text("Manage Services", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        }
        item {
            Surface(modifier = Modifier.fillMaxWidth().clickable(onClick = onAddService), shape = RoundedCornerShape(16.dp), color = PrimaryTint, shadowElevation = 2.dp) {
                Row(modifier = Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    Box(Modifier.size(52.dp).clip(RoundedCornerShape(14.dp)).background(Primary), contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.Add, null, tint = Color.White, modifier = Modifier.size(28.dp))
                    }
                    Column {
                        Text("Add New Service", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = Primary)
                        Text("Set your pricing and service details", style = MaterialTheme.typography.bodySmall, color = Primary.copy(0.7f))
                    }
                    Spacer(Modifier.weight(1f))
                    Icon(Icons.Default.ChevronRight, null, tint = Primary)
                }
            }
        }
        item {
            Surface(modifier = Modifier.fillMaxWidth().clickable(onClick = onPortfolio), shape = RoundedCornerShape(16.dp), color = MaterialTheme.colorScheme.surface, shadowElevation = 2.dp) {
                Row(modifier = Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    Box(Modifier.size(52.dp).clip(RoundedCornerShape(14.dp)).background(Secondary), contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.PhotoLibrary, null, tint = Color.White, modifier = Modifier.size(26.dp))
                    }
                    Column {
                        Text("Upload Portfolio", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                        Text("Show customers your best work", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Spacer(Modifier.weight(1f))
                    Icon(Icons.Default.ChevronRight, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }

        // Services list
        when (servicesState) {
            is UiState.Loading -> item {
                Box(Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Primary)
                }
            }
            is UiState.Success -> {
                if (servicesState.data.isEmpty()) {
                    item {
                        Surface(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), color = MaterialTheme.colorScheme.surfaceVariant) {
                            Column(modifier = Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Default.Handyman, null, tint = Primary.copy(0.4f), modifier = Modifier.size(48.dp))
                                Spacer(Modifier.height(8.dp))
                                Text("No services added yet", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("Tap 'Add New Service' to get started", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(0.6f))
                            }
                        }
                    }
                } else {
                    item {
                        Text(
                            "Your Services (${servicesState.data.size})",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    items(servicesState.data) { service ->
                        ServiceManageCard(service = service, onDelete = { onDeleteService(service.id) })
                    }
                }
            }
            is UiState.Error -> item {
                Text(servicesState.message, color = MaterialTheme.colorScheme.error)
            }
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
            text = { Text("Are you sure you want to delete \"${service.name}\"? This cannot be undone.") },
            confirmButton = { Button(onClick = { onDelete(); showDeleteDialog = false }, colors = ButtonDefaults.buttonColors(containerColor = Error)) { Text("Delete") } },
            dismissButton = { TextButton(onClick = { showDeleteDialog = false }) { Text("Cancel") } }
        )
    }

    Surface(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp), color = MaterialTheme.colorScheme.surface, shadowElevation = 2.dp) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(service.name, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(2.dp))
                Text(service.description.take(60) + if (service.description.length > 60) "…" else "", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
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
    onMarkComplete: (String) -> Unit,
    onProposeAmount: (String, Int) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("Pending", "Confirmed", "Completed", "All")

    Column(modifier = modifier.fillMaxSize()) {
        ScrollableTabRow(selectedTabIndex = selectedTab, containerColor = MaterialTheme.colorScheme.surface, contentColor = Primary, edgePadding = 16.dp) {
            tabs.forEachIndexed { i, tab ->
                Tab(selected = selectedTab == i, onClick = { selectedTab = i }, text = { Text(tab, style = MaterialTheme.typography.labelLarge) })
            }
        }

        val bookingsToShow: List<Booking> = when (selectedTab) {
            0 -> (pendingJobs as? UiState.Success)?.data?.filter { it.status == BookingStatus.PENDING } ?: emptyList()
            1 -> (upcomingJobs as? UiState.Success)?.data ?: emptyList()
            2 -> (allBookings as? UiState.Success)?.data?.filter { it.status == BookingStatus.COMPLETED } ?: emptyList()
            else -> (allBookings as? UiState.Success)?.data ?: emptyList()
        }

        val isLoading = pendingJobs is UiState.Loading || upcomingJobs is UiState.Loading

        if (isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = Primary) }
        } else if (bookingsToShow.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.EventBusy, null, tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(0.4f), modifier = Modifier.size(72.dp))
                    Spacer(Modifier.height(12.dp))
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
                        onMarkComplete = if (booking.status == BookingStatus.CONFIRMED) ({ onMarkComplete(booking.id) }) else null,
                        onProposeAmount = if (booking.status == BookingStatus.CONFIRMED) ({ amount -> onProposeAmount(booking.id, amount) }) else null
                    )
                }
            }
        }
    }
}
