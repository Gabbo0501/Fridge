package com.example.fridgeproject.viewmodel.recipe

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.fridgeproject.FridgeApplication
import com.example.fridgeproject.data.repository.auth.SessionManagerFacade
import com.example.fridgeproject.domain.RecipeWithStatsRepository
import com.example.fridgeproject.domain.UserRepository
import com.example.fridgeproject.model.RecipeWithStats
import com.example.fridgeproject.model.enums.CostRange
import com.example.fridgeproject.model.enums.Difficulty
import com.example.fridgeproject.model.enums.DishType
import com.example.fridgeproject.model.enums.PrepTime
import com.example.fridgeproject.model.utils.toPrepTime
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.collections.filter

class RecipeListViewModel(
    private val recipeRepository: RecipeWithStatsRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(RecipeListUiState())
    val uiState: StateFlow<RecipeListUiState> = _uiState.asStateFlow()

    private var cachedRecipes: List<RecipeWithStats> = emptyList()

    init {
        initialLoad()
    }


    // -------------------------------------------------------------------------
    // Asynchronous actions (repository I/O)
    // -------------------------------------------------------------------------

    private fun initialLoad() {
        _uiState.update { it.copy(isLoading = true) }

        viewModelScope.launch {
            combine(
                SessionManagerFacade.currentUserStateFlow,
                recipeRepository.filterRecipesWithRating("")
            ) { currentUserId, recipeList ->
                currentUserId to recipeList
            }.collect { (currentUserId, recipeList) ->
                val isLoggedIn = currentUserId != null

                val nickname = if (isLoggedIn) {
                    userRepository.getUserById(currentUserId).first()?.nickname
                } else {
                    null
                }

                cachedRecipes = recipeList
                val shuffledList = recipeList.shuffled()
                val displayedRecipes = filterRecipes(recipeList, _uiState.value)

                _uiState.update {
                    it.copy(
                        userNickname = nickname,
                        recipes = recipeList,
                        displayedRecipes = displayedRecipes,
                        dailyMenu = listOfNotNull(
                            shuffledList.find { it.recipe.dishType == DishType.FIRST_COURSE },
                            shuffledList.find { it.recipe.dishType == DishType.SECOND_COURSE },
                            shuffledList.find { it.recipe.dishType == DishType.DESSERT }
                        ),
                        popularRecipes = recipeList.sortedByDescending { recipe -> recipe.recipe.likes },
                        newRecipes = recipeList.sortedByDescending { recipe -> recipe.recipe.createdAt },
                        featuredRecipes = recipeList.sortedByDescending { recipe -> recipe.rating },
                        isLoading = false
                    )
                }
            }
        }
    }

    private fun filterRecipes(
        recipes: List<RecipeWithStats>,
        filters: RecipeListUiState
    ): List<RecipeWithStats> =
        recipes.filter { shortRecipe ->
            val matchesRecipeSearch = filters.searchRecipeQuery.isEmpty() ||
                shortRecipe.recipe.title.contains(
                    filters.searchRecipeQuery,
                    ignoreCase = true
                ) ||
                shortRecipe.recipe.author.contains(
                    filters.searchRecipeQuery,
                    ignoreCase = true
                ) ||
                shortRecipe.recipe.authorId.contains(
                    filters.searchRecipeQuery,
                    ignoreCase = true
                )

            val matchesAutSearch = filters.searchAuthorQuery.isEmpty() ||
                shortRecipe.recipe.author.contains(
                    filters.searchAuthorQuery,
                    ignoreCase = true
                )

            val matchesIngredients = filters.searchIngredientsQuery.isEmpty() ||
                shortRecipe.recipe.ingredients.any { ingredient ->
                    ingredient.ingredient.name.contains(
                        filters.searchIngredientsQuery,
                        ignoreCase = true
                    )
                }

            val matchesCategory = filters.selectedCategory == null ||
                shortRecipe.recipe.dishType == filters.selectedCategory

            val matchesPriceCat = filters.selectedCostRange == null ||
                shortRecipe.recipe.costRange == filters.selectedCostRange

            val matchesDifficulty = filters.selectedDifficulty == null ||
                shortRecipe.recipe.difficulty == filters.selectedDifficulty

            val matchesTime = filters.selectedCookingTime == null ||
                shortRecipe.recipe.preparationTimeSec.toPrepTime() == filters.selectedCookingTime

            matchesRecipeSearch && matchesIngredients && matchesAutSearch &&
                matchesCategory && matchesPriceCat && matchesDifficulty && matchesTime
        }


    // -------------------------------------------------------------------------
    // Synchronous UI state updates
    // -------------------------------------------------------------------------

    fun onExploreCategorySelected(category: DishType?) {
        _uiState.update { currentState ->
            val updated = currentState.copy(
                selectedCategory = if (currentState.selectedCategory == category) null else category
            )
            updated.copy(displayedRecipes = filterRecipes(cachedRecipes, updated))
        }
    }


    // barra di ricerca
    fun onSearchRecipeQueryChanged(newQuery: String) {
        _uiState.update { currentState ->
            val updated = currentState.copy(searchRecipeQuery = newQuery)
            updated.copy(displayedRecipes = filterRecipes(cachedRecipes, updated))
        }
    }

    fun onSearchIngredientsQueryChanged(newQuery: String) {
        _uiState.update { currentState ->
            val updated = currentState.copy(searchIngredientsQuery = newQuery)
            updated.copy(displayedRecipes = filterRecipes(cachedRecipes, updated))
        }
    }

    fun onSearchAuthorQueryChanged(newQuery: String) {
        _uiState.update { currentState ->
            val updated = currentState.copy(searchAuthorQuery = newQuery)
            updated.copy(displayedRecipes = filterRecipes(cachedRecipes, updated))
        }
    }


    // selezione categoria
    fun onCategorySelected(category: DishType?) {
        _uiState.update { currentState ->
            val updated = currentState.copy(
                selectedCategory = if (currentState.selectedCategory == category) null else category
            )
            updated.copy(displayedRecipes = filterRecipes(cachedRecipes, updated))
        }
    }

    fun onDifficultySliderMoved(position: Float) {
        val index = position.toInt().coerceIn(0, Difficulty.entries.lastIndex)
        _uiState.update { currentState ->
            val updated = currentState.copy(
                difficultySliderPosition = position,
                selectedDifficulty = Difficulty.entries[index]
            )
            updated.copy(displayedRecipes = filterRecipes(cachedRecipes, updated))
        }
    }

    fun clearDifficulty() {
        _uiState.update { currentState ->
            val updated = currentState.copy(
                selectedDifficulty = null,
                difficultySliderPosition = 0f
            )
            updated.copy(displayedRecipes = filterRecipes(cachedRecipes, updated))
        }
    }

    fun onCostSliderMoved(position: Float) {
        val index = position.toInt().coerceIn(0, CostRange.entries.lastIndex)
        _uiState.update { currentState ->
            val updated = currentState.copy(
                costSliderPosition = position,
                selectedCostRange = CostRange.entries[index]
            )
            updated.copy(displayedRecipes = filterRecipes(cachedRecipes, updated))
        }
    }

    fun clearCostRange() {
        _uiState.update { currentState ->
            val updated = currentState.copy(
                selectedCostRange = null,
                costSliderPosition = 0f
            )
            updated.copy(displayedRecipes = filterRecipes(cachedRecipes, updated))
        }
    }


    // selezione tempo max
    fun onCookingTimeChanged(newTime: PrepTime) {
        _uiState.update { currentState ->
            val updated = currentState.copy(
                selectedCookingTime = if (currentState.selectedCookingTime == newTime) null else newTime
            )
            updated.copy(displayedRecipes = filterRecipes(cachedRecipes, updated))
        }
    }

    fun clearAllFilters() {
        _uiState.update { currentState ->
            val updated = currentState.copy(
                searchRecipeQuery = "",
                searchAuthorQuery = "",
                searchIngredientsQuery = "",
                selectedCategory = null,
                selectedDifficulty = null,
                selectedCostRange = null,
                selectedCookingTime = null,
                costSliderPosition = 0f,
                difficultySliderPosition = 0f
            )
            updated.copy(displayedRecipes = filterRecipes(cachedRecipes, updated))
        }
    }

    // -------------------------------------------------------------------------
    // Factory
    // -------------------------------------------------------------------------

    companion object {
        val Factory = viewModelFactory {
            initializer {
                val app = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as FridgeApplication
                RecipeListViewModel(
                    recipeRepository = app.container.recipeWithStatsRepository,
                    userRepository = app.container.userRepository
                )
            }
        }
    }
}