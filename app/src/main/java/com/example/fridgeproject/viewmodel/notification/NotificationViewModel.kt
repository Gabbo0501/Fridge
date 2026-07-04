package com.example.fridgeproject.viewmodel.notification

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.navigation.toRoute
import com.example.fridgeproject.FridgeApplication
import com.example.fridgeproject.data.repository.auth.SessionManagerFacade
import com.example.fridgeproject.domain.NotificationRepository
import com.example.fridgeproject.domain.RecipeRepository
import com.example.fridgeproject.domain.UserRepository
import com.example.fridgeproject.model.Notification
import com.example.fridgeproject.model.avatarUrl
import com.example.fridgeproject.model.toUi
import com.example.fridgeproject.navigation.NotificationRoute
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class NotificationViewModel(
    savedStateHandle: SavedStateHandle,
    private val notificationRepository: NotificationRepository,
    private val userRepository: UserRepository,
    private val recipeRepository: RecipeRepository
) : ViewModel() {

    private val route = savedStateHandle.toRoute<NotificationRoute>()
    private val routeUserId = route.profileUserId

    private val _uiState = MutableStateFlow(NotificationUiState())
    val uiState: StateFlow<NotificationUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                SessionManagerFacade.currentUserStateFlow,
                notificationRepository.getNotificationsForUser(routeUserId)
            ) { currentUserId, notifications ->
                buildNotificationUiState(currentUserId, notifications)
            }.collect { loadedState ->
                _uiState.update { loadedState }
            }
        }
    }

    private suspend fun buildNotificationUiState(
        currentUserId: String?,
        notifications: List<Notification>
    ): NotificationUiState {
        if (currentUserId == null) {
            return NotificationUiState(isLoading = false)
        }

        if (routeUserId != currentUserId) {
            return NotificationUiState(
                isLoading = false,
                globalError = "Unauthorized access"
            )
        }

        return try {
            val uiNotifications = notifications.map { notification ->
                val triggerUser = userRepository.getUserById(notification.triggerUserId).first()
                val recipeTitle = notification.recipeId?.let { id ->
                    recipeRepository.getRecipeById(id).first()?.title
                }
                notification.toUi(
                    triggerUsername = triggerUser?.nickname ?: "Unknown User",
                    triggerFirstName = triggerUser?.firstName ?: "",
                    triggerLastName = triggerUser?.lastName ?: "",
                    triggerUserAvatarUrl = triggerUser?.profileImage?.avatarUrl(),
                    recipeTitle = recipeTitle
                )
            }

            NotificationUiState(
                unreadNotifications = uiNotifications.filter { !it.isRead }
                    .sortedByDescending { it.timestamp },
                readNotifications = uiNotifications.filter { it.isRead }
                    .sortedByDescending { it.timestamp },
                isLoading = false
            )
        } catch (e: Exception) {
            NotificationUiState(
                isLoading = false,
                globalError = "Error loading notifications: ${e.message}"
            )
        }
    }

    fun markAsRead(notificationId: String) {
        viewModelScope.launch {
            try {
                notificationRepository.markAsRead(notificationId)
            } catch (e: Exception) {
                _uiState.update { it.copy(globalError = "Error marking notification as read") }
            }
        }
    }

    fun deleteNotification(notificationId: String) {
        viewModelScope.launch {
            try {
                notificationRepository.deleteNotification(notificationId)
            } catch (e: Exception) {
                _uiState.update { it.copy(globalError = "Error deleting notification") }
            }
        }
    }

    fun clearAll() {
        viewModelScope.launch {
            try {
                notificationRepository.clearAllForUser(routeUserId)
            } catch (e: Exception) {
                _uiState.update { it.copy(globalError = "Error clearing notifications") }
            }
        }
    }


    // ─────────────────────────────────────────────────────────────────────────
    // Factory
    // ─────────────────────────────────────────────────────────────────────────

    companion object {
        val Factory = viewModelFactory {
            initializer {
                val savedStateHandle = createSavedStateHandle()
                val app = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as FridgeApplication
                NotificationViewModel(
                    savedStateHandle = savedStateHandle,
                    notificationRepository = app.container.notificationRepository,
                    userRepository = app.container.userRepository,
                    recipeRepository = app.container.recipeRepository
                )
            }
        }
    }
}
