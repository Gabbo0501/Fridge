package com.example.fridgeproject.viewmodel.review_and_tip

import com.example.fridgeproject.model.UserTipUi

data class ProfileTipsUiState (
    val tips: List<UserTipUi> = emptyList(),
    val tipToDeleteId: String? = null,
    val globalError: String = ""
)