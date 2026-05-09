package com.kaushalyakarnataka.app.ui.components.animations

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp

/* ── Fade + slide entrance for lazy items ── */
@Composable
fun <T> AnimatedListItem(
    index: Int,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val delay = index * 60
    val visible = remember { MutableTransitionState(false).apply { targetState = true } }
    AnimatedVisibility(
        visibleState = visible,
        enter = fadeIn(animationSpec = tween(300, delayMillis = delay)) +
                slideInVertically(animationSpec = tween(350, delayMillis = delay)) { it / 4 },
        exit = fadeOut(animationSpec = tween(150)),
        modifier = modifier
    ) {
        content()
    }
}

/* ── Animated counter for numbers ── */
@Composable
fun AnimatedCounter(
    target: Int,
    modifier: Modifier = Modifier,
    content: @Composable (String) -> Unit
) {
    var current by remember { mutableIntStateOf(0) }
    LaunchedEffect(target) {
        val diff = target - current
        val steps = 20.coerceAtLeast(kotlin.math.abs(diff))
        val increment = diff / steps
        repeat(steps) {
            current += increment
            kotlinx.coroutines.delay(16L)
        }
        current = target
    }
    content(current.toString())
}

/* ── Pulse animation for badges / live indicators ── */
@Composable
fun PulseDot(
    modifier: Modifier = Modifier,
    color: androidx.compose.ui.graphics.Color
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )
    val alpha by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 0.4f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_alpha"
    )
    Box(
        modifier = modifier
            .size(8.dp)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
                this.alpha = alpha
            }
            .background(color, androidx.compose.foundation.shape.CircleShape)
    )
}

/* ── Staggered children fade-in ── */
@Composable
fun StaggeredColumn(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(modifier = modifier) {
        content()
    }
}

/* ── Reusable crossfade between states ── */
@Composable
fun <T> SmoothStateCrossfade(
    targetState: T,
    modifier: Modifier = Modifier,
    content: @Composable (T) -> Unit
) {
    Crossfade(
        targetState = targetState,
        animationSpec = tween(350, easing = FastOutSlowInEasing),
        modifier = modifier,
        label = "state_crossfade"
    ) { state ->
        content(state)
    }
}

/* ── Floating animation for hero elements ── */
@Composable
fun FloatingElement(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "float")
    val offset by infiniteTransition.animateFloat(
        initialValue = -4f,
        targetValue = 4f,
        animationSpec = infiniteRepeatable(
            animation = tween(2500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "float_y"
    )
    Box(modifier = modifier.graphicsLayer { translationY = offset }) {
        content()
    }
}

/* ── Elastic press scale for tactile buttons/cards ── */
@Composable
fun ElasticPressEffect(
    targetScale: Float = 1f,
    pressedScale: Float = 0.94f,
    content: @Composable (Modifier) -> Unit
) {
    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isPressed) pressedScale else targetScale,
        animationSpec = spring(stiffness = 400f, dampingRatio = 0.35f),
        label = "elastic_press"
    )
    content(
        Modifier
            .scale(scale)
            .pointerInput(Unit) {
                awaitPointerEventScope {
                    while (true) {
                        val event = awaitPointerEvent()
                        isPressed = event.changes.any { it.pressed }
                    }
                }
            }
    )
}

/* ── Smooth shimmer sweep for premium loading states ── */
@Composable
fun ShimmerSweep(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "shimmer")
    val translateAnim by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmer_translate"
    )
    Box(modifier = modifier) {
        content()
    }
}
