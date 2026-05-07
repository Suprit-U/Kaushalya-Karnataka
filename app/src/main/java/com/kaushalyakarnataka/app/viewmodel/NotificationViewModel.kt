package com.kaushalyakarnataka.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kaushalyakarnataka.app.data.model.AppNotification
import com.kaushalyakarnataka.app.data.repository.AuthRepository
import com.kaushalyakarnataka.app.data.repository.NotificationRepository
import com.kaushalyakarnataka.app.utils.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NotificationViewModel @Inject constructor(
    private val notificationRepository: NotificationRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _notifications = MutableStateFlow<UiState<List<AppNotification>>>(UiState.Loading)
    val notifications: StateFlow<UiState<List<AppNotification>>> = _notifications.asStateFlow()

    private val _unreadCount = MutableStateFlow(0)
    val unreadCount: StateFlow<Int> = _unreadCount.asStateFlow()

    private var notificationJob: Job? = null

    init { load() }

    fun load() {
        val uid = authRepository.currentUser?.uid ?: return
        _notifications.value = UiState.Loading
        notificationJob?.cancel()
        notificationJob = viewModelScope.launch {
            notificationRepository.observeNotifications(uid).collect { state ->
                _notifications.value = state
                _unreadCount.value = (state as? UiState.Success)?.data?.count { !it.isRead } ?: 0
            }
        }
    }

    fun refresh() {
        val uid = authRepository.currentUser?.uid ?: return
        viewModelScope.launch {
            val state = notificationRepository.getNotifications(uid)
            _notifications.value = state
            _unreadCount.value = (state as? UiState.Success)?.data?.count { !it.isRead } ?: 0
        }
    }

    fun markAllRead() {
        val uid = authRepository.currentUser?.uid ?: return
        viewModelScope.launch {
            notificationRepository.markAllRead(uid)
        }
    }
}
