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
import com.example.fridgeproject.domain.TipRepository
import com.example.fridgeproject.model.Tip
import com.example.fridgeproject.model.enums.TipType
import com.example.fridgeproject.navigation.NewTipRoute
import java.time.LocalDate
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class NewTipViewModel(
    savedStateHandle: SavedStateHandle,
    private val tipRepository: TipRepository
) : ViewModel() {

    private val route = savedStateHandle.toRoute<NewTipRoute>()
    private val recipeId = route.recipeId

    private val _uiState = MutableStateFlow(NewTipUiState())
    val uiState: StateFlow<NewTipUiState> = _uiState.asStateFlow()
    private val _events = Channel<TipEvent>()
    val events = _events.receiveAsFlow()


    // ─────────────────────────────────────────────────────────────────────────
    // Asynchronous actions (repository I/O)
    // ─────────────────────────────────────────────────────────────────────────

    fun publishTip() {
        val state = _uiState.value
        val currentUserId = SessionManagerFacade.currentUserId
        val isLoggedIn = currentUserId != null

        if (!validateFields(isLoggedIn)) return
        val validUserId = currentUserId!!
        val validType = state.type ?: return

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, errors = it.errors.copy(general = "")) }
            try {
                tipRepository.saveTip(
                    Tip(
                        recipeId = recipeId,
                        userId = validUserId,
                        type = validType,
                        date = LocalDate.now().toString(),
                        comment = state.tipText.trim()
                    )
                )
                _uiState.update { it.copy(isSaving = false, success = true) }
                _events.send(TipEvent.Published)
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isSaving = false,
                        errors = it.errors.copy(general = "Could not publish tip: ${e.message.orEmpty()}")
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
        return current.canCheckUnsavedChanges() && current.hasChangesFromDefault()
    }

    fun requestExit() {
        if (hasUnsavedChanges()) {
            _uiState.update { it.copy(showExitDialog = true) }
        } else {
            viewModelScope.launch { _events.send(TipEvent.ExitAllowed) }
        }
    }

    fun confirmExit() {
        _uiState.update { it.copy(showExitDialog = false) }
        viewModelScope.launch { _events.send(TipEvent.ExitAllowed) }
    }

    fun dismissExitDialog() {
        _uiState.update { it.copy(showExitDialog = false) }
        viewModelScope.launch { _events.send(TipEvent.ExitCancelled) }
    }

    private fun validateFields(isLoggedIn: Boolean): Boolean {
        val current = _uiState.value
        val isTypeMissing = current.type == null
        val isTipTextMissing = current.tipText.isBlank()
        val errors = NewTipErrors(
            type = if (isTypeMissing) "Please select DO or DON'T" else "",
            tipText = if (isTipTextMissing) "Please write your tip" else "",
            general = if (!isLoggedIn) "You must be logged in to publish a tip" else ""
        )

        _uiState.update { it.copy(errors = errors) }
        return errors.type.isBlank() &&
                errors.tipText.isBlank() &&
                errors.general.isBlank()
    }

    fun updateType(type: TipType) {
        _uiState.update {
            it.copy(
                type = type,
                errors = it.errors.copy(type = "", general = "")
            )
        }
    }

    fun updateTipText(text: String) {
        _uiState.update {
            it.copy(
                tipText = text,
                errors = it.errors.copy(tipText = "", general = "")
            )
        }
    }

    fun resetCreateState() {
        _uiState.update { it.copy(success = false) }
    }


    // ─────────────────────────────────────────────────────────────────────────
    // Factory
    // ─────────────────────────────────────────────────────────────────────────

    companion object {
        val Factory = viewModelFactory {
            initializer {
                val savedStateHandle = createSavedStateHandle()
                val app = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as FridgeApplication
                NewTipViewModel(
                    savedStateHandle = savedStateHandle,
                    tipRepository = app.container.tipRepository
                )
            }
        }
    }
}