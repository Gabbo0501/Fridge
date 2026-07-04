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
import com.example.fridgeproject.data.repository.auth.SessionManagerFacade
import com.example.fridgeproject.data.useCase.SaveMissingIngredientInGroceryList
import com.example.fridgeproject.domain.CollectionRepository
import com.example.fridgeproject.domain.FridgeRepository
import com.example.fridgeproject.domain.RecipeRepository
import com.example.fridgeproject.domain.RecipeWithStatsRepository
import com.example.fridgeproject.domain.ReviewRepository
import com.example.fridgeproject.domain.TipRepository
import com.example.fridgeproject.domain.UserRepository
import com.example.fridgeproject.model.Fridge
import com.example.fridgeproject.model.IngredientQuantity
import com.example.fridgeproject.model.IngredientQuantityWithTime
import com.example.fridgeproject.model.RecipeWithStats
import com.example.fridgeproject.model.Review
import com.example.fridgeproject.model.SystemCollection
import com.example.fridgeproject.model.Tip
import com.example.fridgeproject.model.avatarUrl
import com.example.fridgeproject.model.enums.UnitOfMeasure
import com.example.fridgeproject.model.toRecipeReviewUi
import com.example.fridgeproject.model.toRecipeUi
import com.example.fridgeproject.model.toRecipeTipUi
import com.example.fridgeproject.navigation.RecipeDetailGraph
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class RecipeViewModel(
    savedStateHandle: SavedStateHandle,
    private val recipeRepository: RecipeRepository,
    private val recipeWithStatsRepository: RecipeWithStatsRepository,
    private val userRepository: UserRepository,
    private val collectionRepository: CollectionRepository,
    private val reviewRepository: ReviewRepository,
    private val tipRepository: TipRepository,
    private val fridgeRepository: FridgeRepository,
    private val saveMissingIngredientInGroceryList: SaveMissingIngredientInGroceryList
) : ViewModel() {

    private val route = savedStateHandle.toRoute<RecipeDetailGraph>()
    private val recipeId = route.recipeId

    private val _uiState = MutableStateFlow(RecipeUiState(isLoading = true))
    val uiState: StateFlow<RecipeUiState> = _uiState.asStateFlow()
    private val _events = Channel<RecipeEvent>()
    val events = _events.receiveAsFlow()

    init {
        loadRecipe()
    }


    // ─────────────────────────────────────────────────────────────────────────
    // Asynchronous actions (repository I/O)
    // ─────────────────────────────────────────────────────────────────────────

    fun loadRecipe() {
        viewModelScope.launch {
            SessionManagerFacade.currentUserStateFlow
                .flatMapLatest { currentUserId ->
                    if (currentUserId == null) {
                        combine(
                            recipeWithStatsRepository.getRecipeWithRatingById(recipeId),
                            reviewRepository.getReviewsByRecipe(recipeId),
                            tipRepository.getTipsByRecipe(recipeId)
                        ) { recipeWithStats, rawReviews, rawTips ->
                            buildRecipeUiState(
                                currentUserId = null,
                                recipeWithStats = recipeWithStats,
                                rawReviews = rawReviews,
                                rawTips = rawTips,
                                favoritesCollection = null,
                                userFridge = null
                            )
                        }
                    } else {
                        combine(
                            recipeWithStatsRepository.getRecipeWithRatingById(recipeId),
                            reviewRepository.getReviewsByRecipe(recipeId),
                            tipRepository.getTipsByRecipe(recipeId),
                            collectionRepository.getFavourites(currentUserId),
                            fridgeRepository.getFridgeByOwner(currentUserId)
                        ) { recipeWithStats, rawReviews, rawTips, favoritesCollection, userFridge ->
                            buildRecipeUiState(
                                currentUserId = currentUserId,
                                recipeWithStats = recipeWithStats,
                                rawReviews = rawReviews,
                                rawTips = rawTips,
                                favoritesCollection = favoritesCollection,
                                userFridge = userFridge
                            )
                        }
                    }
                }
                .collect { loadedState ->
                    _uiState.update { currentState ->
                        loadedState.copy(
                            deleteDialog = currentState.deleteDialog
                        )
                    }
                }
        }
    }

    private suspend fun buildRecipeUiState(
        currentUserId: String?,
        recipeWithStats: RecipeWithStats?,
        rawReviews: List<Review>,
        rawTips: List<Tip>,
        favoritesCollection: SystemCollection?,
        userFridge: Fridge?
    ): RecipeUiState {
        if (recipeWithStats == null) {
            return RecipeUiState(isLoading = false, error = "Recipe not found")
        }

        val recipe = recipeWithStats.recipe
        val isLoggedIn = currentUserId != null
        val isOwner = recipe.authorId == currentUserId

        val authorNickname = userRepository.getUserById(recipe.authorId).first()?.nickname.orEmpty()
        val originRecipe = recipe.remixedFromRecipeId?.let { originId ->
            recipeRepository.getRecipeById(originId).first()
        }
        val originAuthorNickname = originRecipe?.let { origin ->
            userRepository.getUserById(origin.authorId).first()?.nickname
        }

        val reviewsUi = rawReviews.map { review ->
            val user = userRepository.getUserById(review.userId).first()
            review.toRecipeReviewUi(
                userName = user?.nickname.orEmpty(),
                firstName = user?.firstName.orEmpty(),
                lastName = user?.lastName.orEmpty(),
                userAvatarUrl = user?.profileImage?.avatarUrl()
            )
        }
        val myReview = if (isLoggedIn && !isOwner)
            reviewsUi.firstOrNull { it.userId == currentUserId } else null
        val otherReviews = if (isLoggedIn && !isOwner)
            reviewsUi.filterNot { it.userId == currentUserId } else reviewsUi

        val tipsUi = rawTips.map { tip ->
            val user = userRepository.getUserById(tip.userId).first()
            tip.toRecipeTipUi(
                userName = user?.nickname.orEmpty(),
                firstName = user?.firstName.orEmpty(),
                lastName = user?.lastName.orEmpty(),
                userAvatarUrl = user?.profileImage?.avatarUrl()
            )
        }
        val myTip = if (isLoggedIn && !isOwner)
            tipsUi.firstOrNull { it.userId == currentUserId } else null
        val otherTips = if (isLoggedIn && !isOwner)
            tipsUi.filterNot { it.userId == currentUserId } else tipsUi

        val isFavorite = favoritesCollection?.recipeIds?.contains(recipeId) ?: false

        // Calcolo di percentuale e ingredienti mancanti rispetto al frigo
        var percentageFridge = 0
        var missingIngredients = emptyList<IngredientQuantity>()
        var missingQuantityIngredients = emptyList<MissingQuantityDetail>()

        if (userFridge != null) {
            val fridgeMap = userFridge.ingredients.associateBy { it.ingredient.name.lowercase().trim() }
            val totalIngredients = recipe.ingredients.size

            val missingList = mutableListOf<IngredientQuantity>()
            val missingQuantityList = mutableListOf<MissingQuantityDetail>()
            var sufficientCount = 0

            recipe.ingredients.forEach { reqIng ->
                val reqName = reqIng.ingredient.name.lowercase()
                val fridgeItem = fridgeMap[reqName]

                if (fridgeItem == null) { // L'ingrediente manca
                    missingList.add(reqIng)
                } else { // C'è tutto
                    val hasEnoughQty = reqIng.unit == UnitOfMeasure.QB || fridgeItem.quantity >= reqIng.quantity
                    if (hasEnoughQty) {
                        sufficientCount++
                    } else { // Manca la quantità
                        missingQuantityList.add(
                            MissingQuantityDetail(
                                recipeRequirement = reqIng,
                                availableInFridge = fridgeItem.quantity.toInt()
                            )
                        )
                    }
                }
            }

            percentageFridge = (sufficientCount * 100) / totalIngredients
            missingIngredients = missingList
            missingQuantityIngredients = missingQuantityList

        } else { // frigo non ancora creato, tutti gli ingredienti mancano
            percentageFridge = 0
            missingIngredients = recipe.ingredients
            missingQuantityIngredients = emptyList()
        }

        return RecipeUiState(
            content = RecipeContentState(
                recipe = recipeWithStats.toRecipeUi(
                    authorNickname = authorNickname,
                    remixedFromRecipeTitle = originRecipe?.title,
                    remixedFromAuthorNickname = originAuthorNickname,
                    myReview = myReview,
                    otherReviews = otherReviews.sortedByDescending { r -> r.date }.take(3),
                    myTip = myTip,
                    otherTips = otherTips.sortedByDescending { t -> t.date }.take(3)
                ),
                percentageFridge = percentageFridge,
                missingIngredients = missingIngredients,
                missingQuantityIngredients = missingQuantityIngredients,
                isFavorite = isFavorite
            ),
            permissions = RecipePermissionsState(
                isOwner = isOwner,
                canAddReview = isLoggedIn && !isOwner && myReview == null,
                canAddTip = isLoggedIn && !isOwner && myTip == null
            ),
            isLoading = false
        )
    }

    fun deleteRecipe() {
        val recipeId = _uiState.value.content.recipe?.id ?: return
        viewModelScope.launch {
            try {
                reviewRepository.deleteReviewsByRecipe(recipeId)
                tipRepository.deleteTipsByRecipe(recipeId)
                collectionRepository.deleteOwnershipsByRecipe(recipeId)
                recipeRepository.deleteRecipe(recipeId)
                _uiState.update {
                    it.copy(
                        deleteDialog = it.deleteDialog.copy(showDeleteRecipeDialog = false)
                    )
                }
                _events.send(RecipeEvent.RecipeDeleted)
            } catch (e: Exception) {
                _uiState.update { it.copy(error = "Could not delete recipe: ${e.message}") }
            }
        }
    }

    fun confirmDeleteReview() {
        val reviewId = _uiState.value.deleteDialog.reviewToDeleteId ?: return
        viewModelScope.launch {
            try {
                reviewRepository.deleteReview(reviewId)
                dismissDeleteReviewDialog()
                _events.send(RecipeEvent.ReviewDeleted)
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        deleteDialog = it.deleteDialog.copy(reviewToDeleteId = null),
                        error = "Could not delete review: ${e.message}"
                    )
                }
            }
        }
    }

    fun confirmDeleteTip() {
        val tipId = _uiState.value.deleteDialog.tipToDeleteId ?: return
        viewModelScope.launch {
            try {
                tipRepository.deleteTip(tipId)
                dismissDeleteTipDialog()
                _events.send(RecipeEvent.TipDeleted)
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        deleteDialog = it.deleteDialog.copy(tipToDeleteId = null),
                        error = "Could not delete tip: ${e.message}"
                    )
                }
            }
        }
    }

    fun toggleFavorite() {
        val currentRecipe = _uiState.value.content.recipe ?: return
        val userId = SessionManagerFacade.currentUserId ?: return
        val wasFavorite = _uiState.value.content.isFavorite

        val updatedLikes = if (!wasFavorite) currentRecipe.likes + 1 else (currentRecipe.likes - 1).coerceAtLeast(0)

        viewModelScope.launch {
            try {
                collectionRepository.toggleFavourite(userId, currentRecipe.id)

                val recipe = recipeRepository.getRecipeById(currentRecipe.id).first()
                if (recipe != null) {
                    val updatedRecipe = recipe.copy(likes = updatedLikes)
                    recipeRepository.saveRecipe(updatedRecipe)
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        content = it.content.copy(
                            isFavorite = wasFavorite,
                            recipe = it.content.recipe?.copy(likes = currentRecipe.likes)
                        ),
                        error = "Could not update favorites"
                    )
                }
            }
        }
    }
    fun addMissingIngredientsToGroceryList() {
        val missing = _uiState.value.content.missingIngredients
        val insufficient = _uiState.value.content.missingQuantityIngredients
        if (missing.isEmpty() && insufficient.isEmpty()) return


        viewModelScope.launch {
            try {
                val insufficientWithTime = insufficient.map {
                    IngredientQuantityWithTime(
                        ingredient = it.recipeRequirement.ingredient,
                        quantity = it.recipeRequirement.quantity - it.availableInFridge,
                        unit = it.recipeRequirement.unit,
                        insertedAt = System.currentTimeMillis()
                    )
                }
                val missingWithTime = missing.map {
                    IngredientQuantityWithTime(
                        ingredient = it.ingredient,
                        quantity = it.quantity,
                        unit = it.unit,
                        insertedAt = System.currentTimeMillis()
                    )
                }
                val ingredientsWithTime = insufficientWithTime + missingWithTime
                saveMissingIngredientInGroceryList(ingredientsWithTime)
                _events.send(RecipeEvent.MissingIngredientsAddedToGroceryList)
            } catch (e: Exception) {
                _uiState.update { it.copy(error = "Could not add ingredients to grocery list: ${e.message}") }
            }
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Synchronous UI state updates
    // ─────────────────────────────────────────────────────────────────────────

    fun showDeleteRecipeDialog() {
        _uiState.update { it.copy(deleteDialog = it.deleteDialog.copy(showDeleteRecipeDialog = true)) }
    }

    fun dismissDeleteRecipeDialog() {
        _uiState.update { it.copy(deleteDialog = it.deleteDialog.copy(showDeleteRecipeDialog = false)) }
    }

    fun resetDeleteState() {
        _uiState.update {
            it.copy(
                deleteDialog = it.deleteDialog.copy(showDeleteRecipeDialog = false)
            )
        }
    }

    fun requestDeleteReview(reviewId: String) {
        _uiState.update { it.copy(deleteDialog = it.deleteDialog.copy(reviewToDeleteId = reviewId)) }
    }

    fun dismissDeleteReviewDialog() {
        _uiState.update { it.copy(deleteDialog = it.deleteDialog.copy(reviewToDeleteId = null)) }
    }

    fun requestDeleteTip(tipId: String) {
        _uiState.update { it.copy(deleteDialog = it.deleteDialog.copy(tipToDeleteId = tipId)) }
    }

    fun dismissDeleteTipDialog() {
        _uiState.update { it.copy(deleteDialog = it.deleteDialog.copy(tipToDeleteId = null)) }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Factory
    // ─────────────────────────────────────────────────────────────────────────

    companion object {
        val Factory = viewModelFactory {
            initializer {
                val savedStateHandle = createSavedStateHandle()
                val app = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as FridgeApplication
                RecipeViewModel(
                    savedStateHandle = savedStateHandle,
                    recipeRepository = app.container.recipeRepository,
                    userRepository = app.container.userRepository,
                    collectionRepository = app.container.collectionRepository,
                    reviewRepository = app.container.reviewRepository,
                    recipeWithStatsRepository = app.container.recipeWithStatsRepository,
                    tipRepository = app.container.tipRepository,
                    fridgeRepository = app.container.fridgeRepository,
                    saveMissingIngredientInGroceryList = app.container.saveMissingIngredientInGroceryList
                )
            }
        }
    }
}
