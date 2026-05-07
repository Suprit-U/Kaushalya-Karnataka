package com.kaushalyakarnataka.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kaushalyakarnataka.app.data.model.User
import com.kaushalyakarnataka.app.data.model.UserRole
import com.kaushalyakarnataka.app.data.repository.AuthRepository
import com.kaushalyakarnataka.app.utils.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _authState = MutableStateFlow<UiState<User>?>(null)
    val authState: StateFlow<UiState<User>?> = _authState.asStateFlow()

    private val _currentUserRole = MutableStateFlow<UserRole?>(null)
    val currentUserRole: StateFlow<UserRole?> = _currentUserRole.asStateFlow()

    init {
        checkCurrentUser()
    }

    private fun checkCurrentUser() {
        val user = authRepository.currentUser
        if (user != null) {
            _authState.value = UiState.Loading
            viewModelScope.launch {
                val profileResult = authRepository.getUserProfile(user.uid)
                _authState.value = profileResult
                if (profileResult is UiState.Success) {
                    _currentUserRole.value = profileResult.data.role
                }
            }
        }
    }

    fun selectRole(role: UserRole) {
        _currentUserRole.value = role
    }

    fun login(email: String, pass: String) {
        _authState.value = UiState.Loading
        viewModelScope.launch {
            val result = authRepository.signIn(email, pass)
            if (result is UiState.Success) {
                _currentUserRole.value = result.data.role
            }
            _authState.value = result
        }
    }

    fun register(name: String, email: String, pass: String, phone: String, role: UserRole) {
        _authState.value = UiState.Loading
        viewModelScope.launch {
            val result = authRepository.signUp(name, email, pass, phone, role)
            if (result is UiState.Success) {
                _currentUserRole.value = result.data.role
            }
            _authState.value = result
        }
    }

    fun logout() {
        viewModelScope.launch {
            authRepository.signOut()
            _authState.value = null
            _currentUserRole.value = null
        }
    }

    fun clearError() {
        if (_authState.value is UiState.Error) {
            _authState.value = null
        }
    }
}
