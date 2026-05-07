package com.kaushalyakarnataka.app.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kaushalyakarnataka.app.data.model.Review
import com.kaushalyakarnataka.app.data.repository.ReviewRepository
import com.kaushalyakarnataka.app.utils.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ReviewViewModel @Inject constructor(
    private val reviewRepository: ReviewRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val workerId: String = checkNotNull(savedStateHandle["workerId"])

    private val _reviewsState = MutableStateFlow<UiState<List<Review>>>(UiState.Loading)
    val reviewsState: StateFlow<UiState<List<Review>>> = _reviewsState.asStateFlow()

    init {
        loadReviews()
    }

    private fun loadReviews() {
        viewModelScope.launch {
            _reviewsState.value = reviewRepository.getWorkerReviews(workerId)
        }
    }

    fun markReviewHelpful(reviewId: String) {
        viewModelScope.launch {
            val result = reviewRepository.markHelpful(reviewId)
            if (result is UiState.Success) {
                // Optimistically update the UI count
                val currentState = _reviewsState.value
                if (currentState is UiState.Success) {
                    val updatedList = currentState.data.map { review ->
                        if (review.id == reviewId) {
                            review.copy(helpfulCount = review.helpfulCount + 1)
                        } else {
                            review
                        }
                    }
                    _reviewsState.value = UiState.Success(updatedList)
                }
            }
        }
    }
}
