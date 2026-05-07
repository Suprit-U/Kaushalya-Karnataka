package com.kaushalyakarnataka.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kaushalyakarnataka.app.data.model.AppNotification
import com.kaushalyakarnataka.app.data.repository.AuthRepository
import com.kaushalyakarnataka.app.data.repository.NotificationRepository
import com.kaushalyakarnataka.app.utils.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
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

    init { load() }

    fun load() {
        val uid = authRepository.currentUser?.uid ?: return
        viewModelScope.launch {
            _notifications.value = notificationRepository.getNotifications(uid)
            _unreadCount.value = notificationRepository.getUnreadCount(uid)
        }
    }

    fun markAllRead() {
        val uid = authRepository.currentUser?.uid ?: return
        viewModelScope.launch {
            notificationRepository.markAllRead(uid)
            _unreadCount.value = 0
            load()
        }
    }
}
