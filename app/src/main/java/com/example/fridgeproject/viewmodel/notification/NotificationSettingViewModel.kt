package com.example.fridgeproject.viewmodel.notification

import com.example.fridgeproject.viewmodel.*

import com.example.fridgeproject.navigation.NotificationSettingsRoute

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.navigation.toRoute
import com.example.fridgeproject.FridgeApplication
import com.example.fridgeproject.domain.UserRepository
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class NotificationSettingViewModel(
    savedStateHandle: SavedStateHandle,
    private val userRepository: UserRepository
) : ViewModel() {

    private val route = savedStateHandle.toRoute<NotificationSettingsRoute>()
    private val userId = route.profileUserId

    private val _uiState = MutableStateFlow(NotificationSettingUiState())
    val uiState: StateFlow<NotificationSettingUiState> = _uiState.asStateFlow()
    private val _events = Channel<SettingsEvent>()
    val events = _events.receiveAsFlow()

    init {
        loadUserNotificationSettings(userId)
    }


    // ─────────────────────────────────────────────────────────────────────────
    // Asynchronous actions (repository I/O)
    // ─────────────────────────────────────────────────────────────────────────

    private fun loadUserNotificationSettings(id: String) {
        viewModelScope.launch {
            val profile = userRepository.getUserById(id).first()
            if (profile == null) _uiState.update {it.copy( globalError = "ERROR: User info not found!" )}
            else _uiState.update {
                it.copy(
                    receiveNotification = profile.receiveNotification,
                    receiveLikeNotification = profile.receiveLikeNotification,
                    receiveRemixNotification = profile.receiveRemixNotification,
                    receiveNewFollowerNotification = profile.receiveNewFollowerNotification,
                    receiveNewRecipeNotification = profile.receiveNewRecipeNotification,
                    receiveReviewNotification = profile.receiveReviewNotification,
                    receiveTipNotification = profile.receiveTipNotification
                )
            }
        }
    }

    fun saveNotificationSettings(triggerEvent: Boolean = true) {
        viewModelScope.launch {
            try {
                val originalProfile = userRepository.getUserById(userId).first()
                if (originalProfile != null) {
                    userRepository.saveUser(originalProfile.copy(
                        receiveNotification = uiState.value.receiveNotification,
                        receiveLikeNotification = uiState.value.receiveLikeNotification,
                        receiveRemixNotification = uiState.value.receiveRemixNotification,
                        receiveNewFollowerNotification = uiState.value.receiveNewFollowerNotification,
                        receiveNewRecipeNotification = uiState.value.receiveNewRecipeNotification,
                        receiveReviewNotification = uiState.value.receiveReviewNotification,
                        receiveTipNotification = uiState.value.receiveTipNotification
                    ))
                    _uiState.update {
                        it.copy( globalError = "" )
                    }
                    if (triggerEvent) {
                        _events.send(SettingsEvent.PreferencesUpdated)
                    }
                }
            }catch (e: Exception) {
                _uiState.update { it.copy(globalError = "Error: ${e.message}") }
            }

        }
    }


    // ─────────────────────────────────────────────────────────────────────────
    // Synchronous UI state updates
    // ─────────────────────────────────────────────────────────────────────────

    fun toggleReceiveNotification() {
        _uiState.update { it.copy(receiveNotification = !it.receiveNotification) }
        saveNotificationSettings(triggerEvent = false)
    }
    fun toggleReceiveLikeNotification() {
        _uiState.update { it.copy(receiveLikeNotification = !it.receiveLikeNotification) }
        saveNotificationSettings(triggerEvent = false)
    }
    fun toggleReceiveRemixNotification() {
        _uiState.update { it.copy(receiveRemixNotification = !it.receiveRemixNotification) }
        saveNotificationSettings(triggerEvent = false)
    }
    fun toggleReceiveNewFollowerNotification() {
        _uiState.update { it.copy(receiveNewFollowerNotification = !it.receiveNewFollowerNotification) }
        saveNotificationSettings(triggerEvent = false)
    }
    fun toggleReceiveNewRecipeNotification() {
        _uiState.update { it.copy(receiveNewRecipeNotification = !it.receiveNewRecipeNotification) }
        saveNotificationSettings(triggerEvent = false)
    }
    fun toggleReceiveReviewNotification() {
        _uiState.update { it.copy(receiveReviewNotification = !it.receiveReviewNotification) }
        saveNotificationSettings(triggerEvent = false)
    }
    fun toggleReceiveTipNotification() {
        _uiState.update { it.copy(receiveTipNotification = !it.receiveTipNotification) }
        saveNotificationSettings(triggerEvent = false)
    }


    // ─────────────────────────────────────────────────────────────────────────
    // Factory
    // ─────────────────────────────────────────────────────────────────────────

    companion object {
        val Factory = viewModelFactory {
            initializer {
                val savedStateHandle = createSavedStateHandle()
                val app = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as FridgeApplication
                NotificationSettingViewModel(
                    savedStateHandle = savedStateHandle,
                    userRepository = app.container.userRepository
                )
            }
        }
    }
}
