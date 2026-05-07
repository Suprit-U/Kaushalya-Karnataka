package com.kaushalyakarnataka.app.ui.screens.customer

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
import androidx.compose.material.icons.filled.EventBusy
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.RateReview
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Star
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
    var finalAmountBooking by remember { mutableStateOf<Booking?>(null) }
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
                                    onRequestFinalAmount = { finalAmountBooking = booking },
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

    finalAmountBooking?.let { booking ->
        FinalAmountDialog(
            booking = booking,
            isLoading = finalAmountState is UiState.Loading,
            onDismiss = { finalAmountBooking = null },
            onSubmit = { amount -> viewModel.requestFinalAmount(booking.id, amount) }
        )
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
                scope.launch { snackbarHostState.showSnackbar("Final amount sent for worker approval") }
                viewModel.clearFinalAmountState()
                finalAmountBooking = null
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
            Icon(
                Icons.Default.EventBusy,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(0.4f),
                modifier = Modifier.size(64.dp)
            )
            Spacer(Modifier.height(12.dp))
            Text("No bookings here", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
fun CustomerBookingCard(
    booking: Booking,
    onViewWorker: () -> Unit,
    onRequestFinalAmount: () -> Unit = {},
    onAcceptProposal: (() -> Unit)? = null,
    onRejectProposal: (() -> Unit)? = null,
    onLeaveReview: () -> Unit = {}
) {
    val (statusColor, statusBg) = when (booking.status) {
        BookingStatus.PENDING -> Warning to WarningTint
        BookingStatus.CONFIRMED -> Success to SuccessTint
        BookingStatus.IN_PROGRESS -> Primary to PrimaryTint
        BookingStatus.COMPLETED -> Success to SuccessTint
        BookingStatus.CANCELLED -> Error to ErrorTint
        BookingStatus.AWAITING_PAYMENT_CONFIRMATION -> Secondary to SecondaryTint
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 2.dp
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                AvatarComponent(imageUrl = "", name = booking.workerName, size = 48.dp)
                Spacer(Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(booking.workerName, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                    Text(booking.service, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Surface(shape = RoundedCornerShape(8.dp), color = statusBg) {
                    Text(
                        booking.status.cleanLabel(),
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = statusColor,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(Modifier.height(12.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(0.5f))
            Spacer(Modifier.height(12.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                InfoChip(icon = Icons.Default.Schedule, text = booking.timeSlot)
                InfoChip(icon = Icons.Default.LocationOn, text = booking.address.shortAddress())
            }

            Spacer(Modifier.height(10.dp))

            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Booking ID", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(booking.bookingCode, style = MaterialTheme.typography.labelMedium, color = Primary, fontWeight = FontWeight.Bold)
                }
                Text(
                    booking.primaryAmountText(),
                    style = MaterialTheme.typography.titleSmall,
                    color = Primary,
                    fontWeight = FontWeight.ExtraBold
                )
            }

            if (booking.status == BookingStatus.CONFIRMED) {
                Spacer(Modifier.height(10.dp))
                Button(
                    onClick = onRequestFinalAmount,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Primary)
                ) {
                    Icon(Icons.Default.Payments, null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Send Final Amount", fontWeight = FontWeight.Bold)
                }
            }

            if (
                booking.status == BookingStatus.AWAITING_PAYMENT_CONFIRMATION &&
                booking.negotiationStatus == NegotiationStatus.CUSTOMER_PROPOSED
            ) {
                Spacer(Modifier.height(10.dp))
                Surface(shape = RoundedCornerShape(8.dp), color = PrimaryTint, modifier = Modifier.fillMaxWidth()) {
                    Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.HourglassTop, null, tint = Primary, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(
                            "Waiting for worker approval for ${CurrencyUtils.formatRupees(booking.customerProposedAmount)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = Primary,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            if (
                booking.status == BookingStatus.AWAITING_PAYMENT_CONFIRMATION &&
                booking.isWorkerCountered() &&
                onAcceptProposal != null &&
                onRejectProposal != null
            ) {
                Spacer(Modifier.height(10.dp))
                Surface(shape = RoundedCornerShape(8.dp), color = SecondaryTint, modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            "Worker suggested: ${CurrencyUtils.formatRupees(booking.counterAmount())}",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.SemiBold,
                            color = SecondaryDark
                        )
                        Spacer(Modifier.height(8.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            OutlinedButton(
                                onClick = onRejectProposal,
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = Error)
                            ) {
                                Text("Reject", fontWeight = FontWeight.Bold)
                            }
                            Button(
                                onClick = onAcceptProposal,
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(containerColor = Success)
                            ) {
                                Text("Accept & Pay", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            if (booking.status == BookingStatus.COMPLETED) {
                Spacer(Modifier.height(10.dp))
                Surface(shape = RoundedCornerShape(8.dp), color = MaterialTheme.colorScheme.surfaceVariant, modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            "Initial labour: ${booking.initialAmountText()}",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        if (booking.finalAmount > 0) {
                            Text(
                                "Paid: ${CurrencyUtils.formatRupees(booking.finalAmount)}",
                                style = MaterialTheme.typography.titleSmall,
                                color = Success,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
                Spacer(Modifier.height(10.dp))
                OutlinedButton(onClick = onLeaveReview, modifier = Modifier.fillMaxWidth()) {
                    Icon(Icons.Default.RateReview, null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Leave a Review")
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FinalAmountDialog(
    booking: Booking,
    isLoading: Boolean,
    onDismiss: () -> Unit,
    onSubmit: (Int) -> Unit
) {
    var amountText by remember(booking.id) {
        mutableStateOf(
            when {
                booking.finalAmount > 0 -> booking.finalAmount.toString()
                booking.estimatedCostMax > 0 -> booking.estimatedCostMax.toString()
                booking.estimatedCostMin > 0 -> booking.estimatedCostMin.toString()
                else -> ""
            }
        )
    }
    var error by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Final Payable Amount") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    "Initial estimate: ${booking.initialAmountText()}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it.filter { c -> c.isDigit() }; error = null },
                    label = { Text("Agreed final amount") },
                    prefix = { Text("Rs. ") },
                    modifier = Modifier.fillMaxWidth(),
                    isError = error != null,
                    supportingText = { error?.let { Text(it, color = MaterialTheme.colorScheme.error) } },
                    shape = RoundedCornerShape(12.dp)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amount = amountText.toIntOrNull() ?: 0
                    if (amount <= 0) {
                        error = "Enter a valid amount"
                    } else {
                        onSubmit(amount)
                    }
                },
                enabled = !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp, color = MaterialTheme.colorScheme.onPrimary)
                } else {
                    Text("Send")
                }
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
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

private fun BookingStatus.cleanLabel(): String {
    return name.replace('_', ' ')
}

private fun String.shortAddress(): String {
    return if (length > 25) "${take(25)}..." else this
}
