package com.kaushalyakarnataka.app.ui.screens.customer

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
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
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.kaushalyakarnataka.app.data.model.Booking
import com.kaushalyakarnataka.app.data.model.BookingStatus
import com.kaushalyakarnataka.app.data.model.NegotiationStatus
import com.kaushalyakarnataka.app.ui.components.AvatarComponent
import com.kaushalyakarnataka.app.ui.components.KaushalyaBottomNav
import com.kaushalyakarnataka.app.ui.components.NavDestination
import com.kaushalyakarnataka.app.ui.theme.Error
import com.kaushalyakarnataka.app.ui.theme.ErrorTint
import com.kaushalyakarnataka.app.ui.theme.Primary
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
import com.kaushalyakarnataka.app.utils.UiState
import com.kaushalyakarnataka.app.viewmodel.CustomerBookingsViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomerBookingsScreen(
    onNavigateBottomBar: (NavDestination) -> Unit,
    onNavigateToWorkerProfile: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: CustomerBookingsViewModel = hiltViewModel()
) {
    val bookingsState by viewModel.bookingsState.collectAsState()
    val reviewSubmitState by viewModel.reviewSubmitState.collectAsState()
    val finalAmountState by viewModel.finalAmountState.collectAsState()
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("All", "Upcoming", "Completed", "Cancelled")
    var reviewBooking by remember { mutableStateOf<Booking?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("My Bookings", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        },
        bottomBar = {
            KaushalyaBottomNav(currentDestination = NavDestination.BOOKINGS, onNavigate = onNavigateBottomBar)
        }
    ) { paddingValues ->
        Column(modifier = modifier.fillMaxSize().padding(paddingValues)) {
            ScrollableTabRow(
                selectedTabIndex = selectedTab,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = Primary,
                edgePadding = 16.dp
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = { Text(title, style = MaterialTheme.typography.labelLarge) }
                    )
                }
            }

            when (val state = bookingsState) {
                is UiState.Loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Primary)
                }
                is UiState.Error -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(state.message, color = MaterialTheme.colorScheme.error)
                }
                is UiState.Success -> {
                    val filtered = when (selectedTab) {
                        1 -> state.data.filter {
                            it.status == BookingStatus.PENDING ||
                                it.status == BookingStatus.CONFIRMED ||
                                it.status == BookingStatus.AWAITING_PAYMENT_CONFIRMATION
                        }
                        2 -> state.data.filter { it.status == BookingStatus.COMPLETED }
                        3 -> state.data.filter { it.status == BookingStatus.CANCELLED }
                        else -> state.data
                    }

                    if (filtered.isEmpty()) {
                        EmptyBookingsState()
                    } else {
                        LazyColumn(
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(filtered) { booking ->
                                CustomerBookingCard(
                                    booking = booking,
                                    onViewWorker = { onNavigateToWorkerProfile(booking.workerId) },
                                    onAcceptProposal = {
                                        val amount = if (booking.workerCounterAmount > 0) {
                                            booking.workerCounterAmount
                                        } else {
                                            booking.negotiatedAmount
                                        }
                                        viewModel.respondToNegotiation(booking.id, accepted = true, finalAmount = amount)
                                    },
                                    onRejectProposal = {
                                        viewModel.respondToNegotiation(booking.id, accepted = false)
                                    },
                                    onLeaveReview = { reviewBooking = booking }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    reviewBooking?.let { booking ->
        LeaveReviewDialog(
            workerName = booking.workerName,
            serviceType = booking.service,
            onDismiss = { reviewBooking = null },
            onSubmit = { rating, comment ->
                viewModel.submitReview(
                    workerId = booking.workerId,
                    rating = rating,
                    comment = comment,
                    serviceType = booking.service,
                    bookingId = booking.id
                )
            },
            isLoading = reviewSubmitState is UiState.Loading
        )
    }

    LaunchedEffect(reviewSubmitState) {
        when (val state = reviewSubmitState) {
            is UiState.Success -> {
                scope.launch { snackbarHostState.showSnackbar("Review submitted successfully") }
                viewModel.clearReviewSubmitState()
                reviewBooking = null
            }
            is UiState.Error -> {
                scope.launch { snackbarHostState.showSnackbar(state.message) }
                viewModel.clearReviewSubmitState()
            }
            else -> {}
        }
    }

    LaunchedEffect(finalAmountState) {
        when (val state = finalAmountState) {
            is UiState.Success -> {
                scope.launch { snackbarHostState.showSnackbar("Payment confirmed. Booking completed.") }
                viewModel.clearFinalAmountState()
            }
            is UiState.Error -> {
                scope.launch { snackbarHostState.showSnackbar(state.message) }
                viewModel.clearFinalAmountState()
            }
            else -> {}
        }
    }
}

@Composable
private fun EmptyBookingsState() {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier
                    .size(96.dp)
                    .clip(RoundedCornerShape(28.dp))
                    .background(Primary.copy(0.06f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.EventBusy,
                    contentDescription = null,
                    tint = Primary.copy(0.4f),
                    modifier = Modifier.size(44.dp)
                )
            }
            Spacer(Modifier.height(20.dp))
            Text(
                "No bookings here",
                style = MaterialTheme.typography.titleLarge,
                color = Text1
            )
            Spacer(Modifier.height(6.dp))
            Text(
                "Your bookings will appear once you hire a Kaushal",
                style = MaterialTheme.typography.bodyMedium,
                color = Text3
            )
        }
    }
}

@Composable
fun CustomerBookingCard(
    booking: Booking,
    onViewWorker: () -> Unit,
    onAcceptProposal: (() -> Unit)? = null,
    onRejectProposal: (() -> Unit)? = null,
    onLeaveReview: () -> Unit = {}
) {
    val status = when (booking.status) {
        BookingStatus.PENDING ->
            StatusStyle(Warning, WarningTint, Icons.Default.HourglassTop, "Awaiting quotation")
        BookingStatus.CONFIRMED ->
            StatusStyle(Success, SuccessTint, Icons.Default.CheckCircle, "Confirmed")
        BookingStatus.IN_PROGRESS ->
            StatusStyle(Primary, PrimaryTint, Icons.Default.Schedule, "In progress")
        BookingStatus.COMPLETED ->
            StatusStyle(Success, SuccessTint, Icons.Default.EmojiEvents, "Completed")
        BookingStatus.CANCELLED ->
            StatusStyle(Error, ErrorTint, Icons.Default.Cancel, "Declined")
        BookingStatus.AWAITING_PAYMENT_CONFIRMATION ->
            StatusStyle(Secondary, SecondaryTint, Icons.Default.Payments, "Awaiting payment")
    }
    val statusColor = status.color
    val statusBg = status.bg
    val statusIcon = status.icon
    val statusLabel = status.label

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(3.dp, RoundedCornerShape(20.dp), spotColor = statusColor.copy(0.06f)),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 1.dp
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            // ── Header row ──
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(statusBg),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        statusIcon,
                        null,
                        tint = statusColor,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(Modifier.width(14.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        booking.service,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = Text1
                    )
                    Text(
                        booking.workerName,
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

            // ── Info row ──
            Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                InfoChip(icon = Icons.Default.Schedule, text = booking.timeSlot)
                InfoChip(icon = Icons.Default.LocationOn, text = booking.address.shortAddress())
            }

            Spacer(Modifier.height(12.dp))

            // ── Amount & Booking ID row ──
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Booking ID", style = MaterialTheme.typography.labelSmall, color = Text4)
                    Text(
                        booking.bookingCode,
                        style = MaterialTheme.typography.labelMedium,
                        color = Primary,
                        fontWeight = FontWeight.Bold
                    )
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(
                            Brush.horizontalGradient(
                                listOf(statusColor.copy(0.1f), statusColor.copy(0.05f))
                            )
                        )
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        booking.primaryAmountText(),
                        style = MaterialTheme.typography.titleSmall,
                        color = statusColor,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }

            // ── Status-specific panels ──
            if (booking.status == BookingStatus.CONFIRMED) {
                Spacer(Modifier.height(12.dp))
                StatusPanel(
                    icon = Icons.Default.HourglassTop,
                    text = "Waiting for worker to send the final amount",
                    tint = Primary,
                    bg = PrimaryTint
                )
            }

            if (
                booking.status == BookingStatus.AWAITING_PAYMENT_CONFIRMATION &&
                (booking.negotiationStatus == NegotiationStatus.WORKER_PROPOSED || booking.negotiationStatus == NegotiationStatus.WORKER_COUNTERED) &&
                onAcceptProposal != null &&
                onRejectProposal != null
            ) {
                Spacer(Modifier.height(12.dp))
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = SecondaryTint,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(Secondary.copy(0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.Payments,
                                    null,
                                    tint = SecondaryDark,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(Modifier.width(10.dp))
                            Text(
                                "Final amount: ${CurrencyUtils.formatRupees(booking.counterAmount())}",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = SecondaryDark
                            )
                        }
                        Spacer(Modifier.height(12.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            OutlinedButton(
                                onClick = onRejectProposal,
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = Error),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text("Ask Clarification", fontWeight = FontWeight.Bold)
                            }
                            Button(
                                onClick = onAcceptProposal,
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(containerColor = Success),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text("Accept & Confirm", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            if (
                booking.status == BookingStatus.AWAITING_PAYMENT_CONFIRMATION &&
                booking.negotiationStatus == NegotiationStatus.CUSTOMER_PROPOSED
            ) {
                Spacer(Modifier.height(12.dp))
                StatusPanel(
                    icon = Icons.Default.HourglassTop,
                    text = "Waiting for worker approval",
                    tint = Primary,
                    bg = PrimaryTint
                )
            }

            if (booking.status == BookingStatus.COMPLETED) {
                Spacer(Modifier.height(12.dp))
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            "Initial labour: ${booking.initialAmountText()}",
                            style = MaterialTheme.typography.bodySmall,
                            color = Text3
                        )
                        if (booking.finalAmount > 0) {
                            Spacer(Modifier.height(4.dp))
                            Text(
                                "Paid: ${CurrencyUtils.formatRupees(booking.finalAmount)}",
                                style = MaterialTheme.typography.titleSmall,
                                color = Success,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
                Spacer(Modifier.height(12.dp))
                OutlinedButton(
                    onClick = onLeaveReview,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Default.RateReview, null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Leave a Review", fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

@Composable
private fun StatusPanel(
    icon: ImageVector,
    text: String,
    tint: Color,
    bg: Color
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = bg,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(tint.copy(0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, null, tint = tint, modifier = Modifier.size(18.dp))
            }
            Spacer(Modifier.width(10.dp))
            Text(
                text,
                style = MaterialTheme.typography.bodyMedium,
                color = tint,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LeaveReviewDialog(
    workerName: String,
    serviceType: String,
    onDismiss: () -> Unit,
    onSubmit: (Int, String) -> Unit,
    isLoading: Boolean
) {
    var rating by remember { mutableStateOf(0) }
    var comment by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Rate $workerName") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text(serviceType, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                    for (i in 1..5) {
                        Icon(
                            imageVector = if (i <= rating) Icons.Filled.Star else Icons.Outlined.Star,
                            contentDescription = "$i stars",
                            tint = if (i <= rating) Warning else MaterialTheme.colorScheme.outlineVariant,
                            modifier = Modifier.size(40.dp).clickable { rating = i }
                        )
                    }
                }
                OutlinedTextField(
                    value = comment,
                    onValueChange = { comment = it; error = null },
                    label = { Text("Write a review") },
                    modifier = Modifier.fillMaxWidth().height(100.dp),
                    maxLines = 4,
                    isError = error != null,
                    supportingText = { error?.let { Text(it, color = MaterialTheme.colorScheme.error) } }
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (rating == 0) {
                        error = "Please select a rating"
                    } else if (comment.isBlank()) {
                        error = "Please write a review"
                    } else {
                        onSubmit(rating, comment)
                    }
                },
                enabled = !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp, color = MaterialTheme.colorScheme.onPrimary)
                } else {
                    Text("Submit")
                }
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@Composable
private fun InfoChip(icon: ImageVector, text: String) {
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

private fun Booking.primaryAmountText(): String {
    return when {
        finalAmount > 0 -> "Paid: ${CurrencyUtils.formatRupees(finalAmount)}"
        workerCounterAmount > 0 -> "Revised: ${CurrencyUtils.formatRupees(workerCounterAmount)}"
        negotiatedAmount > 0 -> "Revised: ${CurrencyUtils.formatRupees(negotiatedAmount)}"
        customerProposedAmount > 0 -> "Requested: ${CurrencyUtils.formatRupees(customerProposedAmount)}"
        else -> initialAmountText()
    }
}

private fun Booking.counterAmount(): Int {
    return if (workerCounterAmount > 0) workerCounterAmount else negotiatedAmount
}

private fun Booking.isWorkerCountered(): Boolean {
    return negotiationStatus == NegotiationStatus.WORKER_COUNTERED ||
        negotiationStatus == NegotiationStatus.WORKER_PROPOSED ||
        counterAmount() > 0
}


private data class StatusStyle(
    val color: Color,
    val bg: Color,
    val icon: ImageVector,
    val label: String
)

private fun String.shortAddress(): String {
    return if (length > 25) "${take(25)}..." else this
}
