package com.kaushalyakarnataka.app.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kaushalyakarnataka.app.data.model.User
import com.kaushalyakarnataka.app.data.repository.AuthRepository
import com.kaushalyakarnataka.app.utils.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CustomerProfileViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _customerState = MutableStateFlow<UiState<User>>(UiState.Loading)
    val customerState: StateFlow<UiState<User>> = _customerState.asStateFlow()

    init {
        loadCustomerProfile()
    }

    fun loadCustomerProfile() {
        viewModelScope.launch {
            val currentUser = authRepository.currentUser
            if (currentUser != null) {
                _customerState.value = authRepository.getUserProfile(currentUser.uid)
            } else {
                _customerState.value = UiState.Error("User not authenticated")
            }
        }
    }
}