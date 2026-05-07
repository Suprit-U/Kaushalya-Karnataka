package com.kaushalyakarnataka.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kaushalyakarnataka.app.data.model.ServiceCategory
import com.kaushalyakarnataka.app.data.model.Worker
import com.kaushalyakarnataka.app.data.repository.WorkerRepository
import com.kaushalyakarnataka.app.utils.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
@OptIn(FlowPreview::class)
class SearchViewModel @Inject constructor(
    private val workerRepository: WorkerRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedCategory = MutableStateFlow<ServiceCategory?>(null)
    val selectedCategory: StateFlow<ServiceCategory?> = _selectedCategory.asStateFlow()

    private val _searchResults = MutableStateFlow<UiState<List<Worker>>>(UiState.Success(emptyList()))
    val searchResults: StateFlow<UiState<List<Worker>>> = _searchResults.asStateFlow()

    init {
        // Debounce search query changes to avoid hitting Firestore on every keystroke
        _searchQuery
            .debounce(500)
            .distinctUntilChanged()
            .onEach { performSearch() }
            .launchIn(viewModelScope)
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun selectCategory(category: ServiceCategory?) {
        _selectedCategory.value = category
        performSearch()
    }

    private fun performSearch() {
        _searchResults.value = UiState.Loading
        viewModelScope.launch {
            _searchResults.value = workerRepository.searchWorkers(
                query = _searchQuery.value,
                category = _selectedCategory.value
            )
        }
    }
}
