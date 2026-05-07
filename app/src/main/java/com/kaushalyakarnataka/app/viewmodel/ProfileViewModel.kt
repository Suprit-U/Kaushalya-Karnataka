package com.kaushalyakarnataka.app.viewmodel

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import com.kaushalyakarnataka.app.data.firebase.FirestoreCollections
import com.kaushalyakarnataka.app.data.model.User
import com.kaushalyakarnataka.app.data.repository.AuthRepository
import com.kaushalyakarnataka.app.data.repository.StorageRepository
import com.kaushalyakarnataka.app.utils.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val firestore: FirebaseFirestore,
    private val storageRepository: StorageRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _profileState = MutableStateFlow<UiState<User>>(UiState.Loading)
    val profileState: StateFlow<UiState<User>> = _profileState.asStateFlow()

    private val _updateState = MutableStateFlow<UiState<Unit>?>(null)
    val updateState: StateFlow<UiState<Unit>?> = _updateState.asStateFlow()

    init { loadProfile() }

    private fun loadProfile() {
        val uid = authRepository.currentUser?.uid ?: return
        viewModelScope.launch {
            _profileState.value = authRepository.getUserProfile(uid)
        }
    }

    fun updateProfile(name: String, phone: String) {
        val uid = authRepository.currentUser?.uid ?: return
        viewModelScope.launch {
            _updateState.value = UiState.Loading
            val currentLocation = (_profileState.value as? UiState.Success)?.data?.location.orEmpty()
            val result = authRepository.updateUserProfile(uid, name.trim(), phone.trim(), currentLocation)
            _updateState.value = result
            if (result is UiState.Success) {
                loadProfile()
            }
        }
    }

    fun updateSavedAddresses(homeAddress: String, workAddress: String) {
        val uid = authRepository.currentUser?.uid ?: return
        viewModelScope.launch {
            _updateState.value = UiState.Loading
            val result = authRepository.updateSavedAddresses(uid, homeAddress, workAddress)
            _updateState.value = result
            if (result is UiState.Success) {
                val current = (_profileState.value as? UiState.Success)?.data
                if (current != null) {
                    _profileState.value = UiState.Success(
                        current.copy(
                            homeAddress = homeAddress.trim(),
                            workAddress = workAddress.trim()
                        )
                    )
                }
                loadProfile()
            }
        }
    }

    fun clearUpdateState() {
        _updateState.value = null
    }

    fun uploadAvatar(uri: Uri) {
        val uid = authRepository.currentUser?.uid ?: return
        viewModelScope.launch {
            try {
                val bytes = context.contentResolver.openInputStream(uri)?.readBytes() ?: return@launch
                val result = storageRepository.uploadImage(bytes, "avatars/$uid")
                if (result is UiState.Success) {
                    firestore.collection(FirestoreCollections.USERS).document(uid)
                        .update("avatarUrl", result.data)
                        .await()
                    loadProfile()
                }
            } catch (e: Exception) {
                // silent
            }
        }
    }
}
