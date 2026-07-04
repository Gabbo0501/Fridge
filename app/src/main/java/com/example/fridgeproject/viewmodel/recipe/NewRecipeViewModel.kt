package com.example.fridgeproject.viewmodel.recipe

import com.example.fridgeproject.viewmodel.*


import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.fridgeproject.FridgeApplication
import com.example.fridgeproject.domain.IngredientRepository
import com.example.fridgeproject.domain.RecipeRepository
import com.example.fridgeproject.model.IngredientQuantity
import com.example.fridgeproject.model.LocalImageInput
import com.example.fridgeproject.model.RecipeStep
import com.example.fridgeproject.model.previewUrl
import com.example.fridgeproject.model.enums.CostRange
import com.example.fridgeproject.model.enums.Cuisine
import com.example.fridgeproject.model.enums.Diet
import com.example.fridgeproject.model.enums.Difficulty
import com.example.fridgeproject.model.enums.DishType
import com.example.fridgeproject.model.enums.IngredientCategory
import com.example.fridgeproject.model.enums.UnitOfMeasure
import com.example.fridgeproject.model.enums.defaultIngredientQuantity
import com.example.fridgeproject.model.enums.IngredientWizardStep
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

private const val DESC_MAX_LENGTH = 250

class CreateRecipeViewModel  (
    private val recipeRepository: RecipeRepository,
    private val ingredientRepository: IngredientRepository

) : ViewModel() {

    private val _uiState = MutableStateFlow(CreateRecipeUiState())
    val uiState: StateFlow<CreateRecipeUiState> = _uiState.asStateFlow()
    private val _events = Channel<RecipeEvent>()
    val events = _events.receiveAsFlow()

    private fun Map<Int, LocalImageInput>.shiftAfterRemoval(removedIndex: Int): Map<Int, LocalImageInput> =
        entries.mapNotNull { (index, input) ->
            when {
                index == removedIndex -> null
                index > removedIndex -> (index - 1) to input
                else -> index to input
            }
        }.toMap()


    // ─────────────────────────────────────────────────────────────────────────
    // Asynchronous actions (repository I/O)
    // ─────────────────────────────────────────────────────────────────────────

    fun saveRecipe() {
        if (validateFields()) {
            viewModelScope.launch {
                _uiState.update { it.copy(isLoading = true) }
                val state = _uiState.value
                recipeRepository.saveRecipe(
                    recipe = state.newRecipe,
                    pendingCoverImage = state.pendingImageInput,
                    pendingStepImages = state.pendingStepImageInputs
                )
                _uiState.update { it.copy(success = true, isLoading = false) }
                _events.send(RecipeEvent.Published)
            }
        }
    }


    // ─────────────────────────────────────────────────────────────────────────
    // Synchronous UI state updates
    // ─────────────────────────────────────────────────────────────────────────

    private fun resetRecipeState() {
        _uiState.value = CreateRecipeUiState()
    }

    private fun validateFields(): Boolean {
        var isValid = true
        val current = _uiState.value

        val errors = CreateRecipeErrors(
            image = if (current.newRecipe.image.orEmpty().isBlank()) {
                isValid = false
                "Recipe image cannot be empty"
            } else "",
            title = if (current.newRecipe.title.isBlank()) {
                isValid = false
                "Title cannot be empty"
            } else "",
            description = if (current.newRecipe.description.isBlank()) {
                isValid = false
                "Description cannot be empty"
            } else if(current.newRecipe.description.length > DESC_MAX_LENGTH){
                isValid = false
                "Description cannot be more than 250 character"
            } else "",
            steps = if( current.newRecipe.preparationSteps.isEmpty() ) {
                isValid = false
                "Add at least one completed step"
            } else  "",
            stepImage = if (current.newRecipe.preparationSteps.any { it.image.isBlank() }) {
                isValid = false
                "All steps must have an image"
            } else "",
            stepDescription = if (current.newRecipe.preparationSteps.any { it.description.isBlank() }) {
                isValid = false
                "All steps must have a description"
            } else "",
            ingredients = when {
                current.newRecipe.ingredients.isEmpty() -> {
                    isValid = false
                    "Recipe must have at least 1 ingredient"
                }
                current.newRecipe.ingredients.any { it.quantity <= 0f } -> {
                    isValid = false
                    "All ingredients must have a positive quantity"
                }
                else -> ""
            },
        )

        _uiState.update { it.copy(errors = errors, showErrorDialog = !isValid) }
        return isValid
    }

    fun dismissErrorDialog() {
        _uiState.update { it.copy(showErrorDialog = false) }
    }

    fun showDifficultyDialog() {
        _uiState.update {
            it.copy(
                showDifficultyDialog = true,
                showTimeDialog = false,
                showCostDialog = false
            )
        }
    }

    fun showTimeDialog() {
        _uiState.update {
            it.copy(
                showDifficultyDialog = false,
                showTimeDialog = true,
                showCostDialog = false
            )
        }
    }

    fun showCostDialog() {
        _uiState.update {
            it.copy(
                showDifficultyDialog = false,
                showTimeDialog = false,
                showCostDialog = true
            )
        }
    }

    fun dismissStatsDialog() {
        _uiState.update {
            it.copy(
                showDifficultyDialog = false,
                showTimeDialog = false,
                showCostDialog = false
            )
        }
    }

    fun showDietDialog() {
        _uiState.update {
            it.copy(
                showDietDialog = true,
                showCuisineDialog = false
            )
        }
    }

    fun showCuisineDialog() {
        _uiState.update {
            it.copy(
                showDietDialog = false,
                showCuisineDialog = true
            )
        }
    }

    fun dismissTagsDialog() {
        _uiState.update {
            it.copy(
                showDietDialog = false,
                showCuisineDialog = false
            )
        }
    }

    private fun loadSelectableIngredientsForCategory(category: IngredientCategory) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val ingredientsFromDb = ingredientRepository.getIngredientsByCategory(category).first()

                _uiState.update { state ->
                    val alreadySelected = state.newRecipe.ingredients.map { it.ingredient }
                    val filteredIngredients = ingredientsFromDb.filterNot { it in alreadySelected }

                    state.copy(
                        selectableIngredients = filteredIngredients,
                        addingIngredientStep = IngredientWizardStep.INGREDIENT,
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                _uiState.update { state ->
                    state.copy(
                        selectableIngredients = emptyList(),
                        isLoading = false,
                        errors = state.errors.copy(ingredients = "Error loading ingredients from catalog: ${e.message}")
                    )
                }
            }
        }
    }

    fun resetCreateState(){
        resetRecipeState()
    }

    fun hasUnsavedChanges(): Boolean {
        val current = _uiState.value
        return current.canCheckUnsavedChanges() && current.hasChangesFromDefault()
    }

    fun requestExit() {
        if (hasUnsavedChanges()) {
            _uiState.update { it.copy(showExitDialog = true) }
        } else {
            viewModelScope.launch { _events.send(RecipeEvent.ExitAllowed) }
        }
    }

    fun confirmExit() {
        _uiState.update { it.copy(showExitDialog = false) }
        viewModelScope.launch { _events.send(RecipeEvent.ExitAllowed) }
    }

    fun dismissExitDialog() {
        _uiState.update { it.copy(showExitDialog = false) }
        viewModelScope.launch { _events.send(RecipeEvent.ExitCancelled) }
    }

    fun updateImage(input: LocalImageInput) {
        _uiState.update { state ->
            state.copy(
                newRecipe = state.newRecipe.copy(image = input.previewUrl()),
                pendingImageInput = input
            )
        }
    }

    fun updateTitle(value: String) {
        _uiState.update { state ->
            state.copy(newRecipe = state.newRecipe.copy(title = value))
        }
    }

    fun updateDishType(value: DishType) {
        _uiState.update { state ->
            state.copy(newRecipe = state.newRecipe.copy(dishType = value))
        }
    }

    fun toggleDiet(diet: Diet) {
        _uiState.update { state ->
            val newList = if (state.newRecipe.suitableDiets.contains(diet)) {
                state.newRecipe.suitableDiets - diet
            } else {
                state.newRecipe.suitableDiets + diet
            }
            state.copy(newRecipe = state.newRecipe.copy(suitableDiets = newList))
        }
    }

    fun toggleCuisine(cuisine: Cuisine) {
        _uiState.update { state ->
            val newList = if (state.newRecipe.cuisine.contains(cuisine)) {
                state.newRecipe.cuisine - cuisine
            } else {
                state.newRecipe.cuisine + cuisine
            }
            state.copy(newRecipe = state.newRecipe.copy(cuisine = newList))
        }
    }

    fun updateDifficulty(value: Difficulty) {
        _uiState.update { state ->
            state.copy(newRecipe = state.newRecipe.copy(difficulty = value))
        }
    }

    fun updatePrepTime(value: Long) {
        _uiState.update { state ->
            state.copy(newRecipe = state.newRecipe.copy(preparationTimeSec = value))
        }
    }

    fun updateCostRange(value: CostRange) {
        _uiState.update { state ->
            state.copy(newRecipe = state.newRecipe.copy(costRange = value))
        }
    }

    fun updateDescription(value: String) {
        _uiState.update { state ->
            state.copy(newRecipe = state.newRecipe.copy(description = value))
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
            if (currentUnit == UnitOfMeasure.QB) return@update state

            val step = when (currentUnit) {
                UnitOfMeasure.KG -> 1f
                UnitOfMeasure.PCS -> 1f
                else -> 10f
            }

            val nextFloat = state.newIngredient.quantity + step

            state.copy(
                newIngredient = state.newIngredient.copy(quantity = nextFloat),
                quantityInput = nextFloat.toInt().toString(),
                isConfirmQuantityEnabled = nextFloat > 0f
            )
        }
    }

    fun decrementNewIngredientQuantity() {
        _uiState.update { state ->
            val currentUnit = state.newIngredient.unit
            if (currentUnit == UnitOfMeasure.QB) return@update state

            val step = when (currentUnit) {
                UnitOfMeasure.KG -> 1f
                UnitOfMeasure.PCS -> 1f
                else -> 10f
            }

            val nextFloat = state.newIngredient.quantity - step
            if (nextFloat < step) return@update state

            state.copy(
                newIngredient = state.newIngredient.copy(quantity = nextFloat),
                quantityInput = nextFloat.toInt().toString(),
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

    fun addIngredient(ingredient: IngredientQuantity){
        _uiState.update { state ->
            val updatedIngredients = state.newRecipe.ingredients.filterNot {
                it.ingredient.name.lowercase().trim() == ingredient.ingredient.name.lowercase().trim()
            } + ingredient
            state.copy(
                newRecipe = state.newRecipe.copy(
                    ingredients = updatedIngredients
                ),
                addingIngredient = false
            )
        }
    }

    fun updateIngredient(ingredientName: String) {
        _uiState.update { state ->
            val ingredientToUpdate = state.newRecipe.ingredients.find { it.ingredient.name == ingredientName }

            if (ingredientToUpdate != null) {
                val oldQuantity = if (ingredientToUpdate.unit == UnitOfMeasure.QB) {
                    "Q.B."
                } else {
                    ingredientToUpdate.quantity.toInt().toString()
                }

                state.copy(
                    addingIngredient = true,
                    addingIngredientStep = IngredientWizardStep.QUANTITY,
                    newIngredient = ingredientToUpdate,
                    quantityInput = oldQuantity,
                    isEditMode = true,
                    isConfirmQuantityEnabled = true
                )
            } else {
                state
            }
        }
    }

    fun removeIngredient(ingredient: IngredientQuantity){
        _uiState.update { state ->
            state.copy(
                newRecipe = state.newRecipe.copy(
                    ingredients = state.newRecipe.ingredients - ingredient
                )
            )
        }
    }

    fun addStep() {
        _uiState.update { state ->
            val newSteps = state.newRecipe.preparationSteps + RecipeStep()
            state.copy(
                newRecipe = state.newRecipe.copy(preparationSteps = newSteps),
                currentEditedStepIndex = newSteps.lastIndex
            )
        }
    }

    fun updateStepDescription(description: String) {
        _uiState.update { state ->
            val currentIndex = state.currentEditedStepIndex
            state.copy(
                newRecipe = state.newRecipe.copy(
                    preparationSteps = state.newRecipe.preparationSteps.mapIndexed { index, step ->
                        if (index == currentIndex) step.copy(description = description) else step
                    }
                )
            )
        }
    }

    fun updateStepImage(input: LocalImageInput) {
        _uiState.update { state ->
            val currentIndex = state.currentEditedStepIndex
            state.copy(
                newRecipe = state.newRecipe.copy(
                    preparationSteps = state.newRecipe.preparationSteps.mapIndexed { index, step ->
                        if (index == currentIndex) step.copy(image = input.previewUrl()) else step
                    }
                ),
                pendingStepImageInputs = state.pendingStepImageInputs + (currentIndex to input)
            )
        }
    }

    fun removeStep() {
        _uiState.update { state ->
            val steps = state.newRecipe.preparationSteps
            if (steps.isEmpty()) return@update state

            val removeIndex = state.currentEditedStepIndex.coerceIn(steps.indices)
            val newSteps = steps.toMutableList().apply { removeAt(removeIndex) }.toList()
            val newPendingInputs = state.pendingStepImageInputs.shiftAfterRemoval(removeIndex)

            val newCurrentIndex = when {
                newSteps.isEmpty() -> 0
                removeIndex >= newSteps.size -> newSteps.lastIndex
                else -> removeIndex
            }

            state.copy(
                newRecipe = state.newRecipe.copy(preparationSteps = newSteps),
                pendingStepImageInputs = newPendingInputs,
                currentEditedStepIndex = newCurrentIndex
            )
        }
    }

    fun switchLeftStep(){
        _uiState.update { state ->
            val newStep = (state.currentEditedStepIndex - 1).coerceAtLeast(0)
            state.copy(
                currentEditedStepIndex = newStep
            )
        }
    }

    fun switchRightStep(){
        _uiState.update { state ->
            val maxIndex = (state.newRecipe.preparationSteps.size - 1).coerceAtLeast(0)
            val newStep = (state.currentEditedStepIndex + 1).coerceAtMost(maxIndex)
            state.copy(
                currentEditedStepIndex = newStep
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
                    _uiState.update { it.copy(addingIngredientStep = IngredientWizardStep.INGREDIENT) }
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


    // ─────────────────────────────────────────────────────────────────────────
    // Factory
    // ─────────────────────────────────────────────────────────────────────────

    companion object {
        val Factory = viewModelFactory {
            initializer {
                val app = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as FridgeApplication
                CreateRecipeViewModel(
                    recipeRepository = app.container.recipeRepository,
                    ingredientRepository = app.container.ingredientRepository
                )
            }
        }
    }
}
