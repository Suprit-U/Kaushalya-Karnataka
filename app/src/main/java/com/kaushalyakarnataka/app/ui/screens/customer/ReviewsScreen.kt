package com.kaushalyakarnataka.app.ui.screens.customer

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.kaushalyakarnataka.app.data.model.RatingStats
import com.kaushalyakarnataka.app.ui.components.AppTopBar
import com.kaushalyakarnataka.app.ui.components.RatingBreakdown
import com.kaushalyakarnataka.app.ui.components.ReviewCard
import com.kaushalyakarnataka.app.ui.screens.common.LoadingScreen
import com.kaushalyakarnataka.app.utils.UiState
import com.kaushalyakarnataka.app.viewmodel.ReviewViewModel

@Composable
fun ReviewsScreen(
    workerId: String,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ReviewViewModel = hiltViewModel()
) {
    val reviewsState by viewModel.reviewsState.collectAsState()

    Scaffold(
        topBar = {
            AppTopBar(
                title = "Reviews",
                onBackClick = onNavigateBack
            )
        }
    ) { paddingValues ->
        when (val state = reviewsState) {
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
                val reviews = state.data
                
                // Calculate stats
                val totalReviews = reviews.size
                val avgRating = if (totalReviews > 0) reviews.map { it.rating }.average().toFloat() else 0f
                val ratingCounts = mutableMapOf(1 to 0, 2 to 0, 3 to 0, 4 to 0, 5 to 0)
                reviews.forEach { review ->
                    ratingCounts[review.rating] = (ratingCounts[review.rating] ?: 0) + 1
                }
                
                val stats = RatingStats(avgRating, totalReviews, ratingCounts)

                LazyColumn(
                    modifier = modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentPadding = PaddingValues(16.dp)
                ) {
                    item {
                        RatingBreakdown(stats = stats)
                        Spacer(modifier = Modifier.height(24.dp))
                        HorizontalDivider()
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "All Reviews",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )
                    }
                    
                    items(reviews) { review ->
                        ReviewCard(
                            review = review,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )
                    }
                }
            }
        }
    }
}
