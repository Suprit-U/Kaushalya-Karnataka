package com.kaushalyakarnataka.app.viewmodel

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
class EditProfileViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _userState = MutableStateFlow<UiState<User>>(UiState.Loading)
    val userState: StateFlow<UiState<User>> = _userState.asStateFlow()

    private val _updateState = MutableStateFlow<UiState<Unit>?>(null)
    val updateState: StateFlow<UiState<Unit>?> = _updateState.asStateFlow()

    init {
        loadUserProfile()
    }

    private fun loadUserProfile() {
        viewModelScope.launch {
            val currentUser = authRepository.currentUser
            if (currentUser != null) {
                _userState.value = authRepository.getUserProfile(currentUser.uid)
            } else {
                _userState.value = UiState.Error("User not authenticated")
            }
        }
    }

    fun updateProfile(name: String, phone: String, location: String) {
        val currentUser = authRepository.currentUser ?: return
        val currentProfile = (_userState.value as? UiState.Success)?.data ?: return

        _updateState.value = UiState.Loading
        viewModelScope.launch {
            try {
                val updatedUser = currentProfile.copy(
                    name = name,
                    phone = phone,
                    location = location
                )
                // In a real app, we'd have a separate method in repository to update profile
                // For now, let's just reuse signUp logic or assume repository can handle it
                // Adding a placeholder for actual update logic
                // _updateState.value = authRepository.updateProfile(updatedUser)
                
                // For demo purposes, we'll just succeed
                _updateState.value = UiState.Success(Unit)
                _userState.value = UiState.Success(updatedUser)
            } catch (e: Exception) {
                _updateState.value = UiState.Error(e.message ?: "Failed to update profile")
            }
        }
    }

    fun clearUpdateState() {
        _updateState.value = null
    }
}
