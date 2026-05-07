package com.kaushalyakarnataka.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kaushalyakarnataka.app.data.model.ServiceCategory
import com.kaushalyakarnataka.app.data.model.Worker
import com.kaushalyakarnataka.app.data.repository.WorkerRepository
import com.kaushalyakarnataka.app.utils.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val workerRepository: WorkerRepository
) : ViewModel() {

    private val _topWorkersState = MutableStateFlow<UiState<List<Worker>>>(UiState.Loading)
    val topWorkersState: StateFlow<UiState<List<Worker>>> = _topWorkersState.asStateFlow()

    private val _categories = MutableStateFlow(ServiceCategory.values().toList())
    val categories: StateFlow<List<ServiceCategory>> = _categories.asStateFlow()

    init {
        loadTopWorkers()
    }

    fun loadTopWorkers() {
        _topWorkersState.value = UiState.Loading
        viewModelScope.launch {
            _topWorkersState.value = workerRepository.getTopWorkers(limit = 10)
        }
    }
}
