package com.kaushalyakarnataka.app.ui.screens.customer

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import kotlinx.coroutines.launch
import androidx.compose.material.icons.outlined.Star
import com.kaushalyakarnataka.app.data.model.Booking
import com.kaushalyakarnataka.app.data.model.BookingStatus
import com.kaushalyakarnataka.app.ui.components.AvatarComponent
import com.kaushalyakarnataka.app.ui.components.KaushalyaBottomNav
import com.kaushalyakarnataka.app.ui.components.NavDestination
import com.kaushalyakarnataka.app.ui.theme.*
import com.kaushalyakarnataka.app.utils.CurrencyUtils
import com.kaushalyakarnataka.app.utils.DateUtils
import com.kaushalyakarnataka.app.utils.UiState
import com.kaushalyakarnataka.app.viewmodel.CustomerBookingsViewModel

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
            // Tab row
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
                is UiState.Loading -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = Primary)
                    }
                }
                is UiState.Error -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(state.message, color = MaterialTheme.colorScheme.error)
                    }
                }
                is UiState.Success -> {
                    val filtered = when (selectedTab) {
                        1 -> state.data.filter { it.status == BookingStatus.PENDING || it.status == BookingStatus.CONFIRMED }
                        2 -> state.data.filter { it.status == BookingStatus.COMPLETED }
                        3 -> state.data.filter { it.status == BookingStatus.CANCELLED }
                        else -> state.data
                    }

                    if (filtered.isEmpty()) {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Default.EventBusy, null, tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(0.4f), modifier = Modifier.size(72.dp))
                                Spacer(Modifier.height(16.dp))
                                Text("No bookings here", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
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
                                        viewModel.respondToNegotiation(booking.id, accepted = true, finalAmount = booking.negotiatedAmount)
                                        scope.launch { snackbarHostState.showSnackbar("Payment confirmed. Booking completed!") }
                                    },
                                    onRejectProposal = {
                                        viewModel.respondToNegotiation(booking.id, accepted = false)
                                        scope.launch { snackbarHostState.showSnackbar("Price proposal rejected") }
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
        if (reviewSubmitState is UiState.Success) {
            scope.launch { snackbarHostState.showSnackbar("Review submitted successfully") }
            viewModel.clearReviewSubmitState()
            reviewBooking = null
        } else if (reviewSubmitState is UiState.Error) {
            scope.launch { snackbarHostState.showSnackbar((reviewSubmitState as UiState.Error).message) }
            viewModel.clearReviewSubmitState()
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
                        booking.status.name,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = statusColor,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(Modifier.height(12.dp))
            Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(0.5f))
            Spacer(Modifier.height(12.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                InfoChip(icon = Icons.Default.Schedule, text = booking.timeSlot)
                InfoChip(icon = Icons.Default.LocationOn, text = booking.address.take(25) + if (booking.address.length > 25) "…" else "")
            }

            Spacer(Modifier.height(10.dp))

            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Column {
                    Text("Booking ID", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(booking.bookingCode, style = MaterialTheme.typography.labelMedium, color = Primary, fontWeight = FontWeight.Bold)
                }
                val priceText = when {
                    booking.finalAmount > 0 -> "Final: ${CurrencyUtils.formatRupees(booking.finalAmount)}"
                    booking.negotiatedAmount > 0 -> "Proposed: ${CurrencyUtils.formatRupees(booking.negotiatedAmount)}"
                    booking.estimatedCostMin > 0 && booking.estimatedCostMax > 0 -> "${CurrencyUtils.formatRupees(booking.estimatedCostMin)}–${CurrencyUtils.formatRupees(booking.estimatedCostMax)}"
                    else -> "Contact for price"
                }
                Text(
                    priceText,
                    style = MaterialTheme.typography.titleSmall,
                    color = Primary,
                    fontWeight = FontWeight.ExtraBold
                )
            }

            if (booking.status == BookingStatus.AWAITING_PAYMENT_CONFIRMATION && onAcceptProposal != null && onRejectProposal != null) {
                Spacer(Modifier.height(10.dp))
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = SecondaryTint,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            "Worker proposed: ${CurrencyUtils.formatRupees(booking.negotiatedAmount)}",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.SemiBold,
                            color = SecondaryDark
                        )
                        Spacer(Modifier.height(8.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            OutlinedButton(onClick = onRejectProposal, modifier = Modifier.weight(1f), colors = ButtonDefaults.outlinedButtonColors(contentColor = Error)) {
                                Text("Reject", fontWeight = FontWeight.Bold)
                            }
                            Button(onClick = onAcceptProposal, modifier = Modifier.weight(1f), colors = ButtonDefaults.buttonColors(containerColor = Success)) {
                                Text("Accept & Pay", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            if (booking.status == BookingStatus.COMPLETED) {
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
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    for (i in 1..5) {
                        Icon(
                            imageVector = if (i <= rating) Icons.Filled.Star else Icons.Outlined.Star,
                            contentDescription = "$i stars",
                            tint = if (i <= rating) Warning else MaterialTheme.colorScheme.outlineVariant,
                            modifier = Modifier
                                .size(40.dp)
                                .clickable { rating = i }
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
private fun InfoChip(icon: androidx.compose.ui.graphics.vector.ImageVector, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(14.dp))
        Spacer(Modifier.width(4.dp))
        Text(text, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
