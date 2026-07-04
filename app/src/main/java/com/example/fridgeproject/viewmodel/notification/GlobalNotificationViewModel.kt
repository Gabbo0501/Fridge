package com.example.fridgeproject.viewmodel.notification

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.fridgeproject.FridgeApplication
import com.example.fridgeproject.data.repository.auth.SessionManagerFacade
import com.example.fridgeproject.domain.NotificationRepository
import com.example.fridgeproject.model.enums.NotificationType
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class GlobalNotificationViewModel(
    private val notificationRepository: NotificationRepository
) : ViewModel() {
    private val appStartTime = System.currentTimeMillis()

    private val _snackbarEvent = MutableSharedFlow<String>(extraBufferCapacity = 1)
    val snackbarEvent = _snackbarEvent.asSharedFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    val unreadNotification: StateFlow<Int> =
        SessionManagerFacade.currentUserStateFlow
        .flatMapLatest { userId ->
            if (userId != null) notificationRepository.observeUnreadCount(userId)
            else flowOf(0)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0
        )

    init {
        observeIncomingNotifications()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun observeIncomingNotifications() {
        viewModelScope.launch {
            SessionManagerFacade.currentUserStateFlow
                .flatMapLatest { userId ->
                    if (userId != null) notificationRepository.observeNotifications(userId)
                    else emptyFlow()
                }
                .filter { it.timestamp > appStartTime }
                .collect { notification ->
                    _snackbarEvent.emit(notification.type.toSnackbarMessage())
                }
        }
    }
    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as FridgeApplication
                GlobalNotificationViewModel(
                    notificationRepository = app.container.notificationRepository
                )
            }
        }
    }
}

fun NotificationType.toSnackbarMessage(): String = when (this) {
    NotificationType.LIKE    -> "Someone liked one of your recipes!"
    NotificationType.TIP     -> "Someone added a new tip for one of your recipes!"
    NotificationType.REMIX   -> "Someone remixed one of your recipes"
    NotificationType.REVIEW  -> "Someone added a new review for one of your recipes!"
    else                     -> "You have a new notification!"
}
