package com.kaushalyakarnataka.app.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kaushalyakarnataka.app.data.model.Booking
import com.kaushalyakarnataka.app.data.model.Worker
import com.kaushalyakarnataka.app.data.repository.AuthRepository
import com.kaushalyakarnataka.app.data.repository.BookingRepository
import com.kaushalyakarnataka.app.data.repository.WorkerRepository
import com.kaushalyakarnataka.app.utils.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HireViewModel @Inject constructor(
    private val workerRepository: WorkerRepository,
    private val bookingRepository: BookingRepository,
    private val authRepository: AuthRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val workerId: String = checkNotNull(savedStateHandle["workerId"])
    private val serviceId: String? = savedStateHandle["serviceId"] // Optional pre-selected service

    private val _workerState = MutableStateFlow<UiState<Worker>>(UiState.Loading)
    val workerState: StateFlow<UiState<Worker>> = _workerState.asStateFlow()

    private val _bookingState = MutableStateFlow<UiState<Booking>?>(null)
    val bookingState: StateFlow<UiState<Booking>?> = _bookingState.asStateFlow()

    init {
        loadWorker()
    }

    private fun loadWorker() {
        viewModelScope.launch {
            _workerState.value = workerRepository.getWorkerById(workerId)
        }
    }

    fun submitBookingRequest(
        serviceName: String,
        timeSlot: String,
        address: String,
        notes: String
    ) {
        val worker = (_workerState.value as? UiState.Success)?.data ?: return
        val currentUser = authRepository.currentUser ?: return

        _bookingState.value = UiState.Loading
        viewModelScope.launch {
            // Need to get current user details from Firestore to get name, but for simplicity assuming we have it
            // Ideally we get the User object first
            val userProfileResult = authRepository.getUserProfile(currentUser.uid)
            val customerName = if (userProfileResult is UiState.Success) userProfileResult.data.name else "Customer"

            val booking = Booking(
                customerId = currentUser.uid,
                customerName = customerName,
                workerId = worker.uid,
                workerName = worker.name,
                service = serviceName,
                timeSlot = timeSlot,
                address = address,
                notes = notes,
                estimatedCostMin = worker.pricePerHour, // Rough estimate logic
                estimatedCostMax = worker.pricePerHour * 2
            )
            
            _bookingState.value = bookingRepository.createBooking(booking)
        }
    }

    fun clearBookingState() {
        _bookingState.value = null
    }
}
