package com.example.fridgeproject.viewmodel.user

import com.example.fridgeproject.viewmodel.*

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
import com.example.fridgeproject.domain.UserRepository
import com.example.fridgeproject.model.ProfileImageSource
import com.example.fridgeproject.model.SocialProfile
import com.example.fridgeproject.model.UserProfile
import com.example.fridgeproject.model.enums.CookingRole
import com.example.fridgeproject.navigation.EditProfileRoute
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class EditProfileViewModel(
    savedStateHandle: SavedStateHandle,
    private val userRepository: UserRepository
) : ViewModel(){

    private val route = savedStateHandle.toRoute<EditProfileRoute>()
    private val userId = route.profileUserId
    private var originalProfile: UserProfile? = null

    private val _uiState = MutableStateFlow(EditProfileUiState())
    val uiState: StateFlow<EditProfileUiState> = _uiState.asStateFlow()
    private val _events = Channel<SettingsEvent>()
    val events = _events.receiveAsFlow()

    val isUserLoggedIn: StateFlow<Boolean> = SessionManagerFacade.currentUserStateFlow
        .map { it != null }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = SessionManagerFacade.isLoggedIn
        )

    init {
        // Caricamento iniziale
        loadUserInfo(userId)

        // Aggiornamento reattivo sullo stato di login
        viewModelScope.launch {
            isUserLoggedIn.collect { loggedIn ->
                if (loggedIn) {
                    loadUserInfo(userId)
                } else { // utente sloggato, pulisco lo stato
                    _uiState.update { EditProfileUiState() }
                }
            }
        }
    }


    // ─────────────────────────────────────────────────────────────────────────
    // Asynchronous actions (repository I/O)
    // ─────────────────────────────────────────────────────────────────────────

    fun loadUserInfo(id: String) {
        val currentUserId = SessionManagerFacade.currentUserId
        val isLoggedIn = currentUserId != null

        if (!isLoggedIn || id != currentUserId) {
            _uiState.update {
                it.copy(errors = it.errors.copy(globalError = "Unauthorized access"))
            }
            return
        }

        viewModelScope.launch {
            val profile = userRepository.getUserById(id).first()
            if (profile == null){
                _uiState.update {
                    it.copy(
                        errors = it.errors.copy(globalError = "ERROR: User info not found!")
                    )
                }
            } else {
                originalProfile = profile
                _uiState.update {
                    it.copy(
                        firstName = profile.firstName,
                        lastName = profile.lastName,
                        email = profile.email,
                        profileImage = profile.profileImage,
                        nickname = profile.nickname,
                        shortBio = profile.shortBio,
                        phoneNumber = profile.phoneNumber,
                        socialProfiles = profile.socialProfiles,
                        cookingRole = profile.cookingRole,
                        success = false
                    )
                }
            }
        }
    }

    fun saveProfile() {
        val currentUserId = SessionManagerFacade.currentUserId
        val isLoggedIn = currentUserId != null

        if (!isLoggedIn || userId != currentUserId) {
            _uiState.update { it.copy(errors = it.errors.copy(globalError = "Session expired. Login again.")) }
            return
        }

        if (validateFields()) {
            viewModelScope.launch {
                val profileBeforeEdit = userRepository.getUserById(userId).first()
                if (profileBeforeEdit != null) {
                    val selectedProfileImage = uiState.value.profileImage
                    val pendingProfileImage = (selectedProfileImage as? ProfileImageSource.Local)?.input
                    val profileToSave = profileBeforeEdit.copy(
                        profileImage = selectedProfileImage,
                        nickname = uiState.value.nickname,
                        shortBio = uiState.value.shortBio,
                        phoneNumber = uiState.value.phoneNumber,
                        socialProfiles = uiState.value.socialProfiles,
                        cookingRole = uiState.value.cookingRole,
                    )

                    userRepository.saveUser(
                        user = profileToSave,
                        pendingProfileImage = pendingProfileImage
                    )
                    originalProfile = profileToSave
                    _uiState.update {
                        it.copy( success= true, errors = EditProfileErrors() )
                    }
                    _events.send(SettingsEvent.ProfileUpdated)
                }
            }
        }
    }


    // ─────────────────────────────────────────────────────────────────────────
    // Synchronous UI state updates
    // ─────────────────────────────────────────────────────────────────────────

    private fun validateFields(): Boolean {
        var isValid = true

        val errors = EditProfileErrors(
            nickname = if (uiState.value.nickname.isBlank()) { // TODO: deve anche essere univoco?
                isValid = false
                "Nickname cannot be empty"
            } else "",
            phoneNumber = if (
                uiState.value.phoneNumber != null &&
                !uiState.value.phoneNumber!!.matches(Regex("^[+]?[0-9\\s\\-().]{7,20}$"))
            ) {
                isValid = false
                "Invalid phone number format"
            } else "",
            shortBio = if (uiState.value.shortBio != null && uiState.value.shortBio!!.isBlank()) {
                isValid = false
                "Bio cannot contain only spaces"
            } else if (uiState.value.shortBio != null && uiState.value.shortBio!!.length > 200) {
                isValid = false
                "Bio must be under 200 characters"
            } else "",
            socialProfiles = if (uiState.value.socialProfiles.any { it.username.isBlank() }) {
                isValid = false
                val invalid = uiState.value.socialProfiles
                    .filter { it.username.isBlank() }
                    .joinToString(", ") { it.platform.name }
                "Username missing for: $invalid"
            } else ""
        )

        _uiState.update { it.copy(errors = errors) }
        return isValid
    }

    fun hasUnsavedChanges(): Boolean {
        val current = _uiState.value
        val original = originalProfile ?: return false
        return current.canCheckUnsavedChanges() && current.hasChangesFrom(original)
    }

    fun requestExit() {
        if (hasUnsavedChanges()) {
            _uiState.update { it.copy(showExitDialog = true) }
        } else {
            viewModelScope.launch { _events.send(SettingsEvent.ExitAllowed) }
        }
    }

    fun confirmExit() {
        _uiState.update { it.copy(showExitDialog = false) }
        viewModelScope.launch { _events.send(SettingsEvent.ExitAllowed) }
    }

    fun dismissExitDialog() {
        _uiState.update { it.copy(showExitDialog = false) }
        viewModelScope.launch { _events.send(SettingsEvent.ExitCancelled) }
    }

    fun updateProfileImage(value: ProfileImageSource) {
        _uiState.update { it.copy(profileImage = value) }
    }

    fun removeProfileImage() {
        _uiState.update { it.copy(profileImage = ProfileImageSource.Monogram) }
    }

    fun updateNickname(value: String) {
        _uiState.update { it.copy(nickname = value) }
    }

    fun updatePhoneNumber(value: String) {
        _uiState.update { it.copy(phoneNumber = value)}
    }

    fun updateShortBio(value: String) {
        _uiState.update { it.copy(shortBio = value) }
    }

    fun updateSocialProfiles(value: List<SocialProfile>) {
        _uiState.update { it.copy(socialProfiles = value) }
    }

    fun updateCookingRole(value: CookingRole) {
        _uiState.update { it.copy(cookingRole = value) }
    }


    // ─────────────────────────────────────────────────────────────────────────
    // Factory
    // ─────────────────────────────────────────────────────────────────────────

    companion object {
        val Factory = viewModelFactory {
            initializer {
                val savedStateHandle = createSavedStateHandle()
                val app = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as FridgeApplication
                EditProfileViewModel(
                    savedStateHandle = savedStateHandle,
                    userRepository = app.container.userRepository
                )
            }
        }
    }
}