package com.example.fridgeproject.viewmodel.notification

data class NotificationSettingUiState (
    val receiveNotification: Boolean =  false,
    val receiveLikeNotification: Boolean =  false,
    val receiveRemixNotification: Boolean =  false,
    val receiveNewFollowerNotification: Boolean =  false,
    val receiveNewRecipeNotification: Boolean =  false,
    val receiveReviewNotification: Boolean = true,
    val receiveTipNotification: Boolean = true,
    val globalError: String = ""
)