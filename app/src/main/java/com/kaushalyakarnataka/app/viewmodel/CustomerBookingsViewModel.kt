package com.kaushalyakarnataka.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kaushalyakarnataka.app.data.model.Booking
import com.kaushalyakarnataka.app.data.repository.AuthRepository
import com.kaushalyakarnataka.app.data.repository.BookingRepository
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
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _bookingsState = MutableStateFlow<UiState<List<Booking>>>(UiState.Loading)
    val bookingsState: StateFlow<UiState<List<Booking>>> = _bookingsState.asStateFlow()

    init { loadBookings() }

    fun loadBookings() {
        val uid = authRepository.currentUser?.uid ?: return
        _bookingsState.value = UiState.Loading
        viewModelScope.launch {
            _bookingsState.value = bookingRepository.getCustomerBookings(uid)
        }
    }
}
