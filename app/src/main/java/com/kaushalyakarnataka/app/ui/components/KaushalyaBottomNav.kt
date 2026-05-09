package com.kaushalyakarnataka.app.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
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
import androidx.compose.runtime.*
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

enum class NavDestination(
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
) {
    HOME("Home", Icons.Filled.Home, Icons.Outlined.Home),
    SEARCH("Explore", Icons.Filled.Search, Icons.Outlined.Search),
    BOOKINGS("Bookings", Icons.AutoMirrored.Filled.EventNote, Icons.AutoMirrored.Outlined.EventNote),
    PROFILE("Profile", Icons.Filled.Person, Icons.Outlined.Person)
}

@Composable
fun KaushalyaBottomNav(
    currentDestination: NavDestination,
    onNavigate: (NavDestination) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .height(Dimens.bottomNavHeight)
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .shadow(
                elevation = 12.dp,
                shape = RoundedCornerShape(Dimens.radiusFull),
                spotColor = Primary.copy(0.08f),
                ambientColor = Primary.copy(0.04f)
            ),
        shape = RoundedCornerShape(Dimens.radiusFull),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 6.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            NavDestination.values().forEach { destination ->
                val isSelected = currentDestination == destination
                CustomerNavItem(
                    destination = destination,
                    isSelected = isSelected,
                    onClick = { onNavigate(destination) }
                )
            }
        }
    }
}

@Composable
private fun CustomerNavItem(
    destination: NavDestination,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.92f else if (isSelected) 1.06f else 1f,
        animationSpec = spring(stiffness = 400f, dampingRatio = 0.65f),
        label = "cust_nav_scale"
    )
    val iconColor by animateColorAsState(
        targetValue = if (isSelected) Color.White else Text3,
        animationSpec = tween(300),
        label = "cust_nav_icon_color"
    )
    val pillWidth by animateDpAsState(
        targetValue = if (isSelected) 106.dp else 44.dp,
        animationSpec = spring(stiffness = 320f, dampingRatio = 0.75f),
        label = "cust_nav_pill_width"
    )
    val iconRotation by animateFloatAsState(
        targetValue = if (isSelected) 0f else 0f,
        animationSpec = spring(stiffness = 300f, dampingRatio = 0.6f),
        label = "cust_nav_rotation"
    )
    val iconScale by animateFloatAsState(
        targetValue = if (isSelected) 1.15f else 1f,
        animationSpec = spring(stiffness = 400f, dampingRatio = 0.7f),
        label = "cust_nav_icon_scale"
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
                        rotationZ = iconRotation
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

// Keep old BottomNavBar alias for backwards compatibility
@Composable
fun BottomNavBar(
    currentDestination: NavDestination,
    onNavigate: (NavDestination) -> Unit,
    modifier: Modifier = Modifier
) = KaushalyaBottomNav(currentDestination, onNavigate, modifier)
