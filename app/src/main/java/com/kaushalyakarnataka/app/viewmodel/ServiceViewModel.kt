package com.kaushalyakarnataka.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kaushalyakarnataka.app.data.model.PricingType
import com.kaushalyakarnataka.app.data.model.Service
import com.kaushalyakarnataka.app.data.model.ServiceCategory
import com.kaushalyakarnataka.app.data.model.ServiceDuration
import com.kaushalyakarnataka.app.data.repository.AuthRepository
import com.kaushalyakarnataka.app.data.repository.ServiceRepository
import com.kaushalyakarnataka.app.utils.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ServiceViewModel @Inject constructor(
    private val serviceRepository: ServiceRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _addServiceState = MutableStateFlow<UiState<Service>?>(null)
    val addServiceState: StateFlow<UiState<Service>?> = _addServiceState.asStateFlow()

    fun addService(
        name: String,
        category: ServiceCategory,
        description: String,
        startingPrice: Int,
        pricingType: PricingType,
        estimatedDuration: ServiceDuration,
        tags: List<String>
    ) {
        val workerId = authRepository.currentUser?.uid ?: return

        _addServiceState.value = UiState.Loading
        viewModelScope.launch {
            val service = Service(
                workerId = workerId,
                name = name,
                category = category,
                description = description,
                startingPrice = startingPrice,
                pricingType = pricingType,
                estimatedDuration = estimatedDuration,
                tags = tags,
                isActive = true
            )
            _addServiceState.value = serviceRepository.addService(service)
        }
    }

    fun clearState() {
        _addServiceState.value = null
    }
}
