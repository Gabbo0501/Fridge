package com.example.fridgeproject.viewmodel.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.fridgeproject.FridgeApplication
import com.example.fridgeproject.data.repository.auth.SessionManagerFacade
import com.example.fridgeproject.domain.ReviewRepository
import com.example.fridgeproject.domain.TipRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val reviewRepository: ReviewRepository,
    private val tipRepository: TipRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState = _uiState.asStateFlow()

    init {
        loadCounts()
    }

    private fun loadCounts() {
        val userId = SessionManagerFacade.currentUserId
        if (userId == null) {
            _uiState.update {
                it.copy(
                    isLoading = false,
                    globalError = "Unauthorized access"
                )
            }
            return
        }

        viewModelScope.launch {
            try {
                combine(
                    tipRepository.getTipsCountByUser(userId),
                    reviewRepository.getReviewsCountByUser(userId)
                ) { tipsCount, reviewsCount ->
                    SettingsUiState(
                        tipsCount = tipsCount,
                        reviewsCount = reviewsCount,
                        isLoading = false
                    )
                }.collect { state ->
                    _uiState.value = state
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        globalError = "Error loading settings: ${e.message}"
                    )
                }
            }
        }
    }

    companion object {
        val Factory = viewModelFactory {
            initializer {
                val app = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as FridgeApplication
                SettingsViewModel(
                    reviewRepository = app.container.reviewRepository,
                    tipRepository = app.container.tipRepository
                )
            }
        }
    }
}