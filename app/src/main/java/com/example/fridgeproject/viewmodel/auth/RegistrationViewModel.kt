package com.example.fridgeproject.viewmodel.auth

import com.example.fridgeproject.viewmodel.*

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.fridgeproject.FridgeApplication
import com.example.fridgeproject.data.repository.auth.SessionManagerFacade
import com.example.fridgeproject.domain.UserRepository
import com.example.fridgeproject.model.UserProfile
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow


class RegistrationViewModel(
    private val userRepository: UserRepository
) : ViewModel() {

    private val _events = Channel<AuthEvent>()
    val events = _events.receiveAsFlow()


    private val _uiState = MutableStateFlow(RegistrationUiState())
    val uiState: StateFlow<RegistrationUiState> = _uiState.asStateFlow()

    init {
        val googleEmail = SessionManagerFacade.currentUserEmail.orEmpty()
        val googleName = SessionManagerFacade.currentUserDisplayName.orEmpty()

        val nameParts = googleName.split(" ")
        val firstName = nameParts.getOrNull(0).orEmpty()
        val lastName = if (nameParts.size > 1) nameParts.drop(1).joinToString(" ") else ""

        _uiState.update {
            it.copy(
                email = googleEmail,
                firstName = firstName,
                lastName = lastName
            )
        }
    }
    
    fun skipRegistration() {
        viewModelScope.launch {
            _events.send(AuthEvent.OnboardingSkipped)
        }
    }
    
    fun saveProfile() {
        val state = _uiState.value

        if (state.firstName.isBlank() || state.lastName.isBlank() || state.nickname.isBlank()) {
            _uiState.update { it.copy(errorMessage = "All fields are required!") }
            return
        }

        viewModelScope.launch {
            val currentId = SessionManagerFacade.currentUserId ?: return@launch

            val newProfile = UserProfile(
                id = currentId,
                firstName = state.firstName.trim(),
                lastName = state.lastName.trim(),
                nickname = state.nickname.trim(),
                email = state.email
            )

            try {
                userRepository.saveUser(newProfile)
                SessionManagerFacade.saveFcmToken(currentId)
                _uiState.update { it.copy(isProfileSaved = true, errorMessage = null) }
            } catch (e: Exception) {
                _uiState.update { it.copy(errorMessage = "Database Error: ${e.localizedMessage}") }
            }
        }
    }

    fun updateFirstName(name: String) { _uiState.update { it.copy(firstName = name) } }

    fun updateLastName(lastName: String) { _uiState.update { it.copy(lastName = lastName) } }

    fun updateNickname(nickname: String) { _uiState.update { it.copy(nickname = nickname) } }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as FridgeApplication
                RegistrationViewModel(
                    userRepository = app.container.userRepository
                )
            }
        }
    }
}