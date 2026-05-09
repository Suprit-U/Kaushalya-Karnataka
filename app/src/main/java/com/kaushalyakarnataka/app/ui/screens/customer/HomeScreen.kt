package com.kaushalyakarnataka.app.ui.screens.customer

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import kotlinx.coroutines.launch
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import androidx.hilt.navigation.compose.hiltViewModel
import com.kaushalyakarnataka.app.data.model.*
import com.kaushalyakarnataka.app.ui.components.*
import com.kaushalyakarnataka.app.ui.components.animations.ShimmerCard
import com.kaushalyakarnataka.app.ui.theme.*
import com.kaushalyakarnataka.app.utils.CurrencyUtils
import com.kaushalyakarnataka.app.utils.DateUtils
import com.kaushalyakarnataka.app.utils.UiState
import com.kaushalyakarnataka.app.viewmodel.HomeViewModel
import com.kaushalyakarnataka.app.viewmodel.NotificationViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToSearch: (String?) -> Unit,
    onNavigateToCategory: (ServiceCategory) -> Unit,
    onNavigateToWorkerProfile: (String) -> Unit,
    onNavigateBottomBar: (NavDestination) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = hiltViewModel(),
    notifViewModel: NotificationViewModel = hiltViewModel()
) {
    val topWorkersState by viewModel.topWorkersState.collectAsState()
    val themeState = LocalThemeState.current
    val scrollState = rememberLazyListState()
    val unreadCount by notifViewModel.unreadCount.collectAsState()
    val notifications by notifViewModel.notifications.collectAsState()
    var showNotifications by remember { mutableStateOf(false) }

    Scaffold(
        bottomBar = {
            KaushalyaBottomNav(
                currentDestination = NavDestination.HOME,
                onNavigate = onNavigateBottomBar
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        LazyColumn(
            state = scrollState,
            modifier = modifier.fillMaxSize().padding(paddingValues),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            // ── Hero Header ──
            item {
                val scope = rememberCoroutineScope()
                HomeHeroHeader(
                    onSearchClick = { onNavigateToSearch(null) },
                    onThemeToggle = { scope.launch { themeState.toggle() } },
                    isDark = themeState.isDark,
                    unreadCount = unreadCount,
                    onNotificationsClick = { showNotifications = true }
                )
            }

            // ── Promo Banner ──
            item { PromoBannerCarousel() }

            // ── Category Section ──
            item {
                SectionHeader(title = "What do you need?", modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp))
                HomeCategoryGrid(onCategoryClick = onNavigateToCategory)
                Spacer(Modifier.height(4.dp))
            }

            // ── Top Rated Workers Carousel ──
            item {
                SectionHeader(
                    title = "Top Rated Experts",
                    actionText = "See all",
                    onAction = { onNavigateToSearch(null) },
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }

            item {
                when (val state = topWorkersState) {
                    is UiState.Loading -> {
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            contentPadding = PaddingValues(horizontal = 16.dp)
                        ) {
                            items(3) { WorkerCardSkeleton(compact = true) }
                        }
                    }
                    is UiState.Success -> {
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp)
                        ) {
                            items(state.data) { worker ->
                                TopWorkerCard(
                                    worker = worker,
                                    onClick = { onNavigateToWorkerProfile(worker.uid) }
                                )
                            }
                        }
                    }
                    is UiState.Error -> { /* silent */ }
                }
                Spacer(Modifier.height(16.dp))
            }

            // ── All Workers List ──
            item {
                SectionHeader(
                    title = "All Experts Near You",
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }

            when (val state = topWorkersState) {
                is UiState.Loading -> items(3) { WorkerCardSkeleton() }
                is UiState.Success -> items(state.data) { worker ->
                    WorkerListCard(
                        worker = worker,
                        onClick = { onNavigateToWorkerProfile(worker.uid) },
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                    )
                }
                else -> {}
            }
        }
    }

    if (showNotifications) {
        HomeNotificationsDialog(
            notificationsState = notifications,
            onDismiss = { showNotifications = false },
            onMarkAllRead = { notifViewModel.markAllRead() }
        )
    }
}

@Composable
private fun HomeNotificationsDialog(
    notificationsState: UiState<List<com.kaushalyakarnataka.app.data.model.AppNotification>>,
    onDismiss: () -> Unit,
    onMarkAllRead: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Notifications") },
        text = {
            when (notificationsState) {
                is UiState.Loading -> Box(Modifier.fillMaxWidth().height(80.dp), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = Primary) }
                is UiState.Error -> Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp)) {
                    Icon(Icons.Outlined.NotificationsOff, null, tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(0.4f), modifier = Modifier.size(40.dp))
                    Spacer(Modifier.height(8.dp))
                    Text("Could not load notifications", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                is UiState.Success -> {
                    if (notificationsState.data.isEmpty()) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp)) {
                            Icon(Icons.Outlined.Notifications, null, tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(0.4f), modifier = Modifier.size(48.dp))
                            Spacer(Modifier.height(8.dp))
                            Text("No notifications yet", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            notificationsState.data.take(5).forEach { notification ->
                                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                    Box(Modifier.size(32.dp).clip(CircleShape).background(if (notification.isRead) MaterialTheme.colorScheme.surfaceVariant else PrimaryTint), contentAlignment = Alignment.Center) {
                                        Icon(if (notification.isRead) Icons.Default.Info else Icons.Default.Notifications, null, tint = Primary, modifier = Modifier.size(16.dp))
                                    }
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(notification.title, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                                        Text(notification.message, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 2)
                                        Text(DateUtils.getRelativeTimeSpan(notification.createdAt), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(0.7f))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = { Button(onClick = { onMarkAllRead(); onDismiss() }) { Text("Mark Read") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Close") } }
    )
}

@Composable
private fun HomeHeroHeader(
    onSearchClick: () -> Unit,
    onThemeToggle: () -> Unit,
    onNotificationsClick: () -> Unit,
    unreadCount: Int,
    isDark: Boolean
) {
    // Single shared infinite transition for all hero animations
    val infiniteTransition = rememberInfiniteTransition(label = "hero_float")
    val orb1 by infiniteTransition.animateFloat(
        initialValue = -10f, targetValue = 10f,
        animationSpec = infiniteRepeatable(tween(7000, easing = LinearEasing), RepeatMode.Reverse),
        label = "orb1"
    )
    val orb2 by infiniteTransition.animateFloat(
        initialValue = 8f, targetValue = -8f,
        animationSpec = infiniteRepeatable(tween(6000, easing = LinearEasing), RepeatMode.Reverse),
        label = "orb2"
    )
    val waveY by infiniteTransition.animateFloat(
        initialValue = -3f, targetValue = 3f,
        animationSpec = infiniteRepeatable(tween(4000, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "wave_y"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(Primary, PrimaryDark, PrimaryLight)
                ),
                shape = RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp)
            )
            .padding(top = 20.dp, bottom = 32.dp)
            .padding(horizontal = 20.dp)
    ) {
        // Floating decorative orbs
        Box(
            modifier = Modifier
                .size(100.dp)
                .offset(x = (-20).dp, y = (10 + orb1).dp)
                .clip(CircleShape)
                .background(Color.White.copy(0.06f))
                .align(Alignment.TopStart)
        )
        Box(
            modifier = Modifier
                .size(60.dp)
                .offset(x = (0).dp, y = (60 + orb2).dp)
                .clip(CircleShape)
                .background(Color.White.copy(0.04f))
                .align(Alignment.TopEnd)
        )

        Column {
            // Top row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Namaste 👋",
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color.White.copy(alpha = 0.85f),
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "Find Your Expert",
                        style = MaterialTheme.typography.headlineMedium,
                        color = Color.White,
                        fontWeight = FontWeight.ExtraBold,
                        modifier = Modifier.graphicsLayer { translationY = waveY }
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    // Theme toggle
                    Surface(
                        onClick = onThemeToggle,
                        modifier = Modifier.size(42.dp),
                        shape = CircleShape,
                        color = Color.White.copy(alpha = 0.12f),
                        shadowElevation = 0.dp
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = if (isDark) Icons.Filled.LightMode else Icons.Filled.DarkMode,
                                contentDescription = "Toggle theme",
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                    // Notification
                    Surface(
                        onClick = onNotificationsClick,
                        modifier = Modifier.size(42.dp),
                        shape = CircleShape,
                        color = Color.White.copy(alpha = 0.12f),
                        shadowElevation = 0.dp
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Outlined.Notifications,
                                contentDescription = "Notifications",
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                            if (unreadCount > 0) {
                                val pulse by rememberInfiniteTransition(label = "pulse").animateFloat(
                                    initialValue = 0.7f, targetValue = 1.1f,
                                    animationSpec = infiniteRepeatable(
                                        animation = tween(800, easing = FastOutSlowInEasing),
                                        repeatMode = RepeatMode.Reverse
                                    ), label = "pulse"
                                )
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .offset((-2).dp, 2.dp)
                                        .size(18.dp)
                                        .graphicsLayer { scaleX = pulse; scaleY = pulse }
                                        .clip(CircleShape)
                                        .background(Error),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        if (unreadCount > 9) "9+" else unreadCount.toString(),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color.White,
                                        fontSize = 8.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            // Premium search bar with glassmorphism
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(12.dp, RoundedCornerShape(Dimens.radiusMd), spotColor = Color.Black.copy(0.15f))
                    .clickable(onClick = onSearchClick),
                shape = RoundedCornerShape(Dimens.radiusMd),
                color = Color.White.copy(alpha = 0.95f),
                shadowElevation = 0.dp
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 18.dp, vertical = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Primary.copy(0.08f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Search, contentDescription = null, tint = Primary, modifier = Modifier.size(18.dp))
                    }
                    Text(
                        text = "Search for electrician, plumber…",
                        style = MaterialTheme.typography.bodyLarge,
                        color = Text4
                    )
                }
            }
        }
    }
}

@Composable
private fun PromoBannerCard(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(
                Brush.linearGradient(listOf(Secondary, SecondaryDark))
            )
            .padding(20.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text("₹100 OFF", style = MaterialTheme.typography.headlineLarge, color = Color.White, fontWeight = FontWeight.ExtraBold)
                Text("Your first booking", style = MaterialTheme.typography.bodyMedium, color = Color.White.copy(alpha = 0.9f))
                Text("Code: KAUSHAL100", style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.7f))
            }
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = Color.White.copy(alpha = 0.2f),
                modifier = Modifier.clickable {}
            ) {
                Text(
                    "Claim", modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    style = MaterialTheme.typography.labelLarge, color = Color.White
                )
            }
        }
    }
}

@Composable
private fun PromoBannerCarousel() {
    val banners = listOf(
        PromoCardData("Rs. 100 OFF", "First booking discount", "KAUSHAL100", Secondary, SecondaryDark),
        PromoCardData("Trusted nearby", "Verified workers around you", "Book today", Primary, PrimaryLight),
        PromoCardData("Fast AC repair", "Same-day cooling fixes", "From top-rated techs", Color(0xFF0E7490), Color(0xFF0891B2)),
        PromoCardData("Weekend cleaning", "Fresh home, flexible slots", "Limited slots", Color(0xFF047857), Color(0xFF10B981)),
        PromoCardData("Festival offers", "Painter and cleaning deals", "Season special", Color(0xFF9D174D), Color(0xFFDB2777))
    )
    val listState = rememberLazyListState()
    // Auto-scroll effect
    LaunchedEffect(Unit) {
        while (true) {
            kotlinx.coroutines.delay(3000)
            val current = listState.firstVisibleItemIndex
            val next = if (current < banners.size - 1) current + 1 else 0
            listState.animateScrollToItem(next)
        }
    }
    LazyRow(
        state = listState,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
    ) {
        items(banners, key = { it.title }) { banner ->
            PromoMiniCard(banner = banner)
        }
    }
}

private data class PromoCardData(
    val title: String,
    val subtitle: String,
    val action: String,
    val start: Color,
    val end: Color
)

@Composable
private fun PromoMiniCard(banner: PromoCardData) {
    Box(
        modifier = Modifier
            .width(280.dp)
            .height(132.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(Brush.linearGradient(listOf(banner.start, banner.end)))
            .padding(18.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.SpaceBetween) {
            Column {
                Text(banner.title, style = MaterialTheme.typography.titleLarge, color = Color.White, fontWeight = FontWeight.ExtraBold)
                Text(banner.subtitle, style = MaterialTheme.typography.bodyMedium, color = Color.White.copy(alpha = 0.88f))
            }
            Surface(shape = RoundedCornerShape(18.dp), color = Color.White.copy(alpha = 0.18f)) {
                Text(
                    banner.action,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    style = MaterialTheme.typography.labelMedium,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun HomeCategoryGrid(onCategoryClick: (ServiceCategory) -> Unit) {
    val cats = ServiceCategory.entries
        .filter { it != ServiceCategory.OTHER }
        .map { category ->
            val (icon, iconColor, bgColor) = when (category) {
                ServiceCategory.ELECTRICIAN -> Triple(Icons.Filled.ElectricalServices, Color(0xFF1E3A8A), Color(0xFFDBEAFE))
                ServiceCategory.PLUMBER -> Triple(Icons.Filled.Plumbing, Color(0xFF065F46), Color(0xFFD1FAE5))
                ServiceCategory.CARPENTER -> Triple(Icons.Filled.Carpenter, Color(0xFF92400E), Color(0xFFFEF3C7))
                ServiceCategory.PAINTER -> Triple(Icons.Filled.FormatPaint, Color(0xFF9D174D), Color(0xFFFCE7F3))
                ServiceCategory.CLEANER -> Triple(Icons.Filled.CleaningServices, Color(0xFF5B21B6), Color(0xFFEDE9FE))
                ServiceCategory.AC_TECH -> Triple(Icons.Filled.AcUnit, Color(0xFF1E40AF), Color(0xFFDBEAFE))
                ServiceCategory.GARDENER -> Triple(Icons.Filled.Grass, Color(0xFF065F46), Color(0xFFD1FAE5))
                ServiceCategory.MECHANIC -> Triple(Icons.Filled.Handyman, Color(0xFF7C2D12), Color(0xFFFFEDD5))
                else -> Triple(Icons.Filled.MoreHoriz, Color(0xFF1E3A8A), Color(0xFFEFF6FF))
            }
            CategoryItem(category, icon, iconColor, bgColor)
        }

    val rows = cats.chunked(4)
    Column(modifier = Modifier.padding(horizontal = 12.dp)) {
        rows.forEach { row ->
            Row(modifier = Modifier.fillMaxWidth()) {
                row.forEach { cat ->
                    Box(modifier = Modifier.weight(1f)) {
                        HomeCategoryItem(cat, onClick = { onCategoryClick(cat.category) })
                    }
                }
                repeat(4 - row.size) { Spacer(Modifier.weight(1f)) }
            }
        }
    }
}

private data class CategoryItem(
    val category: ServiceCategory,
    val icon: ImageVector,
    val iconColor: Color,
    val bgColor: Color
)

@Composable
private fun HomeCategoryItem(item: CategoryItem, onClick: () -> Unit) {
    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.92f else 1f,
        animationSpec = tween(150),
        label = "cat_scale"
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .scale(scale)
            .clickable(onClick = onClick)
            .padding(horizontal = 4.dp, vertical = 8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .shadow(4.dp, RoundedCornerShape(20.dp), spotColor = item.iconColor.copy(0.15f))
                .clip(RoundedCornerShape(20.dp))
                .background(item.bgColor),
            contentAlignment = Alignment.Center
        ) {
            Icon(item.icon, null, tint = item.iconColor, modifier = Modifier.size(28.dp))
        }
        Spacer(Modifier.height(8.dp))
        Text(
            text = item.category.displayName,
            style = MaterialTheme.typography.labelMedium,
            color = Text2,
            maxLines = 1,
            fontWeight = FontWeight.Medium
        )
    }
}

// ── Top Worker Horizontal Card ───────────────────────────────
@Composable
fun TopWorkerCard(worker: Worker, onClick: () -> Unit) {
    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.96f else 1f,
        animationSpec = tween(120),
        label = "top_worker_scale"
    )

    Surface(
        modifier = Modifier
            .width(190.dp)
            .scale(scale)
            .clickable(onClick = onClick)
            .shadow(6.dp, RoundedCornerShape(20.dp), spotColor = Primary.copy(0.1f)),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 0.dp,
        tonalElevation = 1.dp
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(110.dp)
                    .background(Brush.linearGradient(listOf(Primary, PrimaryLight))),
                contentAlignment = Alignment.Center
            ) {
                AvatarComponent(imageUrl = worker.avatarUrl, name = worker.name, size = 68.dp)
                if (worker.rating > 0 && worker.reviewCount > 0) {
                    Surface(
                        modifier = Modifier.align(Alignment.TopEnd).padding(8.dp),
                        shape = RoundedCornerShape(10.dp),
                        color = Color(0xB3000000)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(Icons.Filled.Star, null, tint = StarColor, modifier = Modifier.size(12.dp))
                            Text(
                                String.format("%.1f", worker.rating),
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    worker.name,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    color = Text1
                )
                Text(
                    worker.category.displayName,
                    style = MaterialTheme.typography.labelSmall,
                    color = Text3
                )
                Spacer(Modifier.height(10.dp))
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val priceText = if (worker.displayBaseCharge > 0) "~${CurrencyUtils.formatRupees(worker.displayBaseCharge)}" else "Book Now"
                    Text(
                        priceText,
                        style = MaterialTheme.typography.labelMedium,
                        color = Primary,
                        fontWeight = FontWeight.Bold
                    )
                    if (worker.distanceKm > 0) {
                        Text(
                            "${worker.distanceKm} km",
                            style = MaterialTheme.typography.labelSmall,
                            color = Text4
                        )
                    }
                }
                Spacer(Modifier.height(10.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(34.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(Brush.horizontalGradient(listOf(Primary, PrimaryLight)))
                        .clickable(onClick = onClick),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "Book Now",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

// ── Worker List Card ──────────────────────────────────────────
@Composable
fun WorkerListCard(worker: Worker, onClick: () -> Unit, modifier: Modifier = Modifier) {
    WorkerCard(worker = worker, onClick = onClick, modifier = modifier)
}

@Composable
fun SectionHeader(
    title: String,
    actionText: String? = null,
    onAction: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(22.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(Brush.verticalGradient(listOf(Primary, PrimaryLight)))
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = Text1
            )
        }
        if (actionText != null && onAction != null) {
            TextButton(onClick = onAction) {
                Text(
                    actionText,
                    style = MaterialTheme.typography.labelMedium,
                    color = Primary,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

// Skeleton compact
@Composable
fun WorkerCardSkeleton(compact: Boolean = false) {
    if (compact) {
        ShimmerCard(
            modifier = Modifier.width(190.dp),
            height = 210.dp,
            shape = RoundedCornerShape(20.dp)
        )
    } else {
        com.kaushalyakarnataka.app.ui.components.animations.ShimmerCard(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
            height = 100.dp,
            shape = RoundedCornerShape(16.dp)
        )
    }
}
