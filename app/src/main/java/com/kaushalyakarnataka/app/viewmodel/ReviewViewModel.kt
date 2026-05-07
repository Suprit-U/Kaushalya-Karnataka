package com.kaushalyakarnataka.app.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kaushalyakarnataka.app.data.model.Review
import com.kaushalyakarnataka.app.data.repository.AuthRepository
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
    private val authRepository: AuthRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val workerId: String = checkNotNull(savedStateHandle["workerId"])

    private val _reviewsState = MutableStateFlow<UiState<List<Review>>>(UiState.Loading)
    val reviewsState: StateFlow<UiState<List<Review>>> = _reviewsState.asStateFlow()

    private val _submitState = MutableStateFlow<UiState<Review>?>(null)
    val submitState: StateFlow<UiState<Review>?> = _submitState.asStateFlow()

    init {
        loadReviews()
    }

    fun loadReviews() {
        viewModelScope.launch {
            _reviewsState.value = UiState.Loading
            _reviewsState.value = reviewRepository.getWorkerReviews(workerId)
        }
    }

    fun submitReview(
        rating: Int,
        comment: String,
        serviceType: String,
        bookingId: String = ""
    ) {
        val currentUser = authRepository.currentUser ?: return
        if (rating == 0 || comment.isBlank()) return

        _submitState.value = UiState.Loading
        viewModelScope.launch {
            val review = Review(
                workerId = workerId,
                customerId = currentUser.uid,
                customerName = currentUser.displayName ?: "Customer",
                customerInitial = (currentUser.displayName?.firstOrNull() ?: 'C').toString(),
                customerAvatarUrl = currentUser.photoUrl?.toString() ?: "",
                rating = rating,
                comment = comment.trim(),
                serviceType = serviceType,
                isVerified = true,
                bookingId = bookingId,
            )
            val result = reviewRepository.addReview(review)
            _submitState.value = result
            if (result is UiState.Success) {
                loadReviews() // Refresh
            }
        }
    }

    fun clearSubmitState() {
        _submitState.value = null
    }

    fun markReviewHelpful(reviewId: String) {
        viewModelScope.launch {
            val result = reviewRepository.markHelpful(reviewId)
            if (result is UiState.Success) {
                val currentState = _reviewsState.value
                if (currentState is UiState.Success) {
                    _reviewsState.value = UiState.Success(
                        currentState.data.map { review ->
                            if (review.id == reviewId) review.copy(helpfulCount = review.helpfulCount + 1)
                            else review
                        }
                    )
                }
            }
        }
    }
}
