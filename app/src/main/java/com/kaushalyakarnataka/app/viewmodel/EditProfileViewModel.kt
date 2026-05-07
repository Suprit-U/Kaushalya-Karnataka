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
                val result = authRepository.updateUserProfile(
                    uid = currentUser.uid,
                    name = name.trim(),
                    phone = phone.trim(),
                    location = location.trim()
                )
                _updateState.value = result
                if (result is UiState.Success) {
                    _userState.value = UiState.Success(
                        currentProfile.copy(
                            name = name.trim(),
                            phone = phone.trim(),
                            location = location.trim()
                        )
                    )
                }
            } catch (e: Exception) {
                _updateState.value = UiState.Error(e.message ?: "Failed to update profile")
            }
        }
    }

    fun clearUpdateState() {
        _updateState.value = null
    }
}
