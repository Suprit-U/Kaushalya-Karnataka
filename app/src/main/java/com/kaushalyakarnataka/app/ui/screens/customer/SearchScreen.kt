package com.kaushalyakarnataka.app.ui.screens.customer

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.graphicsLayer
import androidx.hilt.navigation.compose.hiltViewModel
import com.kaushalyakarnataka.app.data.model.ServiceCategory
import com.kaushalyakarnataka.app.ui.components.AppTopBar
import com.kaushalyakarnataka.app.ui.components.CategoryChipRow
import com.kaushalyakarnataka.app.ui.components.EmptyState
import com.kaushalyakarnataka.app.ui.components.SearchBarField
import com.kaushalyakarnataka.app.ui.components.SearchResultCard
import com.kaushalyakarnataka.app.ui.components.WorkerCardSkeleton
import com.kaushalyakarnataka.app.utils.UiState
import com.kaushalyakarnataka.app.viewmodel.SearchViewModel

@Composable
fun SearchScreen(
    initialQuery: String? = null,
    initialCategory: ServiceCategory? = null,
    onNavigateToWorkerProfile: (String) -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SearchViewModel = hiltViewModel()
) {
    val query by viewModel.searchQuery.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()
    val searchResults by viewModel.searchResults.collectAsState()

    LaunchedEffect(initialQuery, initialCategory) {
        if (initialQuery != null) viewModel.updateSearchQuery(initialQuery)
        if (initialCategory != null) viewModel.selectCategory(initialCategory)
    }

    Scaffold(
        topBar = {
            AppTopBar(
                title = "Search",
                onBackClick = onBackClick
            )
        }
    ) { paddingValues ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                SearchBarField(
                    query = query,
                    onQueryChange = { viewModel.updateSearchQuery(it) }
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                CategoryChipRow(
                    categories = ServiceCategory.values().toList(),
                    selectedCategory = selectedCategory,
                    onCategorySelected = { viewModel.selectCategory(it) }
                )
                
                Spacer(modifier = Modifier.height(16.dp))
            }

            when (val state = searchResults) {
                is UiState.Loading -> {
                    LazyColumn {
                        items(5) { WorkerCardSkeleton() }
                    }
                }
                is UiState.Error -> {
                    Text(
                        text = state.message,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(16.dp)
                    )
                }
                is UiState.Success -> {
                    if (state.data.isEmpty()) {
                        EmptyState(
                            icon = Icons.Default.SearchOff,
                            title = "No Kaushals Found",
                            message = "Try adjusting your search query or category filter."
                        )
                    } else {
                        LazyColumn(
                            contentPadding = PaddingValues(bottom = 24.dp)
                        ) {
                            items(state.data) { worker ->
                                SearchResultCard(
                                    worker = worker,
                                    onClick = { onNavigateToWorkerProfile(worker.uid) },
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
