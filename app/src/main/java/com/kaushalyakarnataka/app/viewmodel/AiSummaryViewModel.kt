package com.kaushalyakarnataka.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kaushalyakarnataka.app.data.model.Review
import com.kaushalyakarnataka.app.data.repository.AiSummaryRepository
import com.kaushalyakarnataka.app.utils.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AiSummaryViewModel @Inject constructor(
    private val repository: AiSummaryRepository
) : ViewModel() {

    private val _summaryState = MutableStateFlow<UiState<String>>(UiState.Loading)
    val summaryState: StateFlow<UiState<String>> = _summaryState.asStateFlow()

    fun loadCachedSummary(workerId: String) {
        viewModelScope.launch {
            repository.getCachedSummary(workerId).collect { cached ->
                if (!cached.isNullOrBlank()) {
                    _summaryState.value = UiState.Success(cached)
                }
            }
        }
    }

    fun generateSummary(workerId: String, reviews: List<Review>) {
        viewModelScope.launch {
            _summaryState.value = UiState.Loading
            val result = repository.getSummary(workerId, reviews)
            _summaryState.value = result.fold(
                onSuccess = { UiState.Success(it) },
                onFailure = { UiState.Error(it.message ?: "Unable to generate summary") }
            )
        }
    }

    fun retry(workerId: String, reviews: List<Review>) {
        viewModelScope.launch {
            repository.clearCache(workerId)
            generateSummary(workerId, reviews)
        }
    }
}
