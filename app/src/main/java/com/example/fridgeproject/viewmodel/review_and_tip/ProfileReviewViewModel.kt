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
import com.example.fridgeproject.domain.RecipeRepository
import com.example.fridgeproject.domain.ReviewRepository
import com.example.fridgeproject.domain.UserRepository
import com.example.fridgeproject.model.toUserReviewUi
import com.example.fridgeproject.navigation.MyReviewsRoute
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ProfileReviewViewModel(
    savedStateHandle: SavedStateHandle,
    private val reviewRepository: ReviewRepository,
    private val recipeRepository: RecipeRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    private val route = savedStateHandle.toRoute< MyReviewsRoute>()
    private val userId = route.profileUserId

    private val _uiState = MutableStateFlow(ProfileReviewUiState())
    val uiState: StateFlow<ProfileReviewUiState> = _uiState.asStateFlow()
    private val _events = Channel<ReviewEvent>()
    val events = _events.receiveAsFlow()

    init {
        loadUserReviews(userId)
    }


    // ─────────────────────────────────────────────────────────────────────────
    // Asynchronous actions (repository I/O)
    // ─────────────────────────────────────────────────────────────────────────

    private fun loadUserReviews(id: String) {
        viewModelScope.launch {
            reviewRepository.getReviewsByUser(id).collect { reviews ->
                val reviewsUi = reviews.map { review ->
                    val recipe = recipeRepository.getRecipeById(review.recipeId).first()
                    val recipeAuthorUsername = recipe?.let {
                        userRepository.getUserById(it.authorId).first()?.nickname
                    }.orEmpty()
                    review.toUserReviewUi(
                        recipeTitle = recipe?.title.orEmpty(),
                        recipeAuthorId = recipe?.authorId.orEmpty(),
                        recipeAuthorUsername = recipeAuthorUsername
                    )
                }

                _uiState.update { it.copy(reviews = reviewsUi) }
            }
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
                ProfileReviewViewModel(
                    savedStateHandle = savedStateHandle,
                    reviewRepository = app.container.reviewRepository,
                    recipeRepository = app.container.recipeRepository,
                    userRepository = app.container.userRepository
                )
            }
        }
    }
}