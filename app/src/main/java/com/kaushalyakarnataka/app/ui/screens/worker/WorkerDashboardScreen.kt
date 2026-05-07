package com.kaushalyakarnataka.app.ui.screens.worker

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.kaushalyakarnataka.app.ui.components.AppTopBar
import com.kaushalyakarnataka.app.ui.components.BottomNavBar
import com.kaushalyakarnataka.app.ui.components.JobCard
import com.kaushalyakarnataka.app.ui.components.NavDestination
import com.kaushalyakarnataka.app.ui.components.WorkerQuickActionGrid
import com.kaushalyakarnataka.app.ui.screens.common.LoadingScreen
import com.kaushalyakarnataka.app.utils.CurrencyUtils
import com.kaushalyakarnataka.app.utils.UiState
import com.kaushalyakarnataka.app.viewmodel.WorkerDashboardViewModel

private enum class DashboardSection {
    REQUESTS,
    EARNINGS,
    HISTORY,
}

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
    var selectedSection by remember { mutableStateOf(DashboardSection.REQUESTS) }

    Scaffold(
        topBar = {
            AppTopBar(title = "Kaushal Dashboard")
        },
        bottomBar = {
            BottomNavBar(
                currentDestination = NavDestination.HOME,
                onNavigate = onNavigateBottomBar
            )
        }
    ) { paddingValues ->
        if (workerState is UiState.Loading) {
            LoadingScreen(modifier = Modifier.padding(paddingValues))
        } else if (workerState is UiState.Success) {
            val worker = (workerState as UiState.Success).data
            
            LazyColumn(
                modifier = modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(16.dp)
            ) {
                item {
                    Text(
                        text = "Welcome back, ${worker.name.split(" ").firstOrNull() ?: worker.name}",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    WorkerQuickActionGrid(
                        onAddService = onNavigateToAddService,
                        onManagePortfolio = onNavigateToPortfolio,
                        onViewEarnings = { selectedSection = DashboardSection.EARNINGS },
                        onViewHistory = { selectedSection = DashboardSection.HISTORY }
                    )
                    
                    Spacer(modifier = Modifier.height(32.dp))
                    
                    Text(
                        text = when (selectedSection) {
                            DashboardSection.REQUESTS -> "Pending Requests"
                            DashboardSection.EARNINGS -> "Earnings"
                            DashboardSection.HISTORY -> "Upcoming Jobs"
                        },
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }
                
                when (selectedSection) {
                    DashboardSection.REQUESTS -> {
                        when (val pending = pendingJobs) {
                            is UiState.Loading -> item { Text("Loading requests...") }
                            is UiState.Error -> item { Text(pending.message, color = MaterialTheme.colorScheme.error) }
                            is UiState.Success -> {
                                if (pending.data.isEmpty()) {
                                    item {
                                        Text(
                                            text = "You have no pending requests.",
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                } else {
                                    items(pending.data) { booking ->
                                        JobCard(
                                            booking = booking,
                                            onClick = { },
                                            onAccept = { viewModel.acceptJob(booking.id) },
                                            onDecline = { viewModel.declineJob(booking.id) },
                                            modifier = Modifier.padding(bottom = 16.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                    DashboardSection.EARNINGS -> {
                        item {
                            EarningsSummaryCard(
                                state = earningsData,
                                modifier = Modifier.padding(bottom = 16.dp)
                            )
                        }
                    }
                    DashboardSection.HISTORY -> {
                        when (val upcoming = upcomingJobs) {
                            is UiState.Loading -> item { Text("Loading jobs...") }
                            is UiState.Error -> item { Text(upcoming.message, color = MaterialTheme.colorScheme.error) }
                            is UiState.Success -> {
                                if (upcoming.data.isEmpty()) {
                                    item {
                                        Text(
                                            text = "No upcoming confirmed jobs.",
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                } else {
                                    items(upcoming.data) { booking ->
                                        JobCard(
                                            booking = booking,
                                            onClick = { },
                                            modifier = Modifier.padding(bottom = 16.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun EarningsSummaryCard(
    state: UiState<com.kaushalyakarnataka.app.data.model.EarningsData>,
    modifier: Modifier = Modifier
) {
    when (state) {
        is UiState.Loading -> Text("Loading earnings...")
        is UiState.Error -> Text(state.message, color = MaterialTheme.colorScheme.error)
        is UiState.Success -> {
            val earnings = state.data
            Card(
                modifier = modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = CurrencyUtils.formatRupees(earnings.thisMonthTotal),
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "This month, ${earnings.percentageChange}% vs last month",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        EarningsMetric("Completed", earnings.completedJobs.toString())
                        EarningsMetric("Pending", earnings.pendingJobs.toString())
                        EarningsMetric("Rating", earnings.averageRating.toString())
                    }
                }
            }
        }
    }
}

@Composable
private fun EarningsMetric(label: String, value: String) {
    Column {
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
