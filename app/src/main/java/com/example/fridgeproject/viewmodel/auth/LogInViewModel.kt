package com.example.fridgeproject.viewmodel.auth

import com.example.fridgeproject.viewmodel.*

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.fridgeproject.FridgeApplication
import com.example.fridgeproject.data.repository.auth.SessionManagerFacade
import com.example.fridgeproject.domain.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow

class LogInViewModel(
    private val userRepository: UserRepository,
    savedStateHandle: SavedStateHandle
    ) : ViewModel() {

    private val _events = Channel<AuthEvent>()
    val events = _events.receiveAsFlow()


    private val _uiState = MutableStateFlow(LogInUiState())
    val uiState: StateFlow<LogInUiState> = _uiState.asStateFlow()

    fun startGoogleLogIn() {
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
    }

    fun completeGoogleLogIn(onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            val userId = SessionManagerFacade.currentUserId
            if(userId != null) {
                val userProfile = userRepository.getUserById(userId).first()
                _uiState.update { it.copy(isLoading = false) }
                if (userProfile != null) {
                    SessionManagerFacade.saveFcmToken(userId)
                    _events.send(AuthEvent.LoggedIn)
                }
                onResult(userProfile != null)
            } else {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    fun failGoogleLogIn(error: Throwable) {
        _uiState.update {
            it.copy(
                isLoading = false,
                errorMessage = error.localizedMessage ?: "Error during Google login"
            )
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as FridgeApplication
                val savedStateHandle = createSavedStateHandle()

                LogInViewModel(
                    userRepository = app.container.userRepository,
                    savedStateHandle = savedStateHandle,
                )
            }
        }
    }
}