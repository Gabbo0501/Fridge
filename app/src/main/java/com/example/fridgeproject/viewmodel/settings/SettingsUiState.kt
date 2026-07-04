package com.example.fridgeproject.viewmodel.settings

data class SettingsUiState(
    val tipsCount: Int = 0,
    val reviewsCount: Int = 0,
    val isLoading: Boolean = true,
    val globalError: String = ""
)