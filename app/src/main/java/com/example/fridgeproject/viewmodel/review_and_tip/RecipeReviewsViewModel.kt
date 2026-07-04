package com.example.fridgeproject.viewmodel.review_and_tip

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
import com.example.fridgeproject.domain.RecipeRepository
import com.example.fridgeproject.domain.ReviewRepository
import com.example.fridgeproject.domain.UserRepository
import com.example.fridgeproject.model.Recipe
import com.example.fridgeproject.model.Review
import com.example.fridgeproject.model.avatarUrl
import com.example.fridgeproject.model.toRecipeReviewUi
import com.example.fridgeproject.navigation.RecipeReviewListRoute
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class RecipeReviewViewModel(
    savedStateHandle: SavedStateHandle,
    private val reviewRepository: ReviewRepository,
    private val recipeRepository: RecipeRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    private val route = savedStateHandle.toRoute<RecipeReviewListRoute>()
    private val recipeId = route.recipeId

    private val _uiState = MutableStateFlow(RecipeReviewsUiState(recipeId = recipeId))
    val uiState: StateFlow<RecipeReviewsUiState> = _uiState.asStateFlow()
    private val _events = Channel<ReviewEvent>()
    val events = _events.receiveAsFlow()

    init {
        viewModelScope.launch {
            combine(
                SessionManagerFacade.currentUserStateFlow,
                reviewRepository.getReviewsByRecipe(recipeId),
                recipeRepository.getRecipeById(recipeId)
            ) { currentUserId, rawReviews, recipe ->
                buildRecipeReviewsUiState(currentUserId, rawReviews, recipe)
            }.collect { loadedState ->
                _uiState.update { it.copy(isLoading = true) }
                _uiState.update { currentState ->
                    loadedState.copy(reviewToDeleteId = currentState.reviewToDeleteId)
                }
            }
        }
    }

    private suspend fun buildRecipeReviewsUiState(
        currentUserId: String?,
        rawReviews: List<Review>,
        recipe: Recipe?
    ): RecipeReviewsUiState {
        return try {
            val isLoggedIn = currentUserId != null
            val reviewsUi = rawReviews.map { review ->
                val user = userRepository.getUserById(review.userId).first()
                review.toRecipeReviewUi(
                    userName = user?.nickname.orEmpty(),
                    firstName = user?.firstName.orEmpty(),
                    lastName = user?.lastName.orEmpty(),
                    userAvatarUrl = user?.profileImage?.avatarUrl()
                )
            }

            val myReview = if (isLoggedIn && recipe?.authorId != currentUserId)
                reviewsUi.firstOrNull { it.userId == currentUserId } else null
            val otherReviews = if (isLoggedIn && recipe?.authorId != currentUserId)
                reviewsUi.filterNot { it.userId == currentUserId } else reviewsUi

            RecipeReviewsUiState(
                recipeId = recipeId,
                myReview = myReview,
                otherReviews = otherReviews,
                canAddReview = isLoggedIn && recipe?.authorId != currentUserId && myReview == null,
                isLoading = false
            )
        } catch (e: Exception) {
            RecipeReviewsUiState(
                recipeId = recipeId,
                isLoading = false,
                globalError = "Error: ${e.message}"
            )
        }
    }


    fun confirmDeleteReview() {
        val reviewId = _uiState.value.reviewToDeleteId ?: return
        viewModelScope.launch {
            try {
                reviewRepository.deleteReview(reviewId)
                dismissDeleteReviewDialog()
                _events.send(ReviewEvent.Deleted)
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        reviewToDeleteId = null,
                        globalError = "Could not delete review: ${e.message}"
                    )
                }
            }
        }
    }


    // ─────────────────────────────────────────────────────────────────────────
    // Synchronous UI state updates
    // ─────────────────────────────────────────────────────────────────────────

    fun requestDeleteReview(reviewId: String) {
        _uiState.update { it.copy(reviewToDeleteId = reviewId) }
    }

    fun dismissDeleteReviewDialog() {
        _uiState.update { it.copy(reviewToDeleteId = null) }
    }


    // ─────────────────────────────────────────────────────────────────────────
    // Factory
    // ─────────────────────────────────────────────────────────────────────────

    companion object {
        val Factory = viewModelFactory {
            initializer {
                val savedStateHandle = createSavedStateHandle()
                val app = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as FridgeApplication
                RecipeReviewViewModel(
                    savedStateHandle = savedStateHandle,
                    reviewRepository = app.container.reviewRepository,
                    recipeRepository = app.container.recipeRepository,
                    userRepository = app.container.userRepository
                )
            }
        }
    }
}