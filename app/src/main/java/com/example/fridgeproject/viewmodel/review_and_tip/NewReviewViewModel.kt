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
import com.example.fridgeproject.domain.ReviewRepository
import com.example.fridgeproject.model.LocalImageInput
import com.example.fridgeproject.model.Review
import com.example.fridgeproject.model.StoredImage
import com.example.fridgeproject.navigation.NewReviewRoute
import java.time.LocalDate
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class NewReviewViewModel(
    savedStateHandle: SavedStateHandle,
    private val reviewRepository: ReviewRepository
) : ViewModel() {

    private val route = savedStateHandle.toRoute<NewReviewRoute>()
    private val recipeId = route.recipeId

    private val _uiState = MutableStateFlow(NewReviewUiState())
    val uiState: StateFlow<NewReviewUiState> = _uiState.asStateFlow()
    private val _events = Channel<ReviewEvent>()
    val events = _events.receiveAsFlow()


    // ─────────────────────────────────────────────────────────────────────────
    // Asynchronous actions (repository I/O)
    // ─────────────────────────────────────────────────────────────────────────

    fun publishReview() {
        val state = _uiState.value
        val currentUserId = SessionManagerFacade.currentUserId
        val isLoggedIn = currentUserId != null

        if (!validateFields(isLoggedIn)) return
        val validUserId = currentUserId!!

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, errors = it.errors.copy(general = "")) }
            try {
                reviewRepository.saveReview(
                    review = Review(
                        recipeId = recipeId,
                        userId = validUserId,
                        stars = state.rating,
                        date = LocalDate.now().toString(),
                        comment = state.reviewText.trim(),
                        images = state.images
                    ),
                    pendingImages = state.pendingImages
                )
                _uiState.update { it.copy(isSaving = false, success = true) }
                _events.send(ReviewEvent.Published)
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isSaving = false,
                        errors = it.errors.copy(general = "Could not publish review: ${e.message.orEmpty()}")
                    )
                }
            }
        }
    }


    // ─────────────────────────────────────────────────────────────────────────
    // Synchronous UI state updates
    // ─────────────────────────────────────────────────────────────────────────

    private fun validateFields(isLoggedIn: Boolean): Boolean {
        val current = _uiState.value
        val isRatingMissing = current.rating == 0
        val isReviewTextMissing = current.reviewText.isBlank()
        val errors = NewReviewErrors(
            rating = if (isRatingMissing) "Please select a rating" else "",
            reviewText = if (isReviewTextMissing) "Please write your review" else "",
            general = if (!isLoggedIn) "You must be logged in to publish a review" else ""
        )

        _uiState.update { it.copy(errors = errors) }
        return errors.rating.isBlank() &&
                errors.reviewText.isBlank() &&
                errors.general.isBlank()
    }

    fun hasUnsavedChanges(): Boolean {
        val current = _uiState.value
        return current.canCheckUnsavedChanges() && current.hasChangesFromDefault()
    }

    fun requestExit() {
        if (hasUnsavedChanges()) {
            _uiState.update { it.copy(showExitDialog = true) }
        } else {
            viewModelScope.launch { _events.send(ReviewEvent.ExitAllowed) }
        }
    }

    fun confirmExit() {
        _uiState.update { it.copy(showExitDialog = false) }
        viewModelScope.launch { _events.send(ReviewEvent.ExitAllowed) }
    }

    fun dismissExitDialog() {
        _uiState.update { it.copy(showExitDialog = false) }
        viewModelScope.launch { _events.send(ReviewEvent.ExitCancelled) }
    }

    fun updateRating(rating: Int) {
        _uiState.update {
            it.copy(
                rating = rating.coerceIn(1, 5),
                errors = it.errors.copy(rating = "", general = "")
            )
        }
    }

    fun updateReviewText(text: String) {
        _uiState.update {
            it.copy(
                reviewText = text,
                errors = it.errors.copy(reviewText = "", general = "")
            )
        }
    }

    fun addReviewImage(input: LocalImageInput) {
        _uiState.update { state ->
            if (state.images.size + state.pendingImages.size >= MAX_REVIEW_IMAGES) {
                state.copy(errors = state.errors.copy(images = "You can add up to 3 photos"))
            } else {
                state.copy(
                    pendingImages = state.pendingImages + input,
                    errors = state.errors.copy(images = "", general = "")
                )
            }
        }
    }

    fun removeReviewImage(image: StoredImage) {
        _uiState.update { state ->
            state.copy(
                images = state.images - image,
                errors = state.errors.copy(images = "", general = "")
            )
        }
    }

    fun removePendingReviewImage(input: LocalImageInput) {
        _uiState.update { state ->
            state.copy(
                pendingImages = state.pendingImages - input,
                errors = state.errors.copy(images = "", general = "")
            )
        }
    }

    fun resetCreateState() {
        _uiState.update { it.copy(success = false) }
    }


    // ─────────────────────────────────────────────────────────────────────────
    // Factory
    // ─────────────────────────────────────────────────────────────────────────ù

    companion object {
        private const val MAX_REVIEW_IMAGES = 3

        val Factory = viewModelFactory {
            initializer {
                val savedStateHandle = createSavedStateHandle()
                val app = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as FridgeApplication
                NewReviewViewModel(
                    savedStateHandle = savedStateHandle,
                    reviewRepository = app.container.reviewRepository
                )
            }
        }
    }
}