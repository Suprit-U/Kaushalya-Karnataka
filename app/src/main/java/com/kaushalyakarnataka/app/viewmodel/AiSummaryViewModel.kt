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
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AiSummaryViewModel @Inject constructor(
    private val repository: AiSummaryRepository
) : ViewModel() {

    private val _summaryState = MutableStateFlow<UiState<String>>(UiState.Loading)
    val summaryState: StateFlow<UiState<String>> = _summaryState.asStateFlow()

    private var lastWorkerId: String? = null
    private var isGenerating = false

    fun generateSummary(workerId: String, reviews: List<Review>) {
        // Prevent duplicate calls for same worker
        if (isGenerating || (lastWorkerId == workerId && _summaryState.value is UiState.Success)) return
        if (reviews.size < 2) {
            _summaryState.value = UiState.Error("Not enough reviews")
            return
        }

        isGenerating = true
        lastWorkerId = workerId

        viewModelScope.launch {
            // Check cache first to avoid loading flash
            val cached = repository.getCachedSummary(workerId).first()
            if (!cached.isNullOrBlank()) {
                _summaryState.value = UiState.Success(cached!!)
                isGenerating = false
                return@launch
            }

            _summaryState.value = UiState.Loading
            val result = repository.getSummary(workerId, reviews)
            _summaryState.value = result.fold(
                onSuccess = { UiState.Success(it) },
                onFailure = { UiState.Error(it.message ?: "Unable to generate summary") }
            )
            isGenerating = false
        }
    }

    fun retry(workerId: String, reviews: List<Review>) {
        if (isGenerating) return // prevent duplicate concurrent retries
        viewModelScope.launch { repository.clearCache(workerId) }
        reset()
        generateSummary(workerId, reviews)
    }

    fun reset() {
        lastWorkerId = null
        isGenerating = false
        _summaryState.value = UiState.Loading
    }
}
