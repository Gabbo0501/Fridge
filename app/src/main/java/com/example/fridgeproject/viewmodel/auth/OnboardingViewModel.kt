package com.example.fridgeproject.viewmodel.auth

import com.example.fridgeproject.viewmodel.*

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.fridgeproject.FridgeApplication
import com.example.fridgeproject.data.repository.auth.SessionManagerFacade
import com.example.fridgeproject.domain.IngredientRepository
import com.example.fridgeproject.domain.UserRepository
import com.example.fridgeproject.model.Ingredient
import com.example.fridgeproject.model.ProfileImageSource
import com.example.fridgeproject.model.SocialProfile
import com.example.fridgeproject.model.UserProfile
import com.example.fridgeproject.model.enums.AllergenWizardStep
import com.example.fridgeproject.model.enums.CookingRole
import com.example.fridgeproject.model.enums.Diet
import com.example.fridgeproject.model.enums.IngredientCategory
import com.example.fridgeproject.model.enums.OnboardingStep
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow

class OnboardingViewModel(
    private val userRepository: UserRepository,
    private val ingredientRepository: IngredientRepository
) : ViewModel() {

    private val _events = Channel<AuthEvent>()
    val events = _events.receiveAsFlow()


    private val _uiState = MutableStateFlow(OnboardingUiState())
    val uiState: StateFlow<OnboardingUiState> = _uiState.asStateFlow()

    init {
        loadUserData()
    }


    // ─────────────────────────────────────────────────────────────────────────
    // Asynchronous actions (repository I/O)
    // ─────────────────────────────────────────────────────────────────────────
    
    private fun loadUserData() {
        viewModelScope.launch {
            val loggedUserId = SessionManagerFacade.currentUserId!!
            val user = userRepository.getUserById(loggedUserId).filterNotNull().first()
            _uiState.update { it.copy(
                firstName = user.firstName,
                lastName = user.lastName
            ) }
        }
    }

    
    // ─────────────────────────────────────────────────────────────────────────
    // Synchronous UI state updates
    // ─────────────────────────────────────────────────────────────────────────

    fun saveInitialProfile(onSaved: () -> Unit) {
        viewModelScope.launch {
            val currentUid = SessionManagerFacade.currentUserId ?: return@launch
            val current = _uiState.value
            val existingUser = userRepository.getUserById(currentUid).first()
            val pendingProfileImage = (current.profileImage as? ProfileImageSource.Local)?.input

            val userToSave = (existingUser ?: UserProfile(id = currentUid)).copy(
                firstName = current.firstName,
                lastName = current.lastName,
                profileImage = current.profileImage,
                cookingRole = current.cookingRole,
                shortBio = current.shortBio.ifBlank { null },
                socialProfiles = current.socialProfiles,
                diet = current.diet,
                allergens = current.allergens
            )

            userRepository.saveUser(
                user = userToSave,
                pendingProfileImage = pendingProfileImage
            )
            _events.send(AuthEvent.ProfileCreated)
            onSaved()
        }
    }

    fun nextStep(): Boolean {
        return when (uiState.value.currentStep) {
            OnboardingStep.PROFILE -> {
                _uiState.update { it.copy(currentStep = OnboardingStep.DIET) }
                false
            }
            OnboardingStep.DIET -> {
                true
            }
        }
    }

    fun previousStep() {
        if (uiState.value.currentStep == OnboardingStep.DIET) {
            _uiState.update { it.copy(currentStep = OnboardingStep.PROFILE) }
        }
    }

    fun confirmSkip() {
        viewModelScope.launch {
            _events.send(AuthEvent.OnboardingSkipped)
        }
    }

    fun requestExit() {
        _uiState.update { it.copy(showExitDialog = true) }
    }

    fun confirmExit() {
        _uiState.update { it.copy(showExitDialog = false) }
        viewModelScope.launch { _events.send(AuthEvent.ExitAllowed) }
    }

    fun dismissExitDialog() {
        _uiState.update { it.copy(showExitDialog = false) }
        viewModelScope.launch { _events.send(AuthEvent.ExitCancelled) }
    }

    fun updateProfileImage(value: ProfileImageSource) {
        _uiState.update { it.copy(profileImage = value) }
    }

    fun removeProfileImage() {
        _uiState.update { it.copy(profileImage = ProfileImageSource.Monogram) }
    }

    fun updateCookingRole(value: CookingRole) {
        _uiState.update { it.copy(cookingRole = value) }
    }

    fun updateShortBio(value: String) {
        _uiState.update { it.copy(shortBio = value) }
    }

    fun updateSocialProfiles(value: List<SocialProfile>) {
        _uiState.update { it.copy(socialProfiles = value) }
    }

    fun updateDiet(value: Diet) {
        _uiState.update { it.copy(diet = value) }
    }

    fun openAllergenWizard() {
        _uiState.update {
            it.copy(
                addingAllergen = true,
                allergenWizardStep = AllergenWizardStep.CATEGORY,
                selectedAllergenCategory = IngredientCategory.OTHERS,
                selectableAllergens = emptyList(),
                selectedAllergenName = ""
            )
        }
    }

    fun closeAllergenWizard() {
        _uiState.update {
            it.copy(
                addingAllergen = false,
                allergenWizardStep = AllergenWizardStep.CATEGORY,
                selectedAllergenCategory = IngredientCategory.OTHERS,
                selectableAllergens = emptyList(),
                selectedAllergenName = ""
            )
        }
    }

    fun backAllergenWizard() {
        when (uiState.value.allergenWizardStep) {
            AllergenWizardStep.INGREDIENT -> {
                _uiState.update {
                    it.copy(
                        allergenWizardStep = AllergenWizardStep.CATEGORY,
                        selectedAllergenName = "",
                        selectableAllergens = emptyList()
                    )
                }
            }
            else -> closeAllergenWizard()
        }
    }

    fun updateAllergenCategory(value: IngredientCategory) {
        _uiState.update { it.copy(selectedAllergenCategory = value) }
    }

    fun confirmAllergenCategory() {
        val category = uiState.value.selectedAllergenCategory
        if (category == IngredientCategory.OTHERS) return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val ingredients = ingredientRepository.getIngredientsByCategory(category).first()
                _uiState.update {
                    it.copy(
                        allergenWizardStep = AllergenWizardStep.INGREDIENT,
                        selectableAllergens = ingredients,
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = "Error during allergen category confirmation: {${e.message}") }
            }
        }
    }

    fun updateAllergenName(value: String) {
        _uiState.update { it.copy(selectedAllergenName = value) }
    }

    fun addSelectedAllergen() {
        val ingredient = uiState.value.selectableAllergens
            .firstOrNull { it.name == uiState.value.selectedAllergenName } ?: return

        _uiState.update {
            val alreadySelected = it.allergens.any { allergen -> allergen.name == ingredient.name }
            it.copy(
                allergens = if (alreadySelected) it.allergens else it.allergens + ingredient,
                addingAllergen = false,
                allergenWizardStep = AllergenWizardStep.CATEGORY,
                selectedAllergenCategory = IngredientCategory.OTHERS,
                selectableAllergens = emptyList(),
                selectedAllergenName = ""
            )
        }
    }

    fun removeAllergen(value: Ingredient) {
        _uiState.update { it.copy(allergens = it.allergens.filterNot { allergen -> allergen.name == value.name }) }
    }


    // ─────────────────────────────────────────────────────────────────────────
    // Factory
    // ─────────────────────────────────────────────────────────────────────────

    companion object {
        val Factory = viewModelFactory {
            initializer {
                val app = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as FridgeApplication
                OnboardingViewModel(
                    userRepository = app.container.userRepository,
                    ingredientRepository = app.container.ingredientRepository
                )
            }
        }
    }
}