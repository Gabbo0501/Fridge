package com.example.fridgeproject.viewmodel.fridge

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.fridgeproject.FridgeApplication
import com.example.fridgeproject.data.repository.auth.SessionManagerFacade
import com.example.fridgeproject.domain.FridgeRepository
import com.example.fridgeproject.domain.RecipeWithStatsRepository
import com.example.fridgeproject.model.Fridge
import com.example.fridgeproject.model.RecipeWithStats
import com.example.fridgeproject.model.enums.CostRange
import com.example.fridgeproject.model.enums.Difficulty
import com.example.fridgeproject.model.enums.DishType
import com.example.fridgeproject.model.enums.PrepTime
import com.example.fridgeproject.model.enums.UnitOfMeasure
import com.example.fridgeproject.model.utils.toPrepTime
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class FridgeRecipeListViewModel(
    private val fridgeRepository: FridgeRepository,
    private val recipeRepository: RecipeWithStatsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(FridgeRecipeListUiState())
    val uiState: StateFlow<FridgeRecipeListUiState> = _uiState.asStateFlow()

    private var cachedFridge: Fridge? = null
    private var cachedRecipes: List<RecipeWithStats> = emptyList()

    init {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            SessionManagerFacade.currentUserStateFlow.flatMapLatest { userId ->
                if (userId == null) {
                    cachedFridge = null
                    cachedRecipes = emptyList()
                    flowOf(
                        _uiState.value.copy(
                            displayedRecipes = emptyList(),
                            doableRecipes = emptyList(),
                            missingQuantityRecipes = emptyList(),
                            isLoading = false
                        )
                    )
                } else {
                    combine(
                        fridgeRepository.getFridgeByOwner(userId),
                        recipeRepository.filterRecipesWithRating("")
                    ) { fridge, recipes ->
                        cachedFridge = fridge
                        cachedRecipes = recipes
                        computeRecipeLists(fridge, recipes, _uiState.value)
                    }
                }
            }.collect { newState ->
                _uiState.update { newState }
            }
        }
    }

    private fun computeRecipeLists(
        fridge: Fridge?,
        recipes: List<RecipeWithStats>,
        currentState: FridgeRecipeListUiState
    ): FridgeRecipeListUiState {
        if (fridge == null) return currentState.copy(isLoading = false)

        val fridgeMap = fridge.ingredients.associateBy { it.ingredient.name.lowercase().trim() }

        val base = recipes.filter { shortRecipe ->
            val matchesIngredients = currentState.searchIngredientsQuery.isEmpty() ||
                    shortRecipe.recipe.ingredients.any { it.ingredient.name.contains(currentState.searchIngredientsQuery, ignoreCase = true) }
            val matchesCategory = currentState.selectedCategory == null ||
                    shortRecipe.recipe.dishType == currentState.selectedCategory
            val matchesDifficulty = currentState.selectedDifficulty == null ||
                    shortRecipe.recipe.difficulty == currentState.selectedDifficulty
            val matchesCost = currentState.selectedCostRange == null ||
                    shortRecipe.recipe.costRange == currentState.selectedCostRange
            val matchesTime = currentState.selectedCookingTime == null ||
                    shortRecipe.recipe.preparationTimeSec.toPrepTime() == currentState.selectedCookingTime
            matchesIngredients && matchesCategory && matchesDifficulty && matchesCost && matchesTime
        }

        val displayed = base.filter { r ->
            r.recipe.ingredients.any { fridgeMap.containsKey(it.ingredient.name.lowercase().trim()) }
        }

        val doable = base.filter { r ->
            r.recipe.ingredients.isNotEmpty() && r.recipe.ingredients.all { reqIng ->
                val item = fridgeMap[reqIng.ingredient.name.lowercase().trim()]
                item != null && (reqIng.unit == UnitOfMeasure.QB || item.quantity >= reqIng.quantity)
            }
        }

        val missingQuantity = base.filter { r ->
            r.recipe.ingredients.isNotEmpty() &&
                    r.recipe.ingredients.all { fridgeMap.containsKey(it.ingredient.name.lowercase().trim()) } &&
                    r.recipe.ingredients.any { reqIng ->
                        val item = fridgeMap[reqIng.ingredient.name.lowercase().trim()]
                        item != null && reqIng.unit != UnitOfMeasure.QB && item.quantity < reqIng.quantity
                    }
        }

        return currentState.copy(
            displayedRecipes = displayed,
            doableRecipes = doable,
            missingQuantityRecipes = missingQuantity,
            isLoading = false
        )
    }

    fun onSearchIngredientsQueryChanged(query: String) {
        _uiState.update { currentState ->
            val updated = currentState.copy(searchIngredientsQuery = query)
            computeRecipeLists(cachedFridge, cachedRecipes, updated)
        }
    }

    fun onCategorySelected(category: DishType?) {
        _uiState.update { currentState ->
            val updated = currentState.copy(selectedCategory = if (currentState.selectedCategory == category) null else category)
            computeRecipeLists(cachedFridge, cachedRecipes, updated)
        }
    }

    fun onCookingTimeChanged(time: PrepTime) {
        _uiState.update { currentState ->
            val updated = currentState.copy(selectedCookingTime = if (currentState.selectedCookingTime == time) null else time)
            computeRecipeLists(cachedFridge, cachedRecipes, updated)
        }
    }

    fun onDifficultySliderMoved(position: Float) {
        val index = position.toInt().coerceIn(0, Difficulty.entries.lastIndex)
        _uiState.update { currentState ->
            val updated = currentState.copy(
                difficultySliderPosition = position,
                selectedDifficulty = Difficulty.entries[index]
            )
            computeRecipeLists(cachedFridge, cachedRecipes, updated)
        }
    }

    fun clearDifficulty() {
        _uiState.update { currentState ->
            val updated = currentState.copy(selectedDifficulty = null, difficultySliderPosition = 0f)
            computeRecipeLists(cachedFridge, cachedRecipes, updated)
        }
    }

    fun onCostSliderMoved(position: Float) {
        val index = position.toInt().coerceIn(0, CostRange.entries.lastIndex)
        _uiState.update { currentState ->
            val updated = currentState.copy(
                costSliderPosition = position,
                selectedCostRange = CostRange.entries[index]
            )
            computeRecipeLists(cachedFridge, cachedRecipes, updated)
        }
    }

    fun clearCostRange() {
        _uiState.update { currentState ->
            val updated = currentState.copy(selectedCostRange = null, costSliderPosition = 0f)
            computeRecipeLists(cachedFridge, cachedRecipes, updated)
        }
    }

    fun clearAllFilters() {
        _uiState.update { currentState ->
            val cleared = currentState.copy(
                searchIngredientsQuery = "",
                selectedCategory = null,
                selectedCookingTime = null,
                selectedDifficulty = null,
                difficultySliderPosition = 0f,
                selectedCostRange = null,
                costSliderPosition = 0f
            )
            computeRecipeLists(cachedFridge, cachedRecipes, cleared)
        }
    }

    // Factory
    companion object {
        val Factory = viewModelFactory {
            initializer {
                val app = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as FridgeApplication
                FridgeRecipeListViewModel(
                    fridgeRepository = app.container.fridgeRepository,
                    recipeRepository = app.container.recipeWithStatsRepository
                )
            }
        }
    }
}