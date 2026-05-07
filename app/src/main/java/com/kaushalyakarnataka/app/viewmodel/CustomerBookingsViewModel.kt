package com.kaushalyakarnataka.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kaushalyakarnataka.app.data.model.Booking
import com.kaushalyakarnataka.app.data.model.Review
import com.kaushalyakarnataka.app.data.repository.AuthRepository
import com.kaushalyakarnataka.app.data.repository.BookingRepository
import com.kaushalyakarnataka.app.data.repository.ReviewRepository
import com.kaushalyakarnataka.app.utils.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CustomerBookingsViewModel @Inject constructor(
    private val bookingRepository: BookingRepository,
    private val reviewRepository: ReviewRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _bookingsState = MutableStateFlow<UiState<List<Booking>>>(UiState.Loading)
    val bookingsState: StateFlow<UiState<List<Booking>>> = _bookingsState.asStateFlow()

    private val _reviewSubmitState = MutableStateFlow<UiState<Review>?>(null)
    val reviewSubmitState: StateFlow<UiState<Review>?> = _reviewSubmitState.asStateFlow()

    private val _finalAmountState = MutableStateFlow<UiState<Unit>?>(null)
    val finalAmountState: StateFlow<UiState<Unit>?> = _finalAmountState.asStateFlow()

    init { loadBookings() }

    fun loadBookings() {
        val uid = authRepository.currentUser?.uid ?: return
        _bookingsState.value = UiState.Loading
        viewModelScope.launch {
            _bookingsState.value = bookingRepository.getCustomerBookings(uid)
        }
    }

    fun submitReview(workerId: String, rating: Int, comment: String, serviceType: String, bookingId: String) {
        val currentUser = authRepository.currentUser ?: return
        if (rating == 0 || comment.isBlank()) return

        _reviewSubmitState.value = UiState.Loading
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
            _reviewSubmitState.value = reviewRepository.addReview(review)
            loadBookings()
        }
    }

    fun clearReviewSubmitState() {
        _reviewSubmitState.value = null
    }

    fun respondToNegotiation(bookingId: String, accepted: Boolean, finalAmount: Int = 0) {
        _finalAmountState.value = UiState.Loading
        viewModelScope.launch {
            _finalAmountState.value = bookingRepository.respondToNegotiation(bookingId, accepted, finalAmount)
            loadBookings()
        }
    }

    fun clearFinalAmountState() {
        _finalAmountState.value = null
    }
}
