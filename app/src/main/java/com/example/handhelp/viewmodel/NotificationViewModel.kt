package com.example.handhelp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.handhelp.data.model.Notification
import com.example.handhelp.repository.NotificationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class NotificationUiState {
    object Idle : NotificationUiState()
    object Loading : NotificationUiState()
    object Success : NotificationUiState()
    data class Error(val message: String) : NotificationUiState()
}

@HiltViewModel
class NotificationViewModel @Inject constructor(
    private val notificationRepository: NotificationRepository
) : ViewModel() {

    private val _notifications = MutableStateFlow<List<Notification>>(emptyList())
    val notifications: StateFlow<List<Notification>> = _notifications.asStateFlow()

    private val _unreadCount = MutableStateFlow(0)
    val unreadCount: StateFlow<Int> = _unreadCount.asStateFlow()

    private val _uiState = MutableStateFlow<NotificationUiState>(NotificationUiState.Idle)
    val uiState: StateFlow<NotificationUiState> = _uiState.asStateFlow()

    // ─────────────────────────────────────────
    // Charger les notifications (temps réel)
    // ─────────────────────────────────────────
    fun loadNotifications(userId: String) {
        viewModelScope.launch {
            notificationRepository.getUserNotifications(userId).collect { result ->
                result.onSuccess { list ->
                    _notifications.value = list
                    _unreadCount.value = list.count { !it.isRead }
                }.onFailure { e ->
                    _uiState.value = NotificationUiState.Error(
                        e.localizedMessage ?: "Erreur chargement"
                    )
                }
            }
        }
    }

    // ─────────────────────────────────────────
    // Marquer une notification comme lue
    // ─────────────────────────────────────────
    fun markAsRead(notificationId: String) {
        viewModelScope.launch {
            notificationRepository.markAsRead(notificationId)
        }
    }

    // ─────────────────────────────────────────
    // Tout marquer comme lu
    // ─────────────────────────────────────────
    fun markAllAsRead(userId: String) {
        viewModelScope.launch {
            _uiState.value = NotificationUiState.Loading
            val result = notificationRepository.markAllAsRead(userId)
            result.onSuccess {
                _uiState.value = NotificationUiState.Success
            }.onFailure { e ->
                _uiState.value = NotificationUiState.Error(e.localizedMessage ?: "Erreur")
            }
        }
    }

    // ─────────────────────────────────────────
    // Supprimer une notification
    // ─────────────────────────────────────────
    fun deleteNotification(notificationId: String) {
        viewModelScope.launch {
            notificationRepository.deleteNotification(notificationId)
        }
    }

    // ─────────────────────────────────────────
    // Tout supprimer
    // ─────────────────────────────────────────
    fun deleteAll(userId: String) {
        viewModelScope.launch {
            _uiState.value = NotificationUiState.Loading
            val result = notificationRepository.deleteAllNotifications(userId)
            result.onSuccess {
                _uiState.value = NotificationUiState.Success
            }.onFailure { e ->
                _uiState.value = NotificationUiState.Error(e.localizedMessage ?: "Erreur")
            }
        }
    }

    fun resetUiState() {
        _uiState.value = NotificationUiState.Idle
    }
}