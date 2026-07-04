package com.example.fridgeproject.viewmodel.fridge

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
import com.example.fridgeproject.domain.FridgeRepository
import com.example.fridgeproject.domain.IngredientRepository
import com.example.fridgeproject.model.*
import com.example.fridgeproject.model.enums.IngredientCategory
import com.example.fridgeproject.model.enums.IngredientWizardStep
import com.example.fridgeproject.model.enums.UnitOfMeasure
import com.example.fridgeproject.model.enums.defaultIngredientQuantity
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.collections.plus

class FridgeViewModel(
    private val fridgeRepository: FridgeRepository,
    private val ingredientRepository: IngredientRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(FridgeUiState(isLoading = true))
    val uiState: StateFlow<FridgeUiState> = _uiState.asStateFlow()

    private val _events = Channel<FridgeEvent>()
    val events = _events.receiveAsFlow()

    init {
        viewModelScope.launch {
            SessionManagerFacade.currentUserStateFlow.collectLatest { authUserId ->
                val isLoggedIn = authUserId != null

                if (!isLoggedIn) {
                    _uiState.update {
                        it.copy(
                            fridgeIngredients = emptyList(),
                            isLoading = false,
                            error = null
                        )
                    }
                    return@collectLatest
                }

                _uiState.update { it.copy(isLoading = true) }

                fridgeRepository.getFridgeByOwner(authUserId).collect { fridge ->
                    _uiState.update {
                        it.copy(
                            fridgeId = fridge?.id,
                            fridgeIngredients = fridge?.ingredients?.sortedBy { it.ingredient.name } ?: emptyList(),
                            isLoading = false,
                            error = null
                        )
                    }
                }
            }
        }
    }


    // ─────────────────────────────────────────────────────────────────────────
    // Asynchronous actions (repository I/O)
    // ─────────────────────────────────────────────────────────────────────────

    fun addIngredient(ingredient: IngredientQuantity) {
        viewModelScope.launch {
            try {
                fridgeRepository.addIngredientToFridge(ingredient.toWithTime())
                _uiState.update {
                    it.copy(addingIngredient = false)
                }
                _events.send(FridgeEvent.FridgeUpdated)
            } catch (e: Exception) {
                _uiState.update { it.copy(error = "Could not add ingredient: ${e.message}") }
            }
        }
    }

    fun updateIngredient(ingredientName: String) {
        _uiState.update { state ->
            val ingredientToUpdate = state.fridgeIngredients.find { it.ingredient.name == ingredientName }

            if (ingredientToUpdate != null) {
                val ingredientQuantity = IngredientQuantity(
                    ingredient = ingredientToUpdate.ingredient,
                    quantity = ingredientToUpdate.quantity,
                    unit = ingredientToUpdate.unit
                )

                val textToDisplay = if (ingredientToUpdate.unit == UnitOfMeasure.QB) "Q.B." else ingredientQuantity.quantity.toInt().toString()

                state.copy(
                    addingIngredient = true,
                    addingIngredientStep = IngredientWizardStep.QUANTITY,
                    newIngredient = ingredientQuantity,
                    quantityInput = textToDisplay,
                    isConfirmQuantityEnabled = true,
                    isEditMode = true
                )
            } else {
                state
            }
        }
    }


    fun removeIngredient(ingredientName: String) {
        viewModelScope.launch {
            try {
                fridgeRepository.removeIngredientFromFridge(ingredientName)
                _uiState.update { it.copy(addingIngredient = false) }
                _events.send(FridgeEvent.FridgeUpdated)
            } catch (e: Exception) {
                _uiState.update { it.copy(error = "Could not remove ingredient: ${e.message}") }
            }
        }
    }

    fun requestClearFridge() {
        _uiState.update { it.copy(showClearFridgeDialog = true) }
    }

    fun dismissClearFridgeDialog() {
        _uiState.update { it.copy(showClearFridgeDialog = false) }
    }

    fun selectTab(index: Int) {
        _uiState.update { it.copy(selectedTabIndex = index) }
    }

    fun clearFridge() {
        val fridgeId = _uiState.value.fridgeId ?: return
        _uiState.update { it.copy(showClearFridgeDialog = false) }

        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isLoading = true) }
                fridgeRepository.emptyFridge(fridgeId)
                _events.send(FridgeEvent.FridgeCleared)
                _uiState.update { it.copy(isLoading = false) }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = "Could not clear fridge: ${e.message}", isLoading = false) }
            }
        }
    }

    // Procedura per gli ingredienti

    fun initAddIngredientProcedure() {
        _uiState.update {
            it.copy(
                addingIngredient = true,
                addingIngredientStep = IngredientWizardStep.CATEGORY,
                newIngredient = IngredientQuantity()
            )
        }
    }

    fun cancelAddIngredientProcedure(){
        _uiState.update {
            it.copy(
                addingIngredient = false,
            )
        }
    }

    fun hasUnsavedChanges(): Boolean =
        uiState.value.addingIngredient

    fun requestExit() {
        if (hasUnsavedChanges()) {
            _uiState.update { it.copy(showExitDialog = true) }
        } else {
            viewModelScope.launch { _events.send(FridgeEvent.ExitAllowed) }
        }
    }

    fun confirmExit() {
        _uiState.update { it.copy(showExitDialog = false) }
        viewModelScope.launch { _events.send(FridgeEvent.ExitAllowed) }
    }

    fun dismissExitDialog() {
        _uiState.update { it.copy(showExitDialog = false) }
        viewModelScope.launch { _events.send(FridgeEvent.ExitCancelled) }
    }

    fun updateNewIngredientCategory(category: IngredientCategory){
        _uiState.update { state ->
            val updatedIngredient = state.newIngredient.ingredient.copy(
                category = category
            )
            val updatedNewIngredient = state.newIngredient.copy(
                ingredient = updatedIngredient
            )
            state.copy(
                newIngredient = updatedNewIngredient
            )
        }
    }

    fun updateNewIngredientName(name: String) {
        _uiState.update { state ->
            val dbIngredient = state.selectableIngredients.find { it.name == name }
            val lockedUnit = dbIngredient?.defaultUnit ?: UnitOfMeasure.G

            val updatedIngredient = state.newIngredient.ingredient.copy(
                name = name,
                category = dbIngredient?.category ?: state.newIngredient.ingredient.category,
                defaultUnit = lockedUnit
            )

            val initialText = if (lockedUnit == UnitOfMeasure.QB) "q.b." else {
                val defaultQty = lockedUnit.defaultIngredientQuantity()
                if (defaultQty == defaultQty.toLong().toFloat()) defaultQty.toLong().toString() else defaultQty.toString()
            }

            state.copy(
                newIngredient = state.newIngredient.copy(
                    ingredient = updatedIngredient,
                    unit = lockedUnit,
                    quantity = if (lockedUnit == UnitOfMeasure.QB) 1f else lockedUnit.defaultIngredientQuantity()
                ),
                quantityInput = initialText,
                isConfirmQuantityEnabled = true
            )
        }
    }

    fun updateNewIngredientQuantityText(text: String) {
        val normalized = text.replace(',', '.')

        if (normalized.isEmpty() || normalized.matches(Regex("""^\d*\.?\d*$"""))) {
            _uiState.update { state ->
                val parsed = normalized.toFloatOrNull()
                val isValid = parsed != null && parsed > 0f

                state.copy(
                    quantityInput = normalized,
                    isConfirmQuantityEnabled = isValid,
                    newIngredient = if (isValid) {
                        state.newIngredient.copy(quantity = parsed)
                    } else {
                        state.newIngredient
                    }
                )
            }
        }
    }

    fun incrementNewIngredientQuantity() {
        _uiState.update { state ->
            val currentUnit = state.newIngredient.unit
            if (currentUnit == UnitOfMeasure.QB) return@update state // QB non si incrementa

            val step = when (currentUnit) {
                UnitOfMeasure.KG -> 0.1f
                UnitOfMeasure.PCS -> 1f
                else -> 10f
            }

            val nextFloat = (kotlin.math.round((state.newIngredient.quantity + step) * 10f) / 10f)
            val nextString = if (nextFloat == nextFloat.toLong().toFloat()) nextFloat.toLong().toString() else "%.1f".format(nextFloat)

            state.copy(
                newIngredient = state.newIngredient.copy(quantity = nextFloat),
                quantityInput = nextString,
                isConfirmQuantityEnabled = nextFloat > 0f
            )
        }
    }

    fun decrementNewIngredientQuantity() {
        _uiState.update { state ->
            val currentUnit = state.newIngredient.unit
            if (currentUnit == UnitOfMeasure.QB) return@update state // QB non si decrementa

            val step = when (currentUnit) {
                UnitOfMeasure.KG -> 0.1f
                UnitOfMeasure.PCS -> 1f
                else -> 10f
            }

            val nextFloat = (kotlin.math.round((state.newIngredient.quantity - step) * 10f) / 10f)
            if (nextFloat < step) return@update state // Per evitare valori negativi o meno del minimo

            val nextString = if (nextFloat == nextFloat.toLong().toFloat()) nextFloat.toLong().toString() else "%.1f".format(nextFloat)

            state.copy(
                newIngredient = state.newIngredient.copy(quantity = nextFloat),
                quantityInput = nextString,
                isConfirmQuantityEnabled = nextFloat > 0f
            )
        }
    }

    fun updateNewIngredientQuantity( qty: Float){
        _uiState.update { state ->
            val quantity = if (qty > 0f) qty else state.newIngredient.unit.defaultIngredientQuantity()
            state.copy(newIngredient = state.newIngredient.copy(quantity = quantity))
        }
    }

    fun previousIngredientWizardStep() {
        _uiState.update {
            when (it.addingIngredientStep) {
                IngredientWizardStep.INGREDIENT -> it.copy(
                    addingIngredientStep = IngredientWizardStep.CATEGORY,
                    newIngredient = it.newIngredient.copy(
                        ingredient = it.newIngredient.ingredient.copy(name = "")
                    )
                )
                IngredientWizardStep.QUANTITY -> it.copy(
                    addingIngredientStep = IngredientWizardStep.INGREDIENT,
                    newIngredient = it.newIngredient.copy(
                        quantity = 0f,
                        unit = UnitOfMeasure.G
                    )
                )
                IngredientWizardStep.CATEGORY -> it
            }
        }
    }

    fun nextIngredientWizardStep() {
        val current = uiState.value
        when (current.addingIngredientStep) {
            IngredientWizardStep.CATEGORY -> {
                val category = current.newIngredient.ingredient.category
                if (category != IngredientCategory.OTHERS) {
                    loadSelectableIngredientsForCategory(category)
                }
            }
            IngredientWizardStep.INGREDIENT -> {
                if (current.newIngredient.ingredient.name.isNotBlank()) {
                    _uiState.update {
                        it.copy(
                            addingIngredientStep = IngredientWizardStep.QUANTITY,
                            newIngredient = it.newIngredient.copy(
                                quantity = it.newIngredient.unit.defaultIngredientQuantity()
                            )
                        )
                    }
                }
            }
            IngredientWizardStep.QUANTITY -> Unit
        }
    }

    private fun loadSelectableIngredientsForCategory(category: IngredientCategory) {
        viewModelScope.launch {
            try {
                val ingredientsFromDb = ingredientRepository.getIngredientsByCategory(category).first()

                _uiState.update { state ->
                    val alreadySelected = state.fridgeIngredients.map { it.ingredient }
                    val filteredIngredients = ingredientsFromDb.filterNot { it in alreadySelected }

                    state.copy(
                        selectableIngredients = filteredIngredients,
                        addingIngredientStep = IngredientWizardStep.INGREDIENT
                    )
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = "Error loading ingredients: ${e.message}") }
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
                FridgeViewModel(
                    fridgeRepository = app.container.fridgeRepository,
                    ingredientRepository = app.container.ingredientRepository
                )
            }
        }
    }
}