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
import com.example.fridgeproject.domain.UserRepository
import com.example.fridgeproject.domain.IngredientRepository // <-- Importato
import com.example.fridgeproject.model.Ingredient
import com.example.fridgeproject.model.enums.AllergenWizardStep
import com.example.fridgeproject.model.enums.Diet
import com.example.fridgeproject.model.enums.IngredientCategory
import com.example.fridgeproject.navigation.DietRoute
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class EditDietViewModel(
    savedStateHandle: SavedStateHandle,
    private val userRepository: UserRepository,
    private val ingredientRepository: IngredientRepository
) : ViewModel() {

    private val route = savedStateHandle.toRoute<DietRoute>()
    private val userId = route.profileUserId
    private var originalDiet: Diet? = null
    private var originalAllergens: List<Ingredient> = emptyList()

    private val _uiState = MutableStateFlow(EditDietUiState())
    val uiState: StateFlow<EditDietUiState> = _uiState.asStateFlow()
    private val _events = Channel<SettingsEvent>()
    val events = _events.receiveAsFlow()

    init {
        loadUserPreference(userId)
    }


    // ─────────────────────────────────────────────────────────────────────────
    // Asynchronous actions (repository I/O)
    // ─────────────────────────────────────────────────────────────────────────

    fun loadUserPreference(id: String) {
        viewModelScope.launch {
            val profile = userRepository.getUserById(id).first()
            if (profile == null) _uiState.update { it.copy(globalError = "ERROR: User info not found!") }
            else {
                originalDiet = profile.diet
                originalAllergens = profile.allergens
                _uiState.update {
                    it.copy(
                        diet = profile.diet,
                        allergens = profile.allergens
                    )
                }
            }
        }
    }

    fun savePreference() {
        viewModelScope.launch {
            try {
                val originalProfile = userRepository.getUserById(userId).first()
                if (originalProfile != null) {
                    userRepository.saveUser(originalProfile.copy(
                        diet = uiState.value.diet,
                        allergens = uiState.value.allergens
                    ))
                    originalDiet = uiState.value.diet
                    originalAllergens = uiState.value.allergens
                    _uiState.update {
                        it.copy( globalError = "" )
                    }
                    _events.send(SettingsEvent.PreferencesUpdated)
                }
            } catch (e: Exception) {
                _uiState.update { it.copy( globalError = "ERROR: ${e.message}" ) }
            }
        }
    }


    // ─────────────────────────────────────────────────────────────────────────
    // Synchronous UI state updates
    // ─────────────────────────────────────────────────────────────────────────

    fun updateDiet(value: Diet) {
        _uiState.update { it.copy(diet = value)}
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
            try {
                val ingredients = ingredientRepository.getIngredientsByCategory(category).first()
                _uiState.update {
                    it.copy(
                        allergenWizardStep = AllergenWizardStep.INGREDIENT,
                        selectableAllergens = ingredients
                    )
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(globalError = "ERROR: Cannot load ingredients.") }
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

    fun removeAllergens(value: Ingredient) {
        _uiState.update { it.copy(allergens = it.allergens - value) }
    }

    fun hasUnsavedChanges(): Boolean {
        val original = originalDiet ?: return false
        val current = uiState.value
        return current.canCheckUnsavedChanges() && current.hasChangesFrom(original, originalAllergens)
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


    // ─────────────────────────────────────────────────────────────────────────
    // Factory
    // ─────────────────────────────────────────────────────────────────────────

    companion object {
        val Factory = viewModelFactory {
            initializer {
                val savedStateHandle = createSavedStateHandle()
                val app = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as FridgeApplication
                EditDietViewModel(
                    savedStateHandle = savedStateHandle,
                    userRepository = app.container.userRepository,
                    ingredientRepository = app.container.ingredientRepository // <-- Fornito dal container di DI
                )
            }
        }
    }
}