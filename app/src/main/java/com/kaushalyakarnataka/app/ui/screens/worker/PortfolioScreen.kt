package com.kaushalyakarnataka.app.ui.screens.worker

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.kaushalyakarnataka.app.data.model.PortfolioItem
import com.kaushalyakarnataka.app.data.model.ServiceCategory
import com.kaushalyakarnataka.app.ui.components.AppTopBar
import com.kaushalyakarnataka.app.ui.theme.*
import com.kaushalyakarnataka.app.utils.UiState
import com.kaushalyakarnataka.app.viewmodel.PortfolioViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PortfolioScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: PortfolioViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val uploadState by viewModel.uploadState.collectAsState()
    val portfolioItems by viewModel.portfolioItems.collectAsState()

    var caption by remember { mutableStateOf("") }
    var selectedUri by remember { mutableStateOf<Uri?>(null) }
    var selectedCategory by remember { mutableStateOf(ServiceCategory.OTHER) }
    val snackbarHostState = remember { SnackbarHostState() }

    val imagePickerLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        selectedUri = uri
    }

    LaunchedEffect(uploadState) {
        when (val s = uploadState) {
            is UiState.Success -> {
                caption = ""
                selectedUri = null
                snackbarHostState.showSnackbar("Portfolio item uploaded!")
                viewModel.clearState()
            }
            is UiState.Error -> {
                snackbarHostState.showSnackbar(s.message)
            }
            else -> {}
        }
    }

    Scaffold(
        topBar = { AppTopBar(title = "My Portfolio", onBackClick = onNavigateBack) },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        LazyColumn(
            modifier = modifier.fillMaxSize().padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Upload section
            item {
                Surface(shape = RoundedCornerShape(16.dp), color = MaterialTheme.colorScheme.surface, shadowElevation = 2.dp, modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text("Add New Work Photo", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

                        // Image picker area
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(160.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (selectedUri != null) Color.Transparent else MaterialTheme.colorScheme.surfaceVariant)
                                .border(2.dp, if (selectedUri != null) Primary else MaterialTheme.colorScheme.outline, RoundedCornerShape(12.dp))
                                .clickable { imagePickerLauncher.launch("image/*") },
                            contentAlignment = Alignment.Center
                        ) {
                            if (selectedUri != null) {
                                AsyncImage(
                                    model = selectedUri,
                                    contentDescription = "Selected image",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(12.dp))
                                )
                                // Overlay change button
                                Surface(
                                    modifier = Modifier.align(Alignment.BottomEnd).padding(8.dp),
                                    shape = RoundedCornerShape(8.dp),
                                    color = Color.Black.copy(alpha = 0.6f)
                                ) {
                                    Row(modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                        Icon(Icons.Default.Edit, null, tint = Color.White, modifier = Modifier.size(14.dp))
                                        Text("Change", style = MaterialTheme.typography.labelSmall, color = Color.White)
                                    }
                                }
                            } else {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(Icons.Default.AddPhotoAlternate, null, tint = Primary, modifier = Modifier.size(48.dp))
                                    Spacer(Modifier.height(8.dp))
                                    Text("Tap to select an image", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text("from your gallery", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(0.6f))
                                }
                            }
                        }

                        OutlinedTextField(
                            value = caption,
                            onValueChange = { caption = it },
                            label = { Text("Caption (optional)") },
                            placeholder = { Text("Describe this work...") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )

                        if (uploadState is UiState.Loading) {
                            LinearProgressIndicator(modifier = Modifier.fillMaxWidth(), color = Primary)
                        }

                        Button(
                            onClick = {
                                if (selectedUri != null) {
                                    val bytes = context.contentResolver.openInputStream(selectedUri!!)?.readBytes()
                                    if (bytes != null) {
                                        viewModel.uploadPortfolioItem(bytes, caption, selectedCategory)
                                    }
                                }
                            },
                            enabled = selectedUri != null && uploadState !is UiState.Loading,
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = Primary),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.CloudUpload, null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(6.dp))
                            Text(if (uploadState is UiState.Loading) "Uploading..." else "Upload to Portfolio", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Portfolio stats
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Surface(modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp), color = PrimaryTint) {
                        Column(modifier = Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            val itemCount = (portfolioItems as? UiState.Success<List<PortfolioItem>>)?.data?.size ?: 0
                            Text("$itemCount", style = MaterialTheme.typography.titleLarge, color = Primary, fontWeight = FontWeight.ExtraBold)
                            Text("Photos", style = MaterialTheme.typography.labelSmall, color = Primary.copy(0.7f))
                        }
                    }
                    Surface(modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp), color = SuccessTint) {
                        Column(modifier = Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("4.9★", style = MaterialTheme.typography.titleLarge, color = Success, fontWeight = FontWeight.ExtraBold)
                            Text("Rating", style = MaterialTheme.typography.labelSmall, color = Success.copy(0.7f))
                        }
                    }
                }
            }

            // Portfolio gallery header
            item {
                Text("Your Work Gallery", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }

            // Portfolio items
            when (val pState = portfolioItems) {
                is UiState.Loading -> item {
                    Box(Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = Primary)
                    }
                }
                is UiState.Success<List<PortfolioItem>> -> {
                    if (pState.data.isEmpty()) {
                        item {
                            Surface(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp), color = MaterialTheme.colorScheme.surfaceVariant) {
                                Column(modifier = Modifier.padding(32.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(Icons.Default.PhotoLibrary, null, tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(0.4f), modifier = Modifier.size(56.dp))
                                    Spacer(Modifier.height(8.dp))
                                    Text("No portfolio items yet", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text("Upload your first work photo above", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(0.6f))
                                }
                            }
                        }
                    } else {
                        // Grid-style: 2 per row
                        val rows = pState.data.chunked(2)
                        items(rows) { row ->
                            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                row.forEach { item ->
                                    PortfolioItemCard(item = item, onDelete = { viewModel.deletePortfolioItem(item.id) }, modifier = Modifier.weight(1f))
                                }
                                if (row.size < 2) Spacer(Modifier.weight(1f))
                            }
                        }
                    }
                }
                is UiState.Error -> item {
                    Text(pState.message, color = MaterialTheme.colorScheme.error)
                }
            }

            item { Spacer(Modifier.height(24.dp)) }
        }
    }
}

@Composable
private fun PortfolioItemCard(item: PortfolioItem, onDelete: () -> Unit, modifier: Modifier = Modifier) {
    var showDeleteDialog by remember { mutableStateOf(false) }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Photo?") },
            text = { Text("Remove this photo from your portfolio?") },
            confirmButton = { Button(onClick = { onDelete(); showDeleteDialog = false }, colors = ButtonDefaults.buttonColors(containerColor = Error)) { Text("Delete") } },
            dismissButton = { TextButton(onClick = { showDeleteDialog = false }) { Text("Cancel") } }
        )
    }

    Surface(modifier = modifier, shape = RoundedCornerShape(12.dp), shadowElevation = 2.dp) {
        Box {
            AsyncImage(
                model = item.photoUrl,
                contentDescription = item.caption,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxWidth().aspectRatio(1f).clip(RoundedCornerShape(12.dp))
            )
            if (item.caption.isNotBlank()) {
                Surface(
                    modifier = Modifier.align(Alignment.BottomStart).fillMaxWidth(),
                    color = Color.Black.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(bottomStart = 12.dp, bottomEnd = 12.dp)
                ) {
                    Text(item.caption, modifier = Modifier.padding(8.dp), style = MaterialTheme.typography.labelSmall, color = Color.White, maxLines = 1)
                }
            }
            IconButton(
                onClick = { showDeleteDialog = true },
                modifier = Modifier.align(Alignment.TopEnd)
            ) {
                Surface(shape = RoundedCornerShape(8.dp), color = Color.Black.copy(alpha = 0.5f)) {
                    Icon(Icons.Default.Delete, null, tint = Color.White, modifier = Modifier.padding(4.dp).size(16.dp))
                }
            }
        }
    }
}
