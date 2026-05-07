package com.kaushalyakarnataka.app.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kaushalyakarnataka.app.data.model.PortfolioItem
import com.kaushalyakarnataka.app.data.model.PortfolioStats
import com.kaushalyakarnataka.app.data.model.RatingStats
import com.kaushalyakarnataka.app.data.model.Review
import com.kaushalyakarnataka.app.data.model.Service
import com.kaushalyakarnataka.app.data.model.ServiceCategory
import com.kaushalyakarnataka.app.data.model.Worker
import com.kaushalyakarnataka.app.data.repository.ReviewRepository
import com.kaushalyakarnataka.app.data.repository.ServiceRepository
import com.kaushalyakarnataka.app.data.repository.AuthRepository
import com.kaushalyakarnataka.app.data.repository.WorkerRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import com.kaushalyakarnataka.app.utils.UiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WorkerProfileViewModel @Inject constructor(
    private val workerRepository: WorkerRepository,
    private val serviceRepository: ServiceRepository,
    private val reviewRepository: ReviewRepository,
    private val authRepository: AuthRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val workerId: String = savedStateHandle["workerId"] ?: authRepository.currentUser?.uid ?: ""

    private val _workerState = MutableStateFlow<UiState<Worker>>(UiState.Loading)
    val workerState: StateFlow<UiState<Worker>> = _workerState.asStateFlow()

    private val _servicesState = MutableStateFlow<UiState<List<Service>>>(UiState.Loading)
    val servicesState: StateFlow<UiState<List<Service>>> = _servicesState.asStateFlow()

    private val _reviewsState = MutableStateFlow<UiState<List<Review>>>(UiState.Loading)
    val reviewsState: StateFlow<UiState<List<Review>>> = _reviewsState.asStateFlow()

    private val _portfolioState = MutableStateFlow<UiState<List<PortfolioItem>>>(UiState.Loading)
    val portfolioState: StateFlow<UiState<List<PortfolioItem>>> = _portfolioState.asStateFlow()

    private val _ratingStatsState = MutableStateFlow<UiState<RatingStats>>(UiState.Loading)
    val ratingStatsState: StateFlow<UiState<RatingStats>> = _ratingStatsState.asStateFlow()

    private val _updateState = MutableStateFlow<UiState<Unit>?>(null)
    val updateState: StateFlow<UiState<Unit>?> = _updateState.asStateFlow()

    private var workerJob: Job? = null
    private var reviewsJob: Job? = null

    init {
        loadWorkerProfile()
    }

    fun loadWorkerProfile() {
        if (workerId.isBlank()) {
            _workerState.value = UiState.Error("Worker ID not provided")
            return
        }
        viewModelScope.launch {
            _servicesState.value = serviceRepository.getWorkerServices(workerId)
            _portfolioState.value = serviceRepository.getWorkerPortfolio(workerId)
            _ratingStatsState.value = reviewRepository.getWorkerRatingStats(workerId)
        }
        workerJob?.cancel()
        workerJob = viewModelScope.launch {
            workerRepository.observeWorkerById(workerId).collect { _workerState.value = it }
        }
        reviewsJob?.cancel()
        reviewsJob = viewModelScope.launch {
            reviewRepository.observeWorkerReviews(workerId).collect { state ->
                _reviewsState.value = state
                if (state is UiState.Success) {
                    val reviews = state.data
                    _ratingStatsState.value = UiState.Success(
                        RatingStats(
                            averageRating = if (reviews.isEmpty()) 0f else reviews.map { it.rating }.average().toFloat(),
                            totalReviews = reviews.size,
                            ratingCounts = (1..5).associateWith { rating -> reviews.count { it.rating == rating } }
                        )
                    )
                }
            }
        }
    }

    fun updateWorkerProfile(name: String, phone: String, bio: String, role: String, category: ServiceCategory, baseLabourCharge: Int) {
        if (workerId.isBlank()) return
        _updateState.value = UiState.Loading
        viewModelScope.launch {
            val result = workerRepository.updateWorkerProfileFields(workerId, name, phone, bio, role, category, baseLabourCharge)
            _updateState.value = result
            if (result is UiState.Success) {
                loadWorkerProfile()
            }
        }
    }

    fun updateBio(bio: String) {
        if (workerId.isBlank()) return
        _updateState.value = UiState.Loading
        viewModelScope.launch {
            val result = workerRepository.updateWorkerProfile(workerId, mapOf("bio" to bio.trim()))
            _updateState.value = result
            if (result is UiState.Success) {
                loadWorkerProfile()
            }
        }
    }

    fun clearUpdateState() {
        _updateState.value = null
    }
}
