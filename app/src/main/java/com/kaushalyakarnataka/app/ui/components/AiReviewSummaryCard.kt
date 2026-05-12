package com.kaushalyakarnataka.app.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.kaushalyakarnataka.app.ui.components.animations.ShimmerLine
import com.kaushalyakarnataka.app.ui.theme.*
import com.kaushalyakarnataka.app.utils.UiState

@Composable
fun AiReviewSummaryCard(
    state: UiState<String>,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            Brush.horizontalGradient(listOf(Primary.copy(0.15f), PrimaryLight.copy(0.08f), Primary.copy(0.15f)))
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(Brush.horizontalGradient(listOf(Primary.copy(0.12f), PrimaryLight.copy(0.08f)))),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = Primary,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "AI Review Summary",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            when (state) {
                is UiState.Loading -> {
                    AiSummaryShimmer()
                }
                is UiState.Error -> {
                    AiSummaryError(state.message, onRetry)
                }
                is UiState.Success -> {
                    AiSummaryContent(state.data)
                }
            }
        }
    }
}

@Composable
private fun AiSummaryContent(summary: String) {
    AnimatedVisibility(
        visible = true,
        enter = fadeIn(animationSpec = androidx.compose.animation.core.tween(400))
    ) {
        Text(
            text = summary,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun AiSummaryShimmer() {
    Column {
        ShimmerLine(height = 14.dp)
        Spacer(modifier = Modifier.height(8.dp))
        ShimmerLine(width = 200.dp, height = 14.dp)
    }
}

@Composable
private fun AiSummaryError(message: String, onRetry: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = if (message.contains("Not enough")) Icons.Default.Warning else Icons.Default.Refresh,
                contentDescription = null,
                tint = if (message.contains("Not enough")) Warning else Error,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = if (message.contains("Not enough")) "Not enough reviews yet" else "Unable to generate summary",
                style = MaterialTheme.typography.bodySmall,
                color = if (message.contains("Not enough")) Warning else Error
            )
        }
        if (!message.contains("Not enough")) {
            Spacer(modifier = Modifier.height(8.dp))
            TextButton(onClick = onRetry) {
                Text("Retry", color = Primary)
            }
        }
    }
}
