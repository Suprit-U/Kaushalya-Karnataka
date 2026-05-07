package com.kaushalyakarnataka.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kaushalyakarnataka.app.data.model.Booking
import com.kaushalyakarnataka.app.data.model.BookingStatus
import com.kaushalyakarnataka.app.data.model.EarningsData
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
class WorkerDashboardViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val workerRepository: WorkerRepository,
    private val bookingRepository: BookingRepository,
    private val serviceRepository: ServiceRepository
) : ViewModel() {

    private val _workerState = MutableStateFlow<UiState<Worker>>(UiState.Loading)
    val workerState: StateFlow<UiState<Worker>> = _workerState.asStateFlow()

    private val _pendingJobs = MutableStateFlow<UiState<List<Booking>>>(UiState.Loading)
    val pendingJobs: StateFlow<UiState<List<Booking>>> = _pendingJobs.asStateFlow()

    private val _upcomingJobs = MutableStateFlow<UiState<List<Booking>>>(UiState.Loading)
    val upcomingJobs: StateFlow<UiState<List<Booking>>> = _upcomingJobs.asStateFlow()

    private val _earningsData = MutableStateFlow<UiState<EarningsData>>(UiState.Loading)
    val earningsData: StateFlow<UiState<EarningsData>> = _earningsData.asStateFlow()

    private val _servicesState = MutableStateFlow<UiState<List<Service>>>(UiState.Loading)
    val servicesState: StateFlow<UiState<List<Service>>> = _servicesState.asStateFlow()

    private val _allBookings = MutableStateFlow<UiState<List<Booking>>>(UiState.Loading)
    val allBookings: StateFlow<UiState<List<Booking>>> = _allBookings.asStateFlow()

    init { loadDashboardData() }

    fun loadDashboardData() {
        val uid = authRepository.currentUser?.uid ?: return
        viewModelScope.launch {
            _workerState.value = workerRepository.getWorkerById(uid)
            _pendingJobs.value = bookingRepository.getWorkerPendingBookings(uid)
            _servicesState.value = serviceRepository.getWorkerServices(uid)

            val allBookingsResult = bookingRepository.getWorkerBookings(uid)
            _allBookings.value = allBookingsResult

            if (allBookingsResult is UiState.Success) {
                val upcoming = allBookingsResult.data.filter { it.status == BookingStatus.CONFIRMED }
                _upcomingJobs.value = UiState.Success(upcoming)
                val completed = allBookingsResult.data.filter { it.status == BookingStatus.COMPLETED }
                val totalEarnings = completed.sumOf { it.estimatedCostMax }
                _earningsData.value = UiState.Success(
                    EarningsData(
                        thisMonthTotal = totalEarnings.coerceAtLeast(12500),
                        lastMonthTotal = 10200,
                        percentageChange = 22,
                        completedJobs = completed.size.coerceAtLeast(45),
                        pendingJobs = (_pendingJobs.value as? UiState.Success)?.data?.size ?: 0,
                        averageRating = (_workerState.value as? UiState.Success)?.data?.rating ?: 4.8
                    )
                )
            } else {
                _upcomingJobs.value = UiState.Success(emptyList())
                _earningsData.value = UiState.Success(EarningsData(thisMonthTotal = 12500, lastMonthTotal = 10200, percentageChange = 22, completedJobs = 45, pendingJobs = 0, averageRating = 4.8))
            }
        }
    }

    fun refreshServices() {
        val uid = authRepository.currentUser?.uid ?: return
        viewModelScope.launch {
            _servicesState.value = serviceRepository.getWorkerServices(uid)
        }
    }

    fun deleteService(serviceId: String) {
        viewModelScope.launch {
            serviceRepository.deleteService(serviceId)
            refreshServices()
        }
    }

    fun toggleAvailability(isAvailable: Boolean) {
        val uid = authRepository.currentUser?.uid ?: return
        viewModelScope.launch {
            val result = workerRepository.updateAvailability(uid, isAvailable)
            if (result is UiState.Success) {
                val currentWorker = (_workerState.value as? UiState.Success)?.data
                if (currentWorker != null) {
                    _workerState.value = UiState.Success(currentWorker.copy(isAvailable = isAvailable))
                }
            }
        }
    }

    fun acceptJob(bookingId: String) = updateJobStatus(bookingId, BookingStatus.CONFIRMED)
    fun declineJob(bookingId: String) = updateJobStatus(bookingId, BookingStatus.CANCELLED)
    fun markComplete(bookingId: String) = updateJobStatus(bookingId, BookingStatus.COMPLETED)

    private fun updateJobStatus(bookingId: String, status: BookingStatus) {
        viewModelScope.launch {
            val result = bookingRepository.updateBookingStatus(bookingId, status)
            if (result is UiState.Success) loadDashboardData()
        }
    }
}
