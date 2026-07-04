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
import com.example.fridgeproject.navigation.EditReviewRoute
import java.time.LocalDate
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class EditReviewViewModel(
    savedStateHandle: SavedStateHandle,
    private val reviewRepository: ReviewRepository
) : ViewModel() {

    private val route = savedStateHandle.toRoute<EditReviewRoute>()
    private val reviewId = route.reviewId
    private var originalReview: Review? = null

    private val _uiState = MutableStateFlow(EditReviewUiState(isLoading = true))
    val uiState: StateFlow<EditReviewUiState> = _uiState.asStateFlow()
    private val _events = Channel<ReviewEvent>()
    val events = _events.receiveAsFlow()

    init {
        loadReview()
    }


    // ─────────────────────────────────────────────────────────────────────────
    // Asynchronous actions (repository I/O)
    // ─────────────────────────────────────────────────────────────────────────

    private fun loadReview() {
        viewModelScope.launch {
            val review = reviewRepository.getReviewById(reviewId).first()
            originalReview = review
            if (review == null) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errors = it.errors.copy(general = "Review not found")
                    )
                }
            } else {
                _uiState.update {
                    it.copy(
                        rating = review.stars,
                        reviewText = review.comment,
                        images = review.images,
                        isLoading = false
                    )
                }
            }
        }
    }

    fun saveReview() {
        val original = originalReview ?: return
        val currentUserId = SessionManagerFacade.currentUserId

        if (currentUserId != original.userId) {
            _uiState.update {
                it.copy(errors = it.errors.copy(general = "You can only edit your own review"))
            }
            return
        }

        if (!validateFields()) return

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, errors = it.errors.copy(general = "")) }
            try {
                val state = _uiState.value
                val reviewToSave = original.copy(
                    stars = state.rating,
                    date = LocalDate.now().toString(),
                    comment = state.reviewText.trim(),
                    images = state.images
                )
                reviewRepository.saveReview(
                    review = reviewToSave,
                    pendingImages = state.pendingImages
                )
                originalReview = reviewToSave
                _uiState.update { it.copy(isSaving = false, success = true, pendingImages = emptyList()) }
                _events.send(ReviewEvent.Updated)
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isSaving = false,
                        errors = it.errors.copy(general = "Could not save review: ${e.message.orEmpty()}")
                    )
                }
            }
        }
    }


    // ─────────────────────────────────────────────────────────────────────────
    // Synchronous UI state updates
    // ─────────────────────────────────────────────────────────────────────────

    fun hasUnsavedChanges(): Boolean {
        val original = originalReview ?: return false
        val current = _uiState.value
        return current.canCheckUnsavedChanges() && current.hasChangesFrom(original)
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

    private fun validateFields(): Boolean {
        val current = _uiState.value
        val errors = EditReviewErrors(
            rating = if (current.rating == 0) "Please select a rating" else "",
            reviewText = if (current.reviewText.isBlank()) "Please write your review" else ""
        )

        _uiState.update { it.copy(errors = errors) }
        return errors.rating.isBlank() && errors.reviewText.isBlank()
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

    fun resetEditState() {
        _uiState.update { it.copy(success = false) }
    }


    // ─────────────────────────────────────────────────────────────────────────
    // Factory
    // ─────────────────────────────────────────────────────────────────────────

    companion object {
        private const val MAX_REVIEW_IMAGES = 3

        val Factory = viewModelFactory {
            initializer {
                val savedStateHandle = createSavedStateHandle()
                val app = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as FridgeApplication
                EditReviewViewModel(
                    savedStateHandle = savedStateHandle,
                    reviewRepository = app.container.reviewRepository
                )
            }
        }
    }
}