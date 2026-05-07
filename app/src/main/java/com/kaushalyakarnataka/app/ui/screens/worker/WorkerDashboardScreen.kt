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
import com.kaushalyakarnataka.app.ui.components.*
import com.kaushalyakarnataka.app.ui.theme.*
import com.kaushalyakarnataka.app.utils.CurrencyUtils
import com.kaushalyakarnataka.app.utils.UiState
import com.kaushalyakarnataka.app.viewmodel.WorkerDashboardViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkerDashboardScreen(
    onNavigateToAddService: () -> Unit,
    onNavigateToPortfolio: () -> Unit,
    onNavigateBottomBar: (NavDestination) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: WorkerDashboardViewModel = hiltViewModel()
) {
    val workerState by viewModel.workerState.collectAsState()
    val pendingJobs by viewModel.pendingJobs.collectAsState()
    val upcomingJobs by viewModel.upcomingJobs.collectAsState()
    val earningsData by viewModel.earningsData.collectAsState()
    var currentWorkerTab by remember { mutableStateOf(WorkerNavDestination.DASHBOARD) }

    Scaffold(
        bottomBar = {
            WorkerBottomNav(
                currentDestination = currentWorkerTab,
                onNavigate = { dest ->
                    currentWorkerTab = dest
                    when (dest) {
                        WorkerNavDestination.PROFILE -> onNavigateBottomBar(NavDestination.PROFILE)
                        else -> { /* handled internally */ }
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
                onAccept = { viewModel.acceptJob(it) },
                onDecline = { viewModel.declineJob(it) },
                modifier = Modifier.padding(paddingValues)
            )
            WorkerNavDestination.SERVICES -> ServicesTab(
                onAddService = onNavigateToAddService,
                onPortfolio = onNavigateToPortfolio,
                modifier = Modifier.padding(paddingValues)
            )
            WorkerNavDestination.BOOKINGS -> BookingsTab(
                pendingJobs = pendingJobs,
                upcomingJobs = upcomingJobs,
                onAccept = { viewModel.acceptJob(it) },
                onDecline = { viewModel.declineJob(it) },
                modifier = Modifier.padding(paddingValues)
            )
            WorkerNavDestination.PROFILE -> {
                // Handled by nav callback above; show loading
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
    onAccept: (String) -> Unit,
    onDecline: (String) -> Unit,
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
                    Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Column {
                            Text("$greeting 👋", style = MaterialTheme.typography.bodyMedium, color = Color.White.copy(0.8f))
                            val name = if (workerState is UiState.Success) workerState.data.name.split(" ").first() else "Kaushal"
                            Text(name, style = MaterialTheme.typography.headlineMedium, color = Color.White, fontWeight = FontWeight.Bold)
                        }
                        Box(
                            modifier = Modifier.size(40.dp).clip(CircleShape)
                                .background(Color.White.copy(0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Notifications, null, tint = Color.White, modifier = Modifier.size(22.dp))
                        }
                    }
                    Spacer(Modifier.height(16.dp))
                    // Earnings card embedded in header
                    if (earningsData is UiState.Success) {
                        val e = earningsData.data
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = Color.White.copy(alpha = 0.12f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
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

        // Stats cards row
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
                    StatCard(Icons.Default.Assignment, "New Requests",
                        if (pendingJobs is UiState.Success) "${pendingJobs.data.size}" else "—",
                        Error, modifier = Modifier.weight(1f))
                    StatCard(Icons.Default.EmojiEvents, "Total Jobs",
                        if (earningsData is UiState.Success) "${earningsData.data.completedJobs}" else "—",
                        Success, modifier = Modifier.weight(1f))
                    StatCard(Icons.Default.Star, "Rating",
                        if (earningsData is UiState.Success) "${earningsData.data.averageRating}" else "—",
                        Warning, modifier = Modifier.weight(1f))
                }
            }
        }

        // Pending requests
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
                            color = Error,
                            fontWeight = FontWeight.Bold
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
                    Surface(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.surface
                    ) {
                        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Icon(Icons.Default.EventAvailable, null, tint = Success, modifier = Modifier.size(32.dp))
                            Text("No pending requests right now.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            } else {
                items(pending) { booking ->
                    JobCard(
                        booking = booking,
                        onClick = {},
                        onAccept = { onAccept(booking.id) },
                        onDecline = { onDecline(booking.id) },
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun EarningChip(icon: ImageVector, text: String) {
    Surface(shape = RoundedCornerShape(20.dp), color = Color.White.copy(0.15f)) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(icon, null, tint = Color.White, modifier = Modifier.size(12.dp))
            Text(text, style = MaterialTheme.typography.labelSmall, color = Color.White)
        }
    }
}

@Composable
private fun StatCard(icon: ImageVector, label: String, value: String, color: Color, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 2.dp
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(icon, null, tint = color, modifier = Modifier.size(22.dp))
            Spacer(Modifier.height(4.dp))
            Text(value, style = MaterialTheme.typography.titleMedium, color = color, fontWeight = FontWeight.ExtraBold)
            Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
        }
    }
}

@Composable
private fun ServicesTab(
    onAddService: () -> Unit,
    onPortfolio: () -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(modifier = modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item {
            Text("Manage Services", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        }
        item {
            // Add service card
            Surface(
                modifier = Modifier.fillMaxWidth().clickable(onClick = onAddService),
                shape = RoundedCornerShape(16.dp),
                color = PrimaryTint,
                shadowElevation = 2.dp
            ) {
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
            // Portfolio card
            Surface(
                modifier = Modifier.fillMaxWidth().clickable(onClick = onPortfolio),
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 2.dp
            ) {
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
        item {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surfaceVariant
            ) {
                Column(modifier = Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.Handyman, null, tint = Primary.copy(0.4f), modifier = Modifier.size(48.dp))
                    Spacer(Modifier.height(8.dp))
                    Text("No services added yet", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("Tap 'Add New Service' to get started", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(0.6f))
                }
            }
        }
    }
}

@Composable
private fun BookingsTab(
    pendingJobs: UiState<List<Booking>>,
    upcomingJobs: UiState<List<Booking>>,
    onAccept: (String) -> Unit,
    onDecline: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("Pending", "Upcoming", "All")

    Column(modifier = modifier.fillMaxSize()) {
        TabRow(selectedTabIndex = selectedTab, containerColor = MaterialTheme.colorScheme.surface, contentColor = Primary) {
            tabs.forEachIndexed { i, tab ->
                Tab(selected = selectedTab == i, onClick = { selectedTab = i }, text = { Text(tab, style = MaterialTheme.typography.labelLarge) })
            }
        }

        val bookingsToShow: List<Booking> = when (selectedTab) {
            0 -> (pendingJobs as? UiState.Success)?.data?.filter { it.status == BookingStatus.PENDING } ?: emptyList()
            1 -> (upcomingJobs as? UiState.Success)?.data ?: emptyList()
            else -> ((pendingJobs as? UiState.Success)?.data ?: emptyList()) + ((upcomingJobs as? UiState.Success)?.data ?: emptyList())
        }

        val isLoading = pendingJobs is UiState.Loading || upcomingJobs is UiState.Loading

        if (isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Primary)
            }
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
                    JobCard(
                        booking = booking,
                        onClick = {},
                        onAccept = if (booking.status == BookingStatus.PENDING) ({ onAccept(booking.id) }) else null,
                        onDecline = if (booking.status == BookingStatus.PENDING) ({ onDecline(booking.id) }) else null
                    )
                }
            }
        }
    }
}
