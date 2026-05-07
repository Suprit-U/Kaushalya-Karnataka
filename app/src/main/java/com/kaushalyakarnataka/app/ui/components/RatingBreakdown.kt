package com.kaushalyakarnataka.app.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.unit.dp
import com.kaushalyakarnataka.app.data.model.RatingStats
import com.kaushalyakarnataka.app.ui.theme.Warning

@Composable
fun RatingBreakdown(
    stats: RatingStats,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = String.format("%.1f", stats.averageRating),
                    style = MaterialTheme.typography.displayMedium,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                )
                RatingStars(rating = stats.averageRating.toInt())
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Based on ${stats.totalReviews} reviews",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        val maxCount = stats.ratingCounts.values.maxOrNull()?.toFloat()?.coerceAtLeast(1f) ?: 1f
        
        // 5 stars to 1 star
        for (i in 5 downTo 1) {
            val count = stats.ratingCounts[i] ?: 0
            val progress = count.toFloat() / maxCount
            
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            ) {
                Text(
                    text = "$i",
                    style = MaterialTheme.typography.labelMedium,
                    modifier = Modifier.width(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .weight(1f)
                        .height(8.dp),
                    color = Warning,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant,
                    strokeCap = StrokeCap.Round,
                )
            }
        }
    }
}
