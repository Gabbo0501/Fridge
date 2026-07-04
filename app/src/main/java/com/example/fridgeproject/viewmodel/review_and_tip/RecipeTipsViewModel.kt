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
import com.example.fridgeproject.domain.TipRepository
import com.example.fridgeproject.domain.UserRepository
import com.example.fridgeproject.model.Recipe
import com.example.fridgeproject.model.Tip
import com.example.fridgeproject.model.avatarUrl
import com.example.fridgeproject.model.toRecipeTipUi
import com.example.fridgeproject.navigation.RecipeTipsListRoute
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class RecipeTipsViewModel(
    savedStateHandle: SavedStateHandle,
    private val tipRepository: TipRepository,
    private val recipeRepository: RecipeRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    private val route = savedStateHandle.toRoute<RecipeTipsListRoute>()
    private val recipeId = route.recipeId

    private val _uiState = MutableStateFlow(RecipeTipsUiState(recipeId = recipeId))
    val uiState: StateFlow<RecipeTipsUiState> = _uiState.asStateFlow()
    private val _events = Channel<TipEvent>()
    val events = _events.receiveAsFlow()

    init {
        viewModelScope.launch {
            combine(
                SessionManagerFacade.currentUserStateFlow,
                tipRepository.getTipsByRecipe(recipeId),
                recipeRepository.getRecipeById(recipeId)
            ) { currentUserId, rawTips, recipe ->
                buildRecipeTipsUiState(currentUserId, rawTips, recipe)
            }.collect { loadedState ->
                _uiState.update { it.copy(isLoading = true) }
                _uiState.update { currentState ->
                    loadedState.copy(tipToDeleteId = currentState.tipToDeleteId)
                }
            }
        }
    }

    private suspend fun buildRecipeTipsUiState(
        currentUserId: String?,
        rawTips: List<Tip>,
        recipe: Recipe?
    ): RecipeTipsUiState {
        return try {
            val isLoggedIn = currentUserId != null
            val tipsUi = rawTips.map { tip ->
                val user = userRepository.getUserById(tip.userId).first()
                tip.toRecipeTipUi(
                    userName = user?.nickname.orEmpty(),
                    firstName = user?.firstName.orEmpty(),
                    lastName = user?.lastName.orEmpty(),
                    userAvatarUrl = user?.profileImage?.avatarUrl()
                )
            }

            val myTip = if (isLoggedIn && recipe?.authorId != currentUserId)
                tipsUi.firstOrNull { it.userId == currentUserId } else null
            val otherTips = if (isLoggedIn && recipe?.authorId != currentUserId)
                tipsUi.filterNot { it.userId == currentUserId } else tipsUi

            RecipeTipsUiState(
                recipeId = recipeId,
                myTip = myTip,
                otherTips = otherTips,
                canAddTip = isLoggedIn && recipe?.authorId != currentUserId && myTip == null,
                isLoading = false
            )
        } catch (e: Exception) {
            RecipeTipsUiState(
                recipeId = recipeId,
                isLoading = false,
                globalError = "Error: ${e.message}"
            )
        }
    }

    fun confirmDeleteTip() {
        val tipId = _uiState.value.tipToDeleteId ?: return
        viewModelScope.launch {
            try {
                tipRepository.deleteTip(tipId)
                dismissDeleteTipDialog()
                _events.send(TipEvent.Deleted)
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        tipToDeleteId = null,
                        globalError = "Could not delete tip: ${e.message}"
                    )
                }
            }
        }
    }


    // ─────────────────────────────────────────────────────────────────────────
    // Synchronous UI state updates
    // ─────────────────────────────────────────────────────────────────────────

    fun requestDeleteTip(tipId: String) {
        _uiState.update { it.copy(tipToDeleteId = tipId) }
    }

    fun dismissDeleteTipDialog() {
        _uiState.update { it.copy(tipToDeleteId = null) }
    }


    // ─────────────────────────────────────────────────────────────────────────
    // Factory
    // ─────────────────────────────────────────────────────────────────────────

    companion object {
        val Factory = viewModelFactory {
            initializer {
                val savedStateHandle = createSavedStateHandle()
                val app = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as FridgeApplication
                RecipeTipsViewModel(
                    savedStateHandle = savedStateHandle,
                    tipRepository = app.container.tipRepository,
                    recipeRepository = app.container.recipeRepository,
                    userRepository = app.container.userRepository
                )
            }
        }
    }
}