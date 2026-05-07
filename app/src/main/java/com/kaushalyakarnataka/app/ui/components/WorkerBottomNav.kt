package com.kaushalyakarnataka.app.ui.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateColorAsState
import androidx.compose.animation.core.spring
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.kaushalyakarnataka.app.ui.theme.Primary
import com.kaushalyakarnataka.app.ui.theme.PrimaryTint

/**
 * Worker-specific bottom navigation tabs.
 * Home/Explore tabs removed as per spec. Replaced with Dashboard/Services/Bookings/Profile.
 */
enum class WorkerNavDestination(
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
) {
    DASHBOARD("Dashboard", Icons.Filled.Dashboard, Icons.Outlined.Dashboard),
    SERVICES("Services", Icons.Filled.Handyman, Icons.Outlined.Handyman),
    BOOKINGS("Bookings", Icons.Filled.EventNote, Icons.Outlined.EventNote),
    PROFILE("Profile", Icons.Filled.Person, Icons.Outlined.Person)
}

@Composable
fun WorkerBottomNav(
    currentDestination: WorkerNavDestination,
    onNavigate: (WorkerNavDestination) -> Unit,
    modifier: Modifier = Modifier
) {
    NavigationBar(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 8.dp
    ) {
        WorkerNavDestination.values().forEach { destination ->
            val isSelected = currentDestination == destination
            val iconColor by animateColorAsState(
                targetValue = if (isSelected) Primary else MaterialTheme.colorScheme.onSurfaceVariant,
                animationSpec = spring(stiffness = Spring.StiffnessMedium),
                label = "worker_nav_color"
            )
            NavigationBarItem(
                selected = isSelected,
                onClick = { onNavigate(destination) },
                icon = {
                    Icon(
                        imageVector = if (isSelected) destination.selectedIcon else destination.unselectedIcon,
                        contentDescription = destination.title,
                        tint = iconColor
                    )
                },
                label = {
                    Text(
                        destination.title,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = Primary,
                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    selectedTextColor = Primary,
                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    indicatorColor = PrimaryTint
                )
            )
        }
    }
}
