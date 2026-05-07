package com.kaushalyakarnataka.app.ui.screens.customer

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import androidx.hilt.navigation.compose.hiltViewModel
import com.kaushalyakarnataka.app.data.model.*
import com.kaushalyakarnataka.app.ui.components.*
import com.kaushalyakarnataka.app.ui.theme.*
import com.kaushalyakarnataka.app.utils.CurrencyUtils
import com.kaushalyakarnataka.app.utils.UiState
import com.kaushalyakarnataka.app.viewmodel.HomeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToSearch: (String?) -> Unit,
    onNavigateToCategory: (ServiceCategory) -> Unit,
    onNavigateToWorkerProfile: (String) -> Unit,
    onNavigateBottomBar: (NavDestination) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val topWorkersState by viewModel.topWorkersState.collectAsState()
    val themeState = LocalThemeState.current
    val scrollState = rememberLazyListState()

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
                HomeHeroHeader(
                    onSearchClick = { onNavigateToSearch(null) },
                    onThemeToggle = { themeState.toggle() },
                    isDark = themeState.isDark
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
}

@Composable
private fun HomeHeroHeader(
    onSearchClick: () -> Unit,
    onThemeToggle: () -> Unit,
    isDark: Boolean
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(Primary, PrimaryLight)
                ),
                shape = RoundedCornerShape(bottomStart = 28.dp, bottomEnd = 28.dp)
            )
            .padding(top = 16.dp, bottom = 28.dp)
            .padding(horizontal = 20.dp)
    ) {
        Column {
            // Top row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Namaskara 👋",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                    Text(
                        text = "Find Your Expert",
                        style = MaterialTheme.typography.headlineMedium,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    // Theme toggle
                    IconButton(
                        onClick = onThemeToggle,
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.15f))
                    ) {
                        Icon(
                            imageVector = if (isDark) Icons.Filled.LightMode else Icons.Filled.DarkMode,
                            contentDescription = "Toggle theme",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    // Notification
                    IconButton(
                        onClick = {},
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.15f))
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Notifications,
                            contentDescription = "Notifications",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            Spacer(Modifier.height(20.dp))

            // Search bar
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onSearchClick),
                shape = RoundedCornerShape(14.dp),
                color = Color.White,
                shadowElevation = 8.dp
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(Icons.Default.Search, contentDescription = null, tint = Primary, modifier = Modifier.size(20.dp))
                    Text(
                        text = "Search for electrician, plumber…",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
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
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
    ) {
        items(banners) { banner ->
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
    val cats = listOf(
        CategoryItem(ServiceCategory.ELECTRICIAN, Icons.Filled.ElectricalServices, Color(0xFF1E3A8A), Color(0xFFDBEAFE)),
        CategoryItem(ServiceCategory.PLUMBER, Icons.Filled.Plumbing, Color(0xFF065F46), Color(0xFFD1FAE5)),
        CategoryItem(ServiceCategory.CARPENTER, Icons.Filled.Carpenter, Color(0xFF92400E), Color(0xFFFEF3C7)),
        CategoryItem(ServiceCategory.PAINTER, Icons.Filled.FormatPaint, Color(0xFF9D174D), Color(0xFFFCE7F3)),
        CategoryItem(ServiceCategory.CLEANER, Icons.Filled.CleaningServices, Color(0xFF5B21B6), Color(0xFFEDE9FE)),
        CategoryItem(ServiceCategory.AC_TECH, Icons.Filled.AcUnit, Color(0xFF1E40AF), Color(0xFFDBEAFE)),
        CategoryItem(ServiceCategory.GARDENER, Icons.Filled.Grass, Color(0xFF065F46), Color(0xFFD1FAE5)),
        CategoryItem(ServiceCategory.OTHER, Icons.Filled.MoreHoriz, Color(0xFF1E3A8A), Color(0xFFEFF6FF)),
    )

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
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(horizontal = 4.dp, vertical = 8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(60.dp)
                .clip(RoundedCornerShape(18.dp))
                .background(item.bgColor),
            contentAlignment = Alignment.Center
        ) {
            Icon(item.icon, null, tint = item.iconColor, modifier = Modifier.size(28.dp))
        }
        Spacer(Modifier.height(6.dp))
        Text(
            text = item.category.displayName,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1
        )
    }
}

// ── Top Worker Horizontal Card ───────────────────────────────
@Composable
fun TopWorkerCard(worker: Worker, onClick: () -> Unit) {
    Surface(
        modifier = Modifier.width(180.dp).clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 4.dp,
        tonalElevation = 2.dp
    ) {
        Column {
            // Colored banner
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
                    .background(Brush.linearGradient(listOf(Primary, PrimaryLight))),
                contentAlignment = Alignment.Center
            ) {
                AvatarComponent(imageUrl = worker.avatarUrl, name = worker.name, size = 64.dp)
                // Rating badge
                if (worker.rating > 0 && worker.reviewCount > 0) {
                    Surface(
                        modifier = Modifier.align(Alignment.TopEnd).padding(6.dp),
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0x99000000)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(3.dp)
                        ) {
                            Icon(Icons.Filled.Star, null, tint = Color(0xFFFCD34D), modifier = Modifier.size(12.dp))
                            Text(String.format("%.1f", worker.rating), style = MaterialTheme.typography.labelSmall, color = Color.White)
                        }
                    }
                }
            }
            Column(modifier = Modifier.padding(12.dp)) {
                Text(worker.name, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, maxLines = 1)
                Text(worker.category.displayName, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                    val priceText = if (worker.displayBaseCharge > 0) "~${CurrencyUtils.formatRupees(worker.displayBaseCharge)}" else "Book Now"
                    Text(
                        priceText,
                        style = MaterialTheme.typography.labelMedium,
                        color = Primary,
                        fontWeight = FontWeight.Bold
                    )
                    if (worker.distanceKm > 0) {
                        Text("${worker.distanceKm} km", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                Spacer(Modifier.height(8.dp))
                Button(
                    onClick = onClick,
                    modifier = Modifier.fillMaxWidth().height(32.dp),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(0.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Primary)
                ) {
                    Text("Book", style = MaterialTheme.typography.labelSmall, color = Color.White)
                }
            }
        }
    }
}

// ── Worker List Card ──────────────────────────────────────────
@Composable
fun WorkerListCard(worker: Worker, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 2.dp
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            AvatarComponent(imageUrl = worker.avatarUrl, name = worker.name, size = 56.dp)
            Spacer(Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(worker.name, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                    if (worker.isVerified) {
                        Icon(Icons.Filled.Verified, null, tint = Primary, modifier = Modifier.size(16.dp))
                    }
                }
                Text(worker.category.displayName, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 4.dp)) {
                    if (worker.rating > 0 && worker.reviewCount > 0) {
                        Icon(Icons.Filled.Star, null, tint = Warning, modifier = Modifier.size(14.dp))
                        Text(" ${String.format("%.1f", worker.rating)} (${worker.reviewCount})", style = MaterialTheme.typography.labelSmall)
                    }
                    if (worker.distanceKm > 0) {
                        Spacer(Modifier.width(8.dp))
                        Text("• ${worker.distanceKm} km", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
            Column(horizontalAlignment = Alignment.End) {
                if (worker.displayBaseCharge > 0) {
                    Text(
                        "~${CurrencyUtils.formatRupees(worker.displayBaseCharge)}",
                        style = MaterialTheme.typography.titleSmall,
                        color = Primary,
                        fontWeight = FontWeight.Bold
                    )
                    Text("starting", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                } else {
                    Text(
                        "Book Now",
                        style = MaterialTheme.typography.labelSmall,
                        color = Primary,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun SectionHeader(
    title: String,
    actionText: String? = null,
    onAction: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Row(modifier = modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        if (actionText != null && onAction != null) {
            TextButton(onClick = onAction) {
                Text(actionText, style = MaterialTheme.typography.labelMedium, color = Primary)
            }
        }
    }
}

// Skeleton compact
@Composable
fun WorkerCardSkeleton(compact: Boolean = false) {
    if (compact) {
        Box(
            modifier = Modifier.width(180.dp).height(200.dp).clip(RoundedCornerShape(20.dp)).background(shimmerBrush())
        )
    } else {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp)
                .clip(RoundedCornerShape(16.dp)).background(shimmerBrush()).padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(Modifier.size(56.dp).clip(CircleShape).background(MaterialTheme.colorScheme.surfaceVariant))
            Spacer(Modifier.width(14.dp))
            Column {
                Box(Modifier.width(120.dp).height(14.dp).clip(RoundedCornerShape(4.dp)).background(MaterialTheme.colorScheme.surfaceVariant))
                Spacer(Modifier.height(6.dp))
                Box(Modifier.width(80.dp).height(10.dp).clip(RoundedCornerShape(4.dp)).background(MaterialTheme.colorScheme.surfaceVariant))
            }
        }
    }
}
