package com.example.fridgeproject.viewmodel.notification

import com.example.fridgeproject.model.NotificationUi

data class NotificationUiState (
    val unreadNotifications: List<NotificationUi> = emptyList(),
    val readNotifications: List<NotificationUi> = emptyList(),
    val isLoading: Boolean = false,
    val globalError: String = ""
)