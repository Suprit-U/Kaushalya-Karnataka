package com.kaushalyakarnataka.app.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.EventNote
import androidx.compose.material.icons.automirrored.outlined.EventNote
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.kaushalyakarnataka.app.ui.theme.*

/**
 * Worker-specific bottom navigation tabs with premium pill-style design.
 */
enum class WorkerNavDestination(
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
) {
    DASHBOARD("Dashboard", Icons.Filled.Dashboard, Icons.Outlined.Dashboard),
    SERVICES("Services", Icons.Filled.Handyman, Icons.Outlined.Handyman),
    BOOKINGS("Bookings", Icons.AutoMirrored.Filled.EventNote, Icons.AutoMirrored.Outlined.EventNote),
    PROFILE("Profile", Icons.Filled.Person, Icons.Outlined.Person)
}

@Composable
fun WorkerBottomNav(
    currentDestination: WorkerNavDestination,
    onNavigate: (WorkerNavDestination) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .height(Dimens.bottomNavHeight)
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .shadow(8.dp, RoundedCornerShape(Dimens.radiusFull), spotColor = Primary.copy(0.06f)),
        shape = RoundedCornerShape(Dimens.radiusFull),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            WorkerNavDestination.values().forEach { destination ->
                val isSelected = currentDestination == destination
                WorkerNavItem(
                    destination = destination,
                    isSelected = isSelected,
                    onClick = { onNavigate(destination) }
                )
            }
        }
    }
}

@Composable
private fun WorkerNavItem(
    destination: WorkerNavDestination,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.92f else if (isSelected) 1.06f else 1f,
        animationSpec = spring(stiffness = 400f, dampingRatio = 0.65f),
        label = "worker_nav_scale"
    )
    val iconColor by animateColorAsState(
        targetValue = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
        animationSpec = tween(300),
        label = "worker_nav_icon_color"
    )
    val pillWidth by animateDpAsState(
        targetValue = if (isSelected) 106.dp else 44.dp,
        animationSpec = spring(stiffness = 320f, dampingRatio = 0.75f),
        label = "worker_nav_pill_width"
    )
    val iconScale by animateFloatAsState(
        targetValue = if (isSelected) 1.15f else 1f,
        animationSpec = spring(stiffness = 400f, dampingRatio = 0.7f),
        label = "worker_nav_icon_scale"
    )

    Box(
        modifier = Modifier
            .scale(scale)
            .width(pillWidth)
            .height(40.dp)
            .clip(RoundedCornerShape(Dimens.radiusFull))
            .background(
                brush = if (isSelected) Brush.horizontalGradient(
                    listOf(Primary, PrimaryLight)
                ) else Brush.horizontalGradient(listOf(Color.Transparent, Color.Transparent))
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = if (isSelected) destination.selectedIcon else destination.unselectedIcon,
                contentDescription = destination.title,
                tint = iconColor,
                modifier = Modifier
                    .size(22.dp)
                    .graphicsLayer {
                        scaleX = iconScale
                        scaleY = iconScale
                    }
            )
            AnimatedVisibility(
                visible = isSelected,
                enter = expandHorizontally(animationSpec = spring(stiffness = 400f)) + fadeIn(animationSpec = tween(200)),
                exit = shrinkHorizontally(animationSpec = spring(stiffness = 500f)) + fadeOut(animationSpec = tween(150))
            ) {
                Row {
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = destination.title,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }
    }
}
