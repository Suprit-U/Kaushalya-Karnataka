package com.kaushalyakarnataka.app.ui.screens.worker

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.kaushalyakarnataka.app.data.model.ServiceCategory
import com.kaushalyakarnataka.app.ui.components.AppTopBar
import com.kaushalyakarnataka.app.ui.components.EmptyState
import com.kaushalyakarnataka.app.ui.components.PrimaryButton
import com.kaushalyakarnataka.app.ui.components.PrimaryInputField
import com.kaushalyakarnataka.app.ui.components.SecondaryButton
import com.kaushalyakarnataka.app.utils.UiState
import com.kaushalyakarnataka.app.viewmodel.PortfolioViewModel

@Composable
fun PortfolioScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: PortfolioViewModel = hiltViewModel()
) {
    val uploadState by viewModel.uploadState.collectAsState()
    
    var caption by remember { mutableStateOf("") }
    
    LaunchedEffect(uploadState) {
        if (uploadState is UiState.Success) {
            caption = "" // Reset on success
            viewModel.clearState()
        }
    }

    Scaffold(
        topBar = {
            AppTopBar(
                title = "Manage Portfolio",
                onBackClick = onNavigateBack
            )
        }
    ) { paddingValues ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            Text(
                text = "Add New Work",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            PrimaryInputField(
                value = caption,
                onValueChange = { caption = it },
                label = "Image Caption"
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            SecondaryButton(
                text = "Select Image (Mock)",
                onClick = { /* In a real app, launch Image Picker Intent here */ },
                modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            if (uploadState is UiState.Error) {
                Text(
                    text = (uploadState as UiState.Error).message,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
            }
            
            PrimaryButton(
                text = if (uploadState is UiState.Loading) "Uploading..." else "Upload to Portfolio",
                onClick = {
                    // Trigger upload with dummy data for now
                    val dummyBytes = byteArrayOf(0, 1, 2)
                    viewModel.uploadPortfolioItem(dummyBytes, caption, ServiceCategory.OTHER)
                },
                enabled = uploadState !is UiState.Loading && caption.isNotBlank(),
                modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            Text(
                text = "Your Current Portfolio",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            EmptyState(
                icon = Icons.Default.Upload,
                title = "No Items Yet",
                message = "Upload images to showcase your work."
            )
        }
    }
}
