package com.kaushalyakarnataka.app.ui.screens.customer

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
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
import com.kaushalyakarnataka.app.viewmodel.WorkerProfileViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun WorkerProfileScreen(
    workerId: String,
    onNavigateBack: () -> Unit,
    onNavigateToHire: (String) -> Unit,
    onNavigateToReviews: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: WorkerProfileViewModel = hiltViewModel()
) {
    LaunchedEffect(workerId) { viewModel.loadWorkerProfile() }

    val context = LocalContext.current
    val profileState by viewModel.workerState.collectAsState()
    val servicesState by viewModel.servicesState.collectAsState()
    val reviewsState by viewModel.reviewsState.collectAsState()
    val portfolioState by viewModel.portfolioState.collectAsState()

    Scaffold(
        bottomBar = {
            if (profileState is UiState.Success) {
                val worker = (profileState as UiState.Success).data
                WorkerProfileBottomBar(
                    startingPrice = worker.pricePerHour,
                    onBook = { onNavigateToHire(workerId) }
                )
            }
        }
    ) { paddingValues ->
        when (val state = profileState) {
            is UiState.Loading -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Primary)
                }
            }
            is UiState.Error -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(state.message, color = MaterialTheme.colorScheme.error)
                }
            }
            is UiState.Success -> {
                val worker = state.data
                LazyColumn(
                    modifier = modifier.fillMaxSize().padding(paddingValues)
                ) {
                    item { WorkerHeroSection(worker = worker, onBack = onNavigateBack) }
                    item { WorkerStatsStrip(worker = worker) }
                    item {
                        WorkerActionRow(
                            phone = worker.phone,
                            onCall = {
                                if (worker.phone.isNotBlank()) {
                                    val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${worker.phone}"))
                                    context.startActivity(intent)
                                }
                            },
                            onWhatsApp = {
                                val number = worker.phone.replace(Regex("[^0-9]"), "")
                                try {
                                    val intent = Intent(Intent.ACTION_VIEW,
                                        Uri.parse("https://wa.me/91$number"))
                                    context.startActivity(intent)
                                } catch (e: Exception) {
                                    val intent = Intent(Intent.ACTION_VIEW,
                                        Uri.parse("https://wa.me/91$number"))
                                    context.startActivity(intent)
                                }
                            },
                            onBook = { onNavigateToHire(workerId) }
                        )
                    }

                    if (worker.isVerified) {
                        item { VerificationBadge() }
                    }

                    item {
                        ProfileSection(title = "About") {
                            Text(worker.bio.ifBlank { "Experienced ${worker.category.displayName} available for your service needs." },
                                style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }

                    if (worker.tags.isNotEmpty()) {
                        item {
                            ProfileSection(title = "Skills") {
                                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    worker.tags.forEach { tag ->
                                        Surface(shape = RoundedCornerShape(20.dp), color = PrimaryTint) {
                                            Text(tag, modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                                style = MaterialTheme.typography.labelSmall, color = Primary)
                                        }
                                    }
                                }
                            }
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
                                ProfileSection(title = "Recent Work") {
                                    PortfolioMiniGrid(items = pv.data.take(6))
                                }
                            }
                        }
                        else -> {}
                    }

                    when (val rv = reviewsState) {
                        is UiState.Success -> if (rv.data.isNotEmpty()) {
                            item {
                                ProfileSection(title = "Reviews") {
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
                        }
                        else -> {}
                    }

                    item { Spacer(Modifier.height(24.dp)) }
                }
            }
        }
    }
}

@Composable
private fun WorkerHeroSection(worker: Worker, onBack: () -> Unit) {
    Box(
        modifier = Modifier.fillMaxWidth().height(240.dp)
            .background(Brush.verticalGradient(listOf(PrimaryDark, PrimaryLight)))
    ) {
        IconButton(
            onClick = onBack,
            modifier = Modifier.align(Alignment.TopStart).padding(8.dp)
                .clip(CircleShape).background(Color.White.copy(alpha = 0.15f))
        ) {
            Icon(Icons.Default.ArrowBack, null, tint = Color.White)
        }

        Column(
            modifier = Modifier.align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box {
                AvatarComponent(imageUrl = worker.avatarUrl, name = worker.name, size = 90.dp)
                if (worker.isGovernmentCertified) {
                    Box(
                        modifier = Modifier.align(Alignment.BottomEnd)
                            .size(24.dp).clip(CircleShape).background(Primary)
                            .border(2.dp, Color.White, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Verified, null, tint = Color.White, modifier = Modifier.size(14.dp))
                    }
                }
            }
            Spacer(Modifier.height(12.dp))
            Text(worker.name, style = MaterialTheme.typography.titleLarge, color = Color.White, fontWeight = FontWeight.Bold)
            Text(worker.role.ifBlank { worker.category.displayName }, style = MaterialTheme.typography.bodyMedium, color = Color.White.copy(alpha = 0.8f))
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Surface(shape = RoundedCornerShape(20.dp), color = Color.White.copy(alpha = 0.15f)) {
                    Row(modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.Star, null, tint = Warning, modifier = Modifier.size(14.dp))
                        Text(" ${worker.rating} · ${worker.reviewCount} reviews", style = MaterialTheme.typography.labelSmall, color = Color.White)
                    }
                }
                if (worker.distanceKm > 0) {
                    Surface(shape = RoundedCornerShape(20.dp), color = Color.White.copy(alpha = 0.15f)) {
                        Row(modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.LocationOn, null, tint = Secondary, modifier = Modifier.size(14.dp))
                            Text(" ${worker.distanceKm} km", style = MaterialTheme.typography.labelSmall, color = Color.White)
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
                Triple("${worker.reviewCount}", "Reviews", Icons.Default.Reviews),
                Triple("${worker.experienceYears}y", "Experience", Icons.Default.WorkHistory),
                Triple("${worker.successRate}%", "Success", Icons.Default.Verified)
            ).forEachIndexed { index, (value, label, icon) ->
                Column(
                    modifier = Modifier.weight(1f).padding(vertical = 14.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(value, style = MaterialTheme.typography.titleMedium, color = Primary, fontWeight = FontWeight.Bold)
                    Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                if (index < 2) HorizontalDivider(modifier = Modifier.width(1.dp).height(40.dp).padding(vertical = 10.dp), color = MaterialTheme.colorScheme.outlineVariant)
            }
        }
    }
}

@Composable
private fun WorkerActionRow(phone: String, onCall: () -> Unit, onWhatsApp: () -> Unit, onBook: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        if (phone.isNotBlank()) {
            OutlinedIconButton(onClick = onCall, modifier = Modifier.size(46.dp), shape = CircleShape) {
                Icon(Icons.Default.Call, contentDescription = "Call Worker", tint = Primary)
            }
            OutlinedIconButton(onClick = onWhatsApp, modifier = Modifier.size(46.dp), shape = CircleShape) {
                Icon(Icons.Default.Chat, contentDescription = "WhatsApp Worker", tint = Primary)
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
                Text("ID, license & police clearance checked", style = MaterialTheme.typography.bodySmall, color = Success.copy(alpha = 0.8f))
            }
        }
    }
}

@Composable
private fun ProfileSection(title: String, content: @Composable () -> Unit) {
    Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp)) {
        Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(10.dp))
        content()
    }
}

@Composable
private fun ServicePricingRow(service: Service) {
    Surface(shape = RoundedCornerShape(12.dp), color = MaterialTheme.colorScheme.surfaceVariant, modifier = Modifier.fillMaxWidth()) {
        Row(modifier = Modifier.padding(14.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(service.name, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                Text(service.estimatedDuration.displayLabel, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(CurrencyUtils.formatRupees(service.startingPrice), style = MaterialTheme.typography.titleSmall, color = Primary, fontWeight = FontWeight.Bold)
                Text(service.pricingType.displayLabel, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun PortfolioMiniGrid(items: List<PortfolioItem>) {
    val rows = items.chunked(3)
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        rows.forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                row.forEach { item ->
                    Box(modifier = Modifier.weight(1f).aspectRatio(1f).clip(RoundedCornerShape(8.dp)).background(MaterialTheme.colorScheme.surfaceVariant)) {
                        if (item.photoUrl.isNotEmpty()) {
                            AsyncImage(model = item.photoUrl, contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
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
    Surface(shape = RoundedCornerShape(12.dp), color = MaterialTheme.colorScheme.surfaceVariant, modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                AvatarComponent(imageUrl = review.customerAvatarUrl, name = review.customerName, size = 36.dp)
                Spacer(Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(review.customerName, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
                    Text(DateUtils.getRelativeTimeSpan(review.createdAt), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Row {
                    repeat(review.rating) { Icon(Icons.Filled.Star, null, tint = Warning, modifier = Modifier.size(14.dp)) }
                }
            }
            Spacer(Modifier.height(8.dp))
            Text(review.comment, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 3)
        }
    }
}

@Composable
private fun WorkerProfileBottomBar(startingPrice: Int, onBook: () -> Unit) {
    Surface(color = MaterialTheme.colorScheme.surface, shadowElevation = 16.dp) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Column {
                Text("Starting from", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(CurrencyUtils.formatRupees(startingPrice), style = MaterialTheme.typography.titleLarge, color = Primary, fontWeight = FontWeight.ExtraBold)
            }
            Button(
                onClick = onBook,
                modifier = Modifier.weight(1f).height(50.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Primary)
            ) {
                Icon(Icons.Default.CalendarMonth, null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("Book Now", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelLarge)
            }
        }
    }
}
