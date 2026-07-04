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
import com.example.fridgeproject.navigation.EditTipRoute
import java.time.LocalDate
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class EditTipViewModel(
    savedStateHandle: SavedStateHandle,
    private val tipRepository: TipRepository
) : ViewModel() {

    private val route = savedStateHandle.toRoute<EditTipRoute>()
    private val tipId = route.tipId
    private var originalTip: Tip? = null

    private val _uiState = MutableStateFlow(EditTipUiState(isLoading = true))
    val uiState: StateFlow<EditTipUiState> = _uiState.asStateFlow()
    private val _events = Channel<TipEvent>()
    val events = _events.receiveAsFlow()

    init {
        loadTip()
    }


    // ─────────────────────────────────────────────────────────────────────────
    // Asynchronous actions (repository I/O)
    // ─────────────────────────────────────────────────────────────────────────

    private fun loadTip() {
        viewModelScope.launch {
            val tip = tipRepository.getTipById(tipId).first()
            originalTip = tip

            if (tip == null) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errors = it.errors.copy(general = "Tip not found")
                    )
                }
            } else {
                _uiState.update {
                    it.copy(
                        type = tip.type,
                        tipText = tip.comment,
                        isLoading = false
                    )
                }
            }
        }
    }

    fun saveTip() {
        val original = originalTip ?: return
        val currentUserId = SessionManagerFacade.currentUserId

        if (currentUserId != original.userId) {
            _uiState.update {
                it.copy(errors = it.errors.copy(general = "You can only edit your own tip"))
            }
            return
        }

        if (!validateFields()) return
        val validType = _uiState.value.type ?: return

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, errors = it.errors.copy(general = "")) }
            try {
                val state = _uiState.value
                val tipToSave = original.copy(
                    type = validType,
                    date = LocalDate.now().toString(),
                    comment = state.tipText.trim()
                )
                tipRepository.saveTip(tipToSave)
                originalTip = tipToSave
                _uiState.update { it.copy(isSaving = false, success = true) }
                _events.send(TipEvent.Updated)
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isSaving = false,
                        errors = it.errors.copy(general = "Could not save tip: ${e.message.orEmpty()}")
                    )
                }
            }
        }
    }


    // ─────────────────────────────────────────────────────────────────────────
    // Synchronous UI state updates
    // ─────────────────────────────────────────────────────────────────────────

    fun hasUnsavedChanges(): Boolean {
        val original = originalTip ?: return false
        val current = _uiState.value
        return current.canCheckUnsavedChanges() && current.hasChangesFrom(original)
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

    private fun validateFields(): Boolean {
        val current = _uiState.value
        val errors = EditTipErrors(
            type = if (current.type == null) "Please select DO or DON'T" else "",
            tipText = if (current.tipText.isBlank()) "Please write your tip" else ""
        )

        _uiState.update { it.copy(errors = errors) }
        return errors.type.isBlank() && errors.tipText.isBlank()
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

    fun resetEditState() {
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
                EditTipViewModel(
                    savedStateHandle = savedStateHandle,
                    tipRepository = app.container.tipRepository
                )
            }
        }
    }
}