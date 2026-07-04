package com.example.fridgeproject.model

import com.example.fridgeproject.model.enums.NotificationType

data class NotificationUi(
    val id: String,
    val userId: String,
    val type: NotificationType,
    val triggerUserId: String,
    val triggerUsername: String,
    val triggerFirstName: String,
    val triggerLastName: String,
    val triggerUserAvatarUrl: String?,
    val recipeId: String?,
    val recipeTitle: String?,
    val timestamp: Long,
    val isRead: Boolean
)

fun Notification.toUi(
    triggerUsername: String,
    triggerFirstName: String,
    triggerLastName: String,
    triggerUserAvatarUrl: String?,
    recipeTitle: String? = null
): NotificationUi = NotificationUi(
    id = id,
    userId = userId,
    type = type,
    triggerUserId = triggerUserId,
    triggerUsername = triggerUsername,
    triggerFirstName = triggerFirstName,
    triggerLastName = triggerLastName,
    triggerUserAvatarUrl = triggerUserAvatarUrl,
    recipeId = recipeId,
    recipeTitle = recipeTitle,
    timestamp = timestamp,
    isRead = isRead
)

fun NotificationUi.toDomain(): Notification = Notification(
    id = id,
    userId = userId,
    type = type,
    triggerUserId = triggerUserId,
    recipeId = recipeId,
    timestamp = timestamp,
    isRead = isRead
)