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

    // Start with Loading so UI shows skeleton, not empty state
    private val _searchResults = MutableStateFlow<UiState<List<Worker>>>(UiState.Loading)
    val searchResults: StateFlow<UiState<List<Worker>>> = _searchResults.asStateFlow()

    init {
        _searchQuery
            .debounce(400)
            .distinctUntilChanged()
            .onEach { query ->
                if (query.isNotBlank() || _selectedCategory.value != null) {
                    performSearch()
                }
            }
            .launchIn(viewModelScope)
    }

    /** Load all workers (for initial empty search state) */
    fun loadAll() {
        _searchResults.value = UiState.Loading
        viewModelScope.launch {
            _searchResults.value = workerRepository.getTopWorkers(limit = 50)
        }
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
        if (query.isBlank() && _selectedCategory.value == null) {
            loadAll()
        }
    }

    fun selectCategory(category: ServiceCategory?) {
        _selectedCategory.value = category
        performSearch()
    }

    private fun performSearch() {
        _searchResults.value = UiState.Loading
        viewModelScope.launch {
            val cat = _selectedCategory.value
            val q = _searchQuery.value
            _searchResults.value = if (q.isBlank() && cat == null) {
                workerRepository.getTopWorkers(limit = 50)
            } else {
                workerRepository.searchWorkers(query = q, category = cat)
            }
        }
    }
}
