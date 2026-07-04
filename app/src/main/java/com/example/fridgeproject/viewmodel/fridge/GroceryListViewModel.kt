package com.example.fridgeproject.viewmodel.fridge

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.fridgeproject.FridgeApplication
import com.example.fridgeproject.data.repository.auth.SessionManagerFacade
import com.example.fridgeproject.data.useCase.MoveIngredientsToFridgeUseCase
import com.example.fridgeproject.domain.GroceryListRepository
import com.example.fridgeproject.domain.IngredientRepository
import com.example.fridgeproject.model.IngredientQuantity
import com.example.fridgeproject.model.IngredientQuantityWithTime
import com.example.fridgeproject.model.enums.IngredientCategory
import com.example.fridgeproject.model.enums.IngredientWizardStep
import com.example.fridgeproject.model.enums.UnitOfMeasure
import com.example.fridgeproject.model.enums.defaultIngredientQuantity
import com.example.fridgeproject.viewmodel.GroceryListEvent
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class GroceryListViewModel(
    private val groceryListRepository: GroceryListRepository,
    private val ingredientRepository: IngredientRepository,
    private val moveIngredientsToFridgeUseCase: MoveIngredientsToFridgeUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(GroceryListUiState())
    val uiState: StateFlow<GroceryListUiState> = _uiState.asStateFlow()

    private val _events = Channel<GroceryListEvent>()
    val events = _events.receiveAsFlow()
    init {
        viewModelScope.launch {
            SessionManagerFacade.currentUserStateFlow.collectLatest { authUserId ->
                val isLoggedIn = authUserId != null

                if (!isLoggedIn) {
                    _uiState.update {
                        it.copy(
                            groceryListIngredients = emptyList(),
                            isLoading = false,
                            error = null
                        )
                    }
                    return@collectLatest
                }

                _uiState.update { it.copy(isLoading = true) }

                groceryListRepository.getGroceryListByOwner(authUserId).collect { groceryList ->
                    _uiState.update {
                        it.copy(
                            groceryListId = groceryList?.id,
                            groceryListIngredients = groceryList?.ingredients?.sortedBy { it.ingredient.name } ?: emptyList(),
                            isLoading = false,
                            error = null
                        )
                    }
                }
            }
        }
    }

    fun requestClearGroceryList() {
        _uiState.update { it.copy(showClearGroceryListDialog = true) }
    }

    fun dismissClearGroceryListDialog() {
        _uiState.update { it.copy(showClearGroceryListDialog = false) }
    }

    fun clearGroceryList() {
        val groceryListId = _uiState.value.groceryListId ?: return
        _uiState.update { it.copy(showClearGroceryListDialog = false) }

        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isLoading = true) }
                groceryListRepository.clearGroceryList(groceryListId)
                _events.send(GroceryListEvent.GroceryListCleared)
                _uiState.update { it.copy(isLoading = false) }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = "Could not clear grocery list: ${e.message}", isLoading = false) }
            }
        }
    }
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
            viewModelScope.launch { _events.send(GroceryListEvent.ExitAllowed) }
        }
    }

    fun confirmExit() {
        _uiState.update { it.copy(showExitDialog = false) }
        viewModelScope.launch { _events.send(GroceryListEvent.ExitAllowed) }
    }

    fun dismissExitDialog() {
        _uiState.update { it.copy(showExitDialog = false) }
        viewModelScope.launch { _events.send(GroceryListEvent.ExitCancelled) }
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

    fun addIngredient(ingredient: IngredientQuantity) {
        viewModelScope.launch {
            try {
                groceryListRepository.addIngredientToGroceryList(ingredient.toWithTime())
                _uiState.update {
                    it.copy(addingIngredient = false)
                }
                _events.send(GroceryListEvent.GroceryListUpdated)
            } catch (e: Exception) {
                _uiState.update { it.copy(error = "Could not add ingredient: ${e.message}") }
            }
        }
    }
    fun removeIngredient(ingredientName: String) {
        viewModelScope.launch {
            try {
                groceryListRepository.removeIngredientFromGroceryList(ingredientName)
                _uiState.update { it.copy(addingIngredient = false) }
                _events.send(GroceryListEvent.GroceryListUpdated)
            } catch (e: Exception) {
                _uiState.update { it.copy(error = "Could not remove ingredient: ${e.message}") }
            }
        }
    }
    fun updateIngredient(ingredientName: String) {
        _uiState.update { state ->
            val ingredientToUpdate = state.groceryListIngredients.find { it.ingredient.name == ingredientName }

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

    fun selectIngredient(ingredient: IngredientQuantityWithTime){
        _uiState.update { it.copy(selectedIngredients = it.selectedIngredients + ingredient) }
    }
    fun unselectIngredient(ingredient: IngredientQuantityWithTime){
        _uiState.update { it.copy(selectedIngredients = it.selectedIngredients - ingredient) }
    }

    fun moveSelectedIngredientsToFridge() {
        val selected = _uiState.value.selectedIngredients
        if (selected.isEmpty()) return

        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isLoading = true) }
                moveIngredientsToFridgeUseCase(selected)
                _uiState.update { it.copy(selectedIngredients = emptyList(), isLoading = false) }
                _events.send(GroceryListEvent.MovedToFridge)
            } catch (e: Exception) {
                _uiState.update { it.copy(error = "Could not move ingredients: ${e.message}", isLoading = false) }
            }
        }
    }
    private fun loadSelectableIngredientsForCategory(category: IngredientCategory) {
        viewModelScope.launch {
            try {
                val ingredientsFromDb = ingredientRepository.getIngredientsByCategory(category).first()

                _uiState.update { state ->
                    val alreadySelected = state.groceryListIngredients.map { it.ingredient }
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

    companion object {
        val Factory = viewModelFactory {
            initializer {
                val app = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as FridgeApplication
                GroceryListViewModel(
                    groceryListRepository = app.container.groceryListRepository,
                    ingredientRepository = app.container.ingredientRepository,
                    moveIngredientsToFridgeUseCase = app.container.moveIngredientsToFridgeUseCase
                )
            }
        }
    }
}