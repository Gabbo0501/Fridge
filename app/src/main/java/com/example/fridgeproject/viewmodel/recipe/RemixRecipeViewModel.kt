package com.example.fridgeproject.viewmodel.recipe

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
import com.example.fridgeproject.domain.IngredientRepository
import com.example.fridgeproject.domain.RecipeRepository
import com.example.fridgeproject.model.IngredientQuantity
import com.example.fridgeproject.model.LocalImageInput
import com.example.fridgeproject.model.Recipe
import com.example.fridgeproject.model.RecipeStep
import com.example.fridgeproject.model.previewUrl
import com.example.fridgeproject.model.enums.*
import com.example.fridgeproject.navigation.RemixRecipeRoute
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class RemixRecipeViewModel (
    savedStateHandle: SavedStateHandle,
    private val recipeRepository: RecipeRepository,
    private val ingredientRepository: IngredientRepository
) : ViewModel() {

    private val route = savedStateHandle.toRoute<RemixRecipeRoute>()
    private val recipeId = route.recipeId
    private var sourceRecipe: Recipe? = null

    private val _uiState = MutableStateFlow(RemixRecipeUiState())
    val uiState: StateFlow<RemixRecipeUiState> = _uiState.asStateFlow()
    private val _events = Channel<RecipeEvent>()
    val events = _events.receiveAsFlow()

    init {
        loadRecipe(recipeId)
    }

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

    fun loadRecipe(id: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            val recipe = recipeRepository.getRecipeById(id).first()
            if (recipe != null) {
                sourceRecipe = recipe
                _uiState.update {
                    it.copy(
                        remixedFromRecipeTitle = recipe.title,
                        title = recipe.title,
                        description = recipe.description,
                        image = recipe.image,
                        dishType = recipe.dishType,
                        costRange = recipe.costRange,
                        difficulty = recipe.difficulty,
                        preparationTimeSec = recipe.preparationTimeSec,
                        ingredients = recipe.ingredients,
                        preparationSteps = recipe.preparationSteps,
                        suitableDiets = recipe.suitableDiets,
                        cuisine = recipe.cuisine,
                        isSaved = false,
                        isLoading = false
                    )
                }
            } else {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errors = it.errors.copy(globalError = "Recipe Details Not Found")
                    )
                }
            }
            _uiState.update { it.copy(isLoading = false) }
        }
    }

    fun publishRemix() {
        val current = _uiState.value
        val newErrors = RemixRecipeErrors(
            titleError = if (current.title.isBlank()) "Title is required" else null,
            descriptionError = if (current.description.isBlank()) "Description is required" else null,
            imageError = if (current.image.isNullOrBlank()) "Recipe image is required" else null,
            prepTimeError = if (current.preparationTimeSec <= 0) "Must be a valid time" else null,
            ingredientsError = if (current.ingredients.isEmpty()) "Add at least one ingredient" else null,
            stepsError = if (current.preparationSteps.isEmpty()) "Add at least one step" else null,
            stepsImageError = if (current.preparationSteps.any { it.image.isBlank() }) "All the steps must have an image" else null,
            stepsDescError = if (current.preparationSteps.any { it.description.isBlank() }) "All the steps must have a description" else null
        )
        val hasErrors =
            newErrors.titleError != null
            || newErrors.prepTimeError != null
            || newErrors.descriptionError != null
            || newErrors.imageError != null
            || newErrors.ingredientsError != null
            || newErrors.stepsError != null
            || newErrors.stepsImageError != null
            || newErrors.stepsDescError != null
        _uiState.update { it.copy(errors = newErrors, showErrorDialog = hasErrors) }

        if (hasErrors) return

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }
            try {
                val state = _uiState.value
                recipeRepository.saveRecipe(
                    Recipe(
                        remixedFromRecipeId = recipeId,
                        title = state.title,
                        image = state.image,
                        description = state.description,
                        dishType = state.dishType,
                        suitableDiets = state.suitableDiets,
                        cuisine = state.cuisine,
                        costRange = state.costRange,
                        difficulty = state.difficulty,
                        preparationTimeSec = state.preparationTimeSec,
                        ingredients = state.ingredients,
                        preparationSteps = state.preparationSteps
                    ),
                    pendingCoverImage = state.pendingImageInput,
                    pendingStepImages = state.pendingStepImageInputs
                )
                _uiState.update { it.copy(isSaving = false, isSaved = true) }
                _events.send(RecipeEvent.Published)
            } catch (e: Exception) {
                _uiState.update { it.copy(isSaving = false, errors = it.errors.copy(globalError = e.message)) }
            }
        }
    }

    private fun loadSelectableIngredientsForCategory(category: IngredientCategory) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val ingredientsFromDb = ingredientRepository.getIngredientsByCategory(category).first()

                _uiState.update { state ->
                    val alreadySelected = state.ingredients.map { ingredient -> ingredient.ingredient }
                    val filteredIngredients = ingredientsFromDb.filterNot { ingredient -> ingredient in alreadySelected }

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
                        errors = state.errors.copy(globalError = "Error loading ingredients from catalog: ${e.message}")
                    )
                }
            }
        }
    }


    // ─────────────────────────────────────────────────────────────────────────
    // Synchronous UI state updates
    // ─────────────────────────────────────────────────────────────────────────

    fun hasUnsavedChanges(): Boolean {
        val current = _uiState.value
        val source = sourceRecipe ?: return false
        return current.canCheckUnsavedChanges() && current.hasChangesFrom(source)
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

    fun onReset() = _uiState.update { RemixRecipeUiState() }

    fun updateTitle(title: String) {
        _uiState.update { it.copy(title = title, errors = it.errors.copy(titleError = null)) }
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

    fun updateDescription(description: String) {
        _uiState.update { it.copy(description = description, errors = it.errors.copy(descriptionError = null)) }
    }

    fun updateImage(input: LocalImageInput) {
        _uiState.update {
            it.copy(
                image = input.previewUrl(),
                pendingImageInput = input,
                errors = it.errors.copy(imageError = null)
            )
        }
    }

    fun updateDishType(dishType: DishType) = _uiState.update { it.copy(dishType = dishType) }

    fun updateCostRange(costRange: CostRange) = _uiState.update { it.copy(costRange = costRange) }

    fun updateDifficulty(difficulty: Difficulty) = _uiState.update { it.copy(difficulty = difficulty) }

    fun updatePrepTime(prepTime: Long) = _uiState.update { it.copy(preparationTimeSec = prepTime, errors = it.errors.copy(prepTimeError = null)) }

    fun toggleDiet(diet: Diet) {
        _uiState.update {
            val newList = if (it.suitableDiets.contains(diet)) it.suitableDiets - diet else it.suitableDiets + diet
            it.copy(suitableDiets = newList)
        }
    }

    fun toggleCuisine(cuisine: Cuisine) {
        _uiState.update {
            val newList = if (it.cuisine.contains(cuisine)) it.cuisine - cuisine else it.cuisine + cuisine
            it.copy(cuisine = newList)
        }
    }

    fun initAddIngredientProcedure() {
        _uiState.update { it.copy(addingIngredient = true, addingIngredientStep = IngredientWizardStep.CATEGORY, newIngredient = IngredientQuantity()) }
    }

    fun cancelAddIngredientProcedure() {
        _uiState.update { it.copy(addingIngredient = false) }
    }

    fun updateNewIngredientCategory(category: IngredientCategory) {
        _uiState.update { state ->
            state.copy(newIngredient = state.newIngredient.copy(ingredient = state.newIngredient.ingredient.copy(category = category)))
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

    fun updateNewIngredientQuantity(qty: Float) {
        _uiState.update {
            val quantity = if (qty > 0f) qty else it.newIngredient.unit.defaultIngredientQuantity()
            it.copy(newIngredient = it.newIngredient.copy(quantity = quantity))
        }
    }

    fun updateNewIngredientUnit(unit: UnitOfMeasure) {
        _uiState.update {
            it.copy(newIngredient = it.newIngredient.copy(
                quantity = unit.defaultIngredientQuantity(),
                unit = unit
            ))
        }
    }


    fun addIngredient(ingredient: IngredientQuantity) {
        _uiState.update { state ->
            val updatedList = state.ingredients.filterNot {
                it.ingredient.name.lowercase().trim() == ingredient.ingredient.name.lowercase().trim()
            } + ingredient

            state.copy(
                ingredients = updatedList,
                addingIngredient = false
            )
        }
    }

    fun updateIngredient(ingredientName: String) {
        _uiState.update { state ->
            val ingredientToUpdate = state.ingredients.find { it.ingredient.name == ingredientName }

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

    fun removeIngredient(ingredient: IngredientQuantity) {
        _uiState.update { it.copy(ingredients = it.ingredients - ingredient) }
    }

    fun addStep() {
        _uiState.update {
            val newSteps = it.preparationSteps + RecipeStep()
            it.copy(
                preparationSteps = newSteps,
                currentEditedStepIndex = newSteps.lastIndex
            )
        }
    }
    fun updateStepDescription(description: String) {
        _uiState.update {
            val currentIndex = it.currentEditedStepIndex
            val newSteps = it.preparationSteps.mapIndexed { index, step ->
                if (index == currentIndex) step.copy(description = description) else step
            }
            it.copy(
                preparationSteps = newSteps
            )
        }
    }
    fun updateImageStep(input: LocalImageInput) {
        _uiState.update {
            val currentIndex = it.currentEditedStepIndex
            if (currentIndex !in it.preparationSteps.indices) return@update it
            val newList = it.preparationSteps.toMutableList()
            newList[currentIndex] = newList[currentIndex].copy(image = input.previewUrl())
            it.copy(
                preparationSteps = newList,
                pendingStepImageInputs = it.pendingStepImageInputs + (currentIndex to input),
                errors = it.errors.copy(stepsError = null)
            )
        }
    }
    fun removeStep() {
        _uiState.update {
            val steps = it.preparationSteps
            if (steps.isEmpty()) return@update it

            val removeIndex = it.currentEditedStepIndex.coerceIn(steps.indices)
            val newSteps = steps.toMutableList().apply { removeAt(removeIndex) }.toList()
            val newPendingInputs = it.pendingStepImageInputs.shiftAfterRemoval(removeIndex)

            val newCurrentIndex = when {
                newSteps.isEmpty() -> 0
                removeIndex >= newSteps.size -> newSteps.lastIndex
                else -> removeIndex
            }

            it.copy(
                preparationSteps = newSteps,
                pendingStepImageInputs = newPendingInputs,
                currentEditedStepIndex = newCurrentIndex
            )
        }
    }
    fun switchLeftStep(){
        _uiState.update {
            val newStep = (it.currentEditedStepIndex - 1).coerceAtLeast(0)
            it.copy(
                currentEditedStepIndex = newStep
            )
        }
    }
    fun switchRightStep(){
        _uiState.update {
            val maxIndex = (it.preparationSteps.size - 1).coerceAtLeast(0)
            val newStep = (it.currentEditedStepIndex + 1).coerceAtMost(maxIndex)
            it.copy(
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
                val savedStateHandle = createSavedStateHandle()
                val app = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as FridgeApplication
                RemixRecipeViewModel(
                    savedStateHandle = savedStateHandle,
                    recipeRepository = app.container.recipeRepository,
                    ingredientRepository = app.container.ingredientRepository
                )
            }
        }
    }
}