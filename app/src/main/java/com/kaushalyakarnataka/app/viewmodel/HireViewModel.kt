package com.kaushalyakarnataka.app.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kaushalyakarnataka.app.data.model.Booking
import com.kaushalyakarnataka.app.data.model.Service
import com.kaushalyakarnataka.app.data.model.Worker
import com.kaushalyakarnataka.app.data.repository.AuthRepository
import com.kaushalyakarnataka.app.data.repository.BookingRepository
import com.kaushalyakarnataka.app.data.repository.ServiceRepository
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
    private val serviceRepository: ServiceRepository,
    private val authRepository: AuthRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val workerId: String = checkNotNull(savedStateHandle["workerId"])

    private val _workerState = MutableStateFlow<UiState<Worker>>(UiState.Loading)
    val workerState: StateFlow<UiState<Worker>> = _workerState.asStateFlow()

    private val _servicesState = MutableStateFlow<UiState<List<Service>>>(UiState.Loading)
    val servicesState: StateFlow<UiState<List<Service>>> = _servicesState.asStateFlow()

    private val _bookingState = MutableStateFlow<UiState<Booking>?>(null)
    val bookingState: StateFlow<UiState<Booking>?> = _bookingState.asStateFlow()

    private val _selectedService = MutableStateFlow<Service?>(null)
    val selectedService: StateFlow<Service?> = _selectedService.asStateFlow()

    init {
        loadWorkerAndServices()
    }

    private fun loadWorkerAndServices() {
        viewModelScope.launch {
            _workerState.value = workerRepository.getWorkerById(workerId)
            _servicesState.value = serviceRepository.getWorkerServices(workerId)
        }
    }

    fun selectService(service: Service) {
        _selectedService.value = service
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
            val userProfileResult = authRepository.getUserProfile(currentUser.uid)
            val customerName = if (userProfileResult is UiState.Success) userProfileResult.data.name
                               else currentUser.displayName ?: "Customer"

            val selectedSvc = _selectedService.value
            val estimatedMin = selectedSvc?.startingPrice ?: worker.pricePerHour
            val estimatedMax = (selectedSvc?.startingPrice ?: worker.pricePerHour) * 2

            val booking = Booking(
                customerId = currentUser.uid,
                customerName = customerName,
                workerId = worker.uid,
                workerName = worker.name,
                service = if (selectedSvc != null) selectedSvc.name else serviceName,
                serviceId = selectedSvc?.id ?: "",
                timeSlot = timeSlot,
                address = address,
                notes = notes,
                estimatedCostMin = estimatedMin,
                estimatedCostMax = estimatedMax,
            )
            _bookingState.value = bookingRepository.createBooking(booking)
        }
    }

    fun clearBookingState() {
        _bookingState.value = null
    }
}
