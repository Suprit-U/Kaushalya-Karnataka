package com.kaushalyakarnataka.app.ui.components.animations

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.material3.MaterialTheme

@Composable
fun shimmerBrush(showShimmer: Boolean = true, targetValue: Float = 1000f): Brush {
    return if (showShimmer) {
        val surfaceColor = MaterialTheme.colorScheme.surfaceVariant
        val shimmerColors = listOf(
            surfaceColor.copy(alpha = 0.6f),
            surfaceColor.copy(alpha = 0.2f),
            surfaceColor.copy(alpha = 0.6f),
        )
        val transition = rememberInfiniteTransition(label = "shimmer")
        val translateAnimation = transition.animateFloat(
            initialValue = 0f,
            targetValue = targetValue,
            animationSpec = infiniteRepeatable(
                animation = tween(1200, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Restart
            ),
            label = "shimmer_translate"
        )
        Brush.linearGradient(
            colors = shimmerColors,
            start = Offset.Zero,
            end = Offset(x = translateAnimation.value, y = translateAnimation.value)
        )
    } else {
        Brush.linearGradient(
            colors = listOf(Color.Transparent, Color.Transparent),
            start = Offset.Zero,
            end = Offset.Zero
        )
    }
}

@Composable
fun ShimmerCard(
    modifier: Modifier = Modifier,
    height: Dp = 120.dp,
    shape: RoundedCornerShape = RoundedCornerShape(16.dp)
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .clip(shape)
            .background(shimmerBrush())
    )
}

@Composable
fun ShimmerCircle(
    modifier: Modifier = Modifier,
    size: Dp = 56.dp
) {
    Box(
        modifier = modifier
            .size(size)
            .clip(RoundedCornerShape(size / 2))
            .background(shimmerBrush())
    )
}

@Composable
fun ShimmerLine(
    modifier: Modifier = Modifier,
    width: Dp = Dp.Unspecified,
    height: Dp = 16.dp
) {
    Box(
        modifier = modifier
            .then(if (width != Dp.Unspecified) Modifier.width(width) else Modifier.fillMaxWidth())
            .height(height)
            .clip(RoundedCornerShape(height / 2))
            .background(shimmerBrush())
    )
}

@Composable
fun ShimmerWorkerCard() {
    Column(
        modifier = Modifier
            .width(200.dp)
            .padding(8.dp)
    ) {
        ShimmerCard(height = 120.dp)
        Spacer(modifier = Modifier.height(8.dp))
        ShimmerLine(width = 140.dp, height = 16.dp)
        Spacer(modifier = Modifier.height(6.dp))
        ShimmerLine(width = 100.dp, height = 12.dp)
    }
}

@Composable
fun ShimmerCategoryItem() {
    Column(horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally) {
        ShimmerCircle(size = 56.dp)
        Spacer(modifier = Modifier.height(8.dp))
        ShimmerLine(width = 60.dp, height = 12.dp)
    }
}

@Composable
fun HomeShimmerLoader() {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        ShimmerLine(height = 48.dp)
        Spacer(modifier = Modifier.height(16.dp))
        ShimmerCard(height = 160.dp)
        Spacer(modifier = Modifier.height(24.dp))
        ShimmerLine(width = 120.dp, height = 20.dp)
        Spacer(modifier = Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            repeat(4) { ShimmerCategoryItem() }
        }
        Spacer(modifier = Modifier.height(24.dp))
        ShimmerLine(width = 160.dp, height = 20.dp)
        Spacer(modifier = Modifier.height(12.dp))
        repeat(3) {
            ShimmerCard(height = 100.dp)
            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}

@Composable
fun DashboardShimmerLoader() {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        ShimmerCard(height = 140.dp)
        Spacer(modifier = Modifier.height(16.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            ShimmerCard(modifier = Modifier.weight(1f), height = 80.dp)
            ShimmerCard(modifier = Modifier.weight(1f), height = 80.dp)
        }
        Spacer(modifier = Modifier.height(16.dp))
        ShimmerLine(width = 140.dp, height = 20.dp)
        Spacer(modifier = Modifier.height(12.dp))
        repeat(4) {
            ShimmerCard(height = 80.dp)
            Spacer(modifier = Modifier.height(10.dp))
        }
    }
}
