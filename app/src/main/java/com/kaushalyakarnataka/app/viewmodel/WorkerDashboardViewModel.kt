package com.kaushalyakarnataka.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kaushalyakarnataka.app.data.model.Booking
import com.kaushalyakarnataka.app.data.model.BookingStatus
import com.kaushalyakarnataka.app.data.model.EarningsData
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
class WorkerDashboardViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val workerRepository: WorkerRepository,
    private val bookingRepository: BookingRepository
) : ViewModel() {

    private val _workerState = MutableStateFlow<UiState<Worker>>(UiState.Loading)
    val workerState: StateFlow<UiState<Worker>> = _workerState.asStateFlow()

    private val _pendingJobs = MutableStateFlow<UiState<List<Booking>>>(UiState.Loading)
    val pendingJobs: StateFlow<UiState<List<Booking>>> = _pendingJobs.asStateFlow()

    private val _upcomingJobs = MutableStateFlow<UiState<List<Booking>>>(UiState.Loading)
    val upcomingJobs: StateFlow<UiState<List<Booking>>> = _upcomingJobs.asStateFlow()

    private val _earningsData = MutableStateFlow<UiState<EarningsData>>(UiState.Loading)
    val earningsData: StateFlow<UiState<EarningsData>> = _earningsData.asStateFlow()

    init {
        loadDashboardData()
    }

    fun loadDashboardData() {
        val currentUser = authRepository.currentUser ?: return
        val uid = currentUser.uid

        viewModelScope.launch {
            _workerState.value = workerRepository.getWorkerById(uid)
            _pendingJobs.value = bookingRepository.getWorkerPendingBookings(uid)
            
            // Load upcoming jobs (Confirmed status)
            val allBookingsResult = bookingRepository.getWorkerBookings(uid)
            if (allBookingsResult is UiState.Success) {
                val upcoming = allBookingsResult.data.filter { it.status == BookingStatus.CONFIRMED }
                _upcomingJobs.value = UiState.Success(upcoming)
                
                // Calculate simple mock earnings
                val completed = allBookingsResult.data.filter { it.status == BookingStatus.COMPLETED }
                val totalEarnings = completed.sumOf { it.estimatedCostMax } // Simplification
                
                _earningsData.value = UiState.Success(
                    EarningsData(
                        thisMonthTotal = totalEarnings.coerceAtLeast(12500),
                        lastMonthTotal = 10200,
                        percentageChange = 22,
                        completedJobs = completed.size.coerceAtLeast(45),
                        pendingJobs = (_pendingJobs.value as? UiState.Success)?.data?.size ?: 0,
                        averageRating = 4.8
                    )
                )
            } else {
                _upcomingJobs.value = allBookingsResult
                _earningsData.value = UiState.Error("Failed to load earnings")
            }
        }
    }

    fun toggleAvailability(isAvailable: Boolean) {
        val currentUser = authRepository.currentUser ?: return
        viewModelScope.launch {
            val result = workerRepository.updateAvailability(currentUser.uid, isAvailable)
            if (result is UiState.Success) {
                // Update local state optimistic
                val currentWorker = (_workerState.value as? UiState.Success)?.data
                if (currentWorker != null) {
                    _workerState.value = UiState.Success(currentWorker.copy(isAvailable = isAvailable))
                }
            }
        }
    }

    fun acceptJob(bookingId: String) {
        updateJobStatus(bookingId, BookingStatus.CONFIRMED)
    }

    fun declineJob(bookingId: String) {
        updateJobStatus(bookingId, BookingStatus.CANCELLED)
    }

    private fun updateJobStatus(bookingId: String, status: BookingStatus) {
        viewModelScope.launch {
            val result = bookingRepository.updateBookingStatus(bookingId, status)
            if (result is UiState.Success) {
                loadDashboardData() // Reload everything
            }
        }
    }
}
