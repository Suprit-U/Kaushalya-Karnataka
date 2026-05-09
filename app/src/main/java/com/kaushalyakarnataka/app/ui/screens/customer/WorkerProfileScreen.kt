package com.kaushalyakarnataka.app.ui.screens.customer

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.graphics.*
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.kaushalyakarnataka.app.data.model.*
import com.kaushalyakarnataka.app.ui.components.*
import com.kaushalyakarnataka.app.ui.theme.*
import com.kaushalyakarnataka.app.utils.CurrencyUtils
import com.kaushalyakarnataka.app.utils.DateUtils
import com.kaushalyakarnataka.app.utils.UiState
import com.kaushalyakarnataka.app.viewmodel.AiSummaryViewModel
import com.kaushalyakarnataka.app.viewmodel.WorkerProfileViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun WorkerProfileScreen(
    workerId: String,
    onNavigateBack: () -> Unit,
    onNavigateToHire: (String) -> Unit,
    onNavigateToReviews: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: WorkerProfileViewModel = hiltViewModel(),
    aiSummaryViewModel: AiSummaryViewModel = hiltViewModel()
) {
    LaunchedEffect(workerId) { viewModel.loadWorkerProfile() }

    val context = LocalContext.current
    val profileState by viewModel.workerState.collectAsState()
    val servicesState by viewModel.servicesState.collectAsState()
    val reviewsState by viewModel.reviewsState.collectAsState()
    val portfolioState by viewModel.portfolioState.collectAsState()
    val summaryState by aiSummaryViewModel.summaryState.collectAsState()
    var showGallery by remember { mutableStateOf(false) }
    var selectedGalleryIndex by remember { mutableIntStateOf(0) }
    val portfolioItems = (portfolioState as? UiState.Success)?.data ?: emptyList()

    Scaffold(
        bottomBar = {
            if (profileState is UiState.Success) {
                val worker = (profileState as UiState.Success).data
                WorkerProfileBottomBar(
                    startingPrice = worker.displayBaseCharge,
                    onBook = { onNavigateToHire(workerId) }
                )
            }
        }
    ) { paddingValues ->
        when (val state = profileState) {
            is UiState.Loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Primary)
            }
            is UiState.Error -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(state.message, color = MaterialTheme.colorScheme.error)
                    Spacer(Modifier.height(12.dp))
                    Button(onClick = { viewModel.loadWorkerProfile() }) { Text("Retry") }
                }
            }
            is UiState.Success -> {
                val worker = state.data
                LazyColumn(modifier = modifier.fillMaxSize().padding(paddingValues)) {
                    item { WorkerHeroSection(worker = worker, onBack = onNavigateBack) }
                    item { WorkerStatsStrip(worker = worker) }
                    item {
                        WorkerActionRow(
                            phone = worker.phone,
                            email = worker.email,
                            onCall = {
                                if (worker.phone.isNotBlank()) {
                                    try {
                                        val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${worker.phone}"))
                                        context.startActivity(intent)
                                    } catch (e: Exception) {
                                        android.widget.Toast.makeText(context, "No dialer app found", android.widget.Toast.LENGTH_SHORT).show()
                                    }
                                }
                            },
                            onWhatsApp = {
                                if (worker.phone.isNotBlank()) {
                                    val cleaned = worker.phone.replace(Regex("[^0-9+]"), "")
                                    val numberWithCountry = if (cleaned.startsWith("+")) cleaned.drop(1) else if (cleaned.startsWith("91") && cleaned.length == 12) cleaned else "91$cleaned"
                                    try {
                                        val intent = Intent(Intent.ACTION_VIEW,
                                            Uri.parse("https://wa.me/$numberWithCountry"))
                                        context.startActivity(intent)
                                    } catch (e: Exception) {
                                        try {
                                            val intent = Intent(Intent.ACTION_VIEW,
                                                Uri.parse("https://api.whatsapp.com/send?phone=$numberWithCountry"))
                                            context.startActivity(intent)
                                        } catch (ex: Exception) {
                                            android.widget.Toast.makeText(context, "WhatsApp not installed", android.widget.Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                }
                            },
                            onEmail = {
                                if (worker.email.isNotBlank()) {
                                    try {
                                        val intent = Intent(Intent.ACTION_SENDTO).apply {
                                            data = Uri.parse("mailto:${worker.email}")
                                            putExtra(Intent.EXTRA_SUBJECT, "Service Inquiry")
                                        }
                                        context.startActivity(intent)
                                    } catch (e: Exception) { /* no email client */ }
                                }
                            },
                            onBook = { onNavigateToHire(workerId) }
                        )
                    }

                    if (worker.isVerified) {
                        item { VerificationBadge() }
                    }

                    item {
                        ProfileSection(title = "About Me") {
                            Text(
                                worker.bio.ifBlank { "No bio added yet." },
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    when (val sv = servicesState) {
                        is UiState.Success -> if (sv.data.isNotEmpty()) {
                            item {
                                ProfileSection(title = "Services & Pricing") {
                                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                        sv.data.forEach { service ->
                                            ServicePricingRow(service = service)
                                        }
                                    }
                                }
                            }
                        }
                        else -> {}
                    }

                    when (val pv = portfolioState) {
                        is UiState.Success -> if (pv.data.isNotEmpty()) {
                            item {
                                ProfileSection(title = "Portfolio") {
                                    PortfolioMiniGrid(
                                        items = pv.data.take(6),
                                        onImageClick = { index ->
                                            selectedGalleryIndex = index
                                            showGallery = true
                                        }
                                    )
                                }
                            }
                        }
                        else -> {}
                    }

                    when (val rv = reviewsState) {
                        is UiState.Success -> {
                            if (rv.data.isNotEmpty()) {
                                item {
                                    // Trigger AI summary generation when reviews are loaded
                                    LaunchedEffect(rv.data) {
                                        aiSummaryViewModel.generateSummary(workerId, rv.data)
                                    }
                                    ProfileSection(title = "Reviews") {
                                        // Rating summary
                                        val avg = rv.data.map { it.rating }.average()
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.padding(bottom = 12.dp)
                                        ) {
                                            Text(
                                                String.format("%.1f", avg),
                                                style = MaterialTheme.typography.headlineMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = Primary
                                            )
                                            Spacer(Modifier.width(8.dp))
                                            Column {
                                                Row {
                                                    repeat(5) { i ->
                                                        Icon(
                                                            if (i < avg.toInt()) Icons.Filled.Star else Icons.Outlined.Star,
                                                            null, tint = Warning, modifier = Modifier.size(16.dp)
                                                        )
                                                    }
                                                }
                                                Text("${rv.data.size} reviews",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                                            }
                                        }

                                        // AI Review Summary card
                                        AiReviewSummaryCard(
                                            state = summaryState,
                                            onRetry = { aiSummaryViewModel.retry(workerId, rv.data) },
                                            modifier = Modifier.padding(bottom = 12.dp)
                                        )

                                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                            rv.data.take(3).forEach { review ->
                                                ReviewPreviewCard(review = review)
                                            }
                                            if (rv.data.size > 3) {
                                                OutlinedButton(
                                                    onClick = { onNavigateToReviews(workerId) },
                                                    modifier = Modifier.fillMaxWidth()
                                                ) {
                                                    Text("See all ${rv.data.size} reviews")
                                                }
                                            }
                                        }
                                    }
                                }
                            } else {
                                item {
                                    ProfileSection(title = "Reviews") {
                                        Text(
                                            "No reviews yet. Be the first to book and review!",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                        else -> {}
                    }

                    item { Spacer(Modifier.height(24.dp)) }
                }
            }
        }
    }

    if (showGallery && portfolioItems.isNotEmpty()) {
        PortfolioGalleryDialog(
            items = portfolioItems,
            initialIndex = selectedGalleryIndex,
            onDismiss = { showGallery = false }
        )
    }
}

@Composable
private fun WorkerHeroSection(worker: Worker, onBack: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(260.dp)
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFF0F2055), PrimaryDark, Primary, PrimaryLight)
                )
            )
    ) {
        Surface(
            onClick = onBack,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(16.dp)
                .size(40.dp),
            shape = CircleShape,
            color = Color.White.copy(alpha = 0.12f),
            shadowElevation = 0.dp
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = Color.White, modifier = Modifier.size(20.dp))
            }
        }

        Column(
            modifier = Modifier.align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box {
                AvatarComponent(imageUrl = worker.avatarUrl, name = worker.name, size = 96.dp)
                if (worker.isGovernmentCertified) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .offset((-4).dp, (-4).dp)
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(Success)
                            .border(2.5.dp, Color.White, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Verified, null, tint = Color.White, modifier = Modifier.size(16.dp))
                    }
                }
            }
            Spacer(Modifier.height(14.dp))
            Text(
                worker.name,
                style = MaterialTheme.typography.headlineSmall,
                color = Color.White,
                fontWeight = FontWeight.ExtraBold
            )
            Text(
                worker.role.ifBlank { worker.category.displayName },
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.8f)
            )
            Spacer(Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (worker.rating > 0 || worker.reviewCount > 0) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(Dimens.radiusFull))
                            .background(Color.White.copy(alpha = 0.15f))
                            .padding(horizontal = 12.dp, vertical = 5.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.Star, null, tint = Warning, modifier = Modifier.size(14.dp))
                            Spacer(Modifier.width(4.dp))
                            Text(
                                "${String.format("%.1f", worker.rating)} · ${worker.reviewCount} reviews",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
                if (worker.location.isNotBlank()) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(Dimens.radiusFull))
                            .background(Color.White.copy(alpha = 0.15f))
                            .padding(horizontal = 12.dp, vertical = 5.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.LocationOn, null, tint = Secondary, modifier = Modifier.size(14.dp))
                            Spacer(Modifier.width(4.dp))
                            Text(
                                " ${worker.location.take(20)}",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun WorkerStatsStrip(worker: Worker) {
    Surface(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp).offset(y = (-20).dp),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 8.dp
    ) {
        Row(modifier = Modifier.fillMaxWidth()) {
            listOf(
                Pair("${worker.reviewCount}", "Reviews"),
                Pair(worker.category.displayName, "Role"),
                Pair(if (worker.isAvailable) "Available" else "Busy", "Status"),
            ).forEachIndexed { index, (value, label) ->
                Column(
                    modifier = Modifier.weight(1f).padding(vertical = 14.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(value, style = MaterialTheme.typography.titleSmall, color = Primary, fontWeight = FontWeight.Bold)
                    Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                if (index < 2) HorizontalDivider(modifier = Modifier.width(1.dp).height(40.dp).padding(vertical = 10.dp), color = MaterialTheme.colorScheme.outlineVariant)
            }
        }
    }
}

@Composable
private fun WorkerActionRow(
    phone: String,
    email: String,
    onCall: () -> Unit,
    onWhatsApp: () -> Unit,
    onEmail: () -> Unit,
    onBook: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        if (phone.isNotBlank()) {
            OutlinedIconButton(onClick = onCall, modifier = Modifier.size(46.dp), shape = CircleShape) {
                Icon(Icons.Default.Call, contentDescription = "Call Worker", tint = Primary)
            }
            OutlinedIconButton(onClick = onWhatsApp, modifier = Modifier.size(46.dp), shape = CircleShape) {
                Icon(Icons.AutoMirrored.Filled.Chat, contentDescription = "WhatsApp", tint = Color(0xFF25D366))
            }
        }
        if (email.isNotBlank()) {
            OutlinedIconButton(onClick = onEmail, modifier = Modifier.size(46.dp), shape = CircleShape) {
                Icon(Icons.Default.Email, contentDescription = "Email Worker", tint = Primary)
            }
        }
        Button(
            onClick = onBook,
            modifier = Modifier.weight(1f).height(46.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Primary)
        ) {
            Icon(Icons.Default.CalendarMonth, null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(6.dp))
            Text("Book Now", fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun VerificationBadge() {
    Surface(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 8.dp),
        shape = RoundedCornerShape(12.dp),
        color = SuccessTint
    ) {
        Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.VerifiedUser, null, tint = Success, modifier = Modifier.size(24.dp))
            Spacer(Modifier.width(10.dp))
            Column {
                Text("Background Verified", style = MaterialTheme.typography.labelLarge, color = Success, fontWeight = FontWeight.Bold)
                Text("ID & license checked", style = MaterialTheme.typography.bodySmall, color = Success.copy(alpha = 0.8f))
            }
        }
    }
}

@Composable
private fun ProfileSection(title: String, content: @Composable () -> Unit) {
    Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(20.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(Brush.verticalGradient(listOf(Primary, PrimaryLight)))
            )
            Spacer(Modifier.width(10.dp))
            Text(
                title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = Text1
            )
        }
        Spacer(Modifier.height(12.dp))
        content()
    }
}

@Composable
private fun ServicePricingRow(service: Service) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(2.dp, RoundedCornerShape(14.dp), spotColor = Primary.copy(0.06f)),
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 0.dp,
        tonalElevation = 1.dp
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    service.name,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = Text1
                )
                Text(
                    service.estimatedDuration.displayLabel,
                    style = MaterialTheme.typography.bodySmall,
                    color = Text3
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                if (service.startingPrice > 0) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(PrimaryTint)
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            "~${CurrencyUtils.formatRupees(service.startingPrice)}",
                            style = MaterialTheme.typography.labelMedium,
                            color = Primary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(Modifier.height(4.dp))
                    Text(
                        service.pricingType.displayLabel,
                        style = MaterialTheme.typography.labelSmall,
                        color = Text4
                    )
                } else {
                    Text(
                        "Set by worker",
                        style = MaterialTheme.typography.labelSmall,
                        color = Text3,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

@Composable
private fun PortfolioMiniGrid(
    items: List<PortfolioItem>,
    onImageClick: (Int) -> Unit
) {
    val rows = items.chunked(3)
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        rows.forEachIndexed { rowIndex, row ->
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                row.forEachIndexed { colIndex, item ->
                    val index = rowIndex * 3 + colIndex
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .aspectRatio(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .clickable { onImageClick(index) }
                    ) {
                        if (item.photoUrl.isNotEmpty()) {
                            AsyncImage(
                                model = item.photoUrl,
                                contentDescription = item.caption,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        } else {
                            Icon(Icons.Default.Image, null, tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(0.4f), modifier = Modifier.align(Alignment.Center).size(28.dp))
                        }
                    }
                }
                repeat(3 - row.size) { Spacer(Modifier.weight(1f)) }
            }
        }
    }
}

@Composable
private fun ReviewPreviewCard(review: Review) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(2.dp, RoundedCornerShape(14.dp), spotColor = Warning.copy(0.06f)),
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 0.dp,
        tonalElevation = 1.dp
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                AvatarComponent(imageUrl = review.customerAvatarUrl, name = review.customerName, size = 40.dp)
                Spacer(Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        review.customerName,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = Text1
                    )
                    Text(
                        DateUtils.getRelativeTimeSpan(review.createdAt),
                        style = MaterialTheme.typography.labelSmall,
                        color = Text4
                    )
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(Warning.copy(0.1f))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.Star, null, tint = Warning, modifier = Modifier.size(14.dp))
                        Spacer(Modifier.width(2.dp))
                        Text(
                            review.rating.toString(),
                            style = MaterialTheme.typography.labelSmall,
                            color = Warning,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
            if (review.serviceType.isNotBlank()) {
                Spacer(Modifier.height(8.dp))
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(PrimaryTint)
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        review.serviceType,
                        style = MaterialTheme.typography.labelSmall,
                        color = Primary,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
            Spacer(Modifier.height(8.dp))
            Text(
                review.comment,
                style = MaterialTheme.typography.bodyMedium,
                color = Text3,
                maxLines = 3
            )
        }
    }
}

@Composable
private fun WorkerProfileBottomBar(startingPrice: Int, onBook: () -> Unit) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 8.dp,
        tonalElevation = 2.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Column {
                Text(
                    "Approx. starting from",
                    style = MaterialTheme.typography.labelSmall,
                    color = Text3
                )
                Text(
                    if (startingPrice > 0) "~${CurrencyUtils.formatRupees(startingPrice)}" else "Book service",
                    style = MaterialTheme.typography.titleLarge,
                    color = Primary,
                    fontWeight = FontWeight.ExtraBold
                )
            }
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(50.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(Brush.horizontalGradient(listOf(Primary, PrimaryLight)))
                    .clickable(onClick = onBook),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.CalendarMonth, null, tint = Color.White, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(
                        "Book Now",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.labelLarge,
                        color = Color.White
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun PortfolioGalleryDialog(
    items: List<PortfolioItem>,
    initialIndex: Int,
    onDismiss: () -> Unit
) {
    val pagerState = rememberPagerState(initialPage = initialIndex, pageCount = { items.size })

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.92f))
                .pointerInput(Unit) {
                    detectTapGestures(onTap = { onDismiss() })
                },
            contentAlignment = Alignment.Center
        ) {
            IconButton(
                onClick = onDismiss,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(16.dp)
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(Color.Black.copy(alpha = 0.4f))
            ) {
                Icon(Icons.Default.Close, null, tint = Color.White, modifier = Modifier.size(24.dp))
            }

            Text(
                "${pagerState.currentPage + 1} / ${items.size}",
                modifier = Modifier.align(Alignment.TopCenter).padding(top = 20.dp),
                color = Color.White.copy(alpha = 0.8f),
                style = MaterialTheme.typography.labelLarge
            )

            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize(),
                beyondViewportPageCount = 1
            ) { page ->
                var scale by remember { mutableFloatStateOf(1f) }
                var offsetX by remember { mutableFloatStateOf(0f) }
                var offsetY by remember { mutableFloatStateOf(0f) }

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .pointerInput(Unit) {
                            detectTransformGestures { _, pan, zoom, _ ->
                                scale = (scale * zoom).coerceIn(1f, 5f)
                                if (scale > 1f) {
                                    offsetX += pan.x
                                    offsetY += pan.y
                                } else {
                                    offsetX = 0f
                                    offsetY = 0f
                                }
                            }
                        }
                        .pointerInput(Unit) {
                            detectTapGestures(
                                onDoubleTap = {
                                    scale = if (scale > 1f) 1f else 3f
                                    if (scale == 1f) {
                                        offsetX = 0f
                                        offsetY = 0f
                                    }
                                }
                            )
                        },
                    contentAlignment = Alignment.Center
                ) {
                    AsyncImage(
                        model = items[page].photoUrl,
                        contentDescription = items[page].caption,
                        contentScale = ContentScale.Fit,
                        modifier = Modifier
                            .fillMaxSize()
                            .graphicsLayer {
                                scaleX = scale
                                scaleY = scale
                                translationX = offsetX
                                translationY = offsetY
                            }
                    )
                }
            }

            if (items[pagerState.currentPage].caption.isNotBlank()) {
                Surface(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(horizontal = 16.dp, vertical = 24.dp)
                        .fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    color = Color.Black.copy(alpha = 0.5f)
                ) {
                    Text(
                        items[pagerState.currentPage].caption,
                        modifier = Modifier.padding(12.dp),
                        color = Color.White,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}
