package com.example.fridgeproject.model

import com.example.fridgeproject.model.enums.NotificationType

data class Notification(
    val id: String = "",
    val userId: String,
    val type: NotificationType,
    val triggerUserId: String,
    val recipeId: String? = null,
    val timestamp: Long = System.currentTimeMillis(),
    val isRead: Boolean = false
)