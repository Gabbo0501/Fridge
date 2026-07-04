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
import com.example.fridgeproject.domain.TipRepository
import com.example.fridgeproject.domain.UserRepository
import com.example.fridgeproject.model.toUserTipUi
import com.example.fridgeproject.navigation.MyTipsRoute
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ProfileTipsViewModel(
    savedStateHandle: SavedStateHandle,
    private val tipRepository: TipRepository,
    private val recipeRepository: RecipeRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    private val route = savedStateHandle.toRoute<MyTipsRoute>()
    private val userId = route.profileUserId

    private val _uiState = MutableStateFlow(ProfileTipsUiState())
    val uiState: StateFlow<ProfileTipsUiState> = _uiState.asStateFlow()
    private val _events = Channel<TipEvent>()
    val events = _events.receiveAsFlow()

    init {
        loadUserTips(userId)
    }


    // ─────────────────────────────────────────────────────────────────────────
    // Asynchronous actions (repository I/O)
    // ─────────────────────────────────────────────────────────────────────────

    private fun loadUserTips(id: String) {
        viewModelScope.launch {
            tipRepository.getTipsByUser(id).collect { tips ->
                val tipsUi = tips.map { tip ->
                    val recipe = recipeRepository.getRecipeById(tip.recipeId).first()
                    val recipeAuthorUsername = recipe?.let {
                        userRepository.getUserById(it.authorId).first()?.nickname
                    }.orEmpty()
                    tip.toUserTipUi(
                        recipeTitle = recipe?.title.orEmpty(),
                        recipeAuthorId = recipe?.authorId.orEmpty(),
                        recipeAuthorUsername = recipeAuthorUsername
                    )
                }

                _uiState.update { it.copy(tips = tipsUi) }
            }
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
                ProfileTipsViewModel(
                    savedStateHandle = savedStateHandle,
                    tipRepository = app.container.tipRepository,
                    recipeRepository = app.container.recipeRepository,
                    userRepository = app.container.userRepository
                )
            }
        }
    }
}
