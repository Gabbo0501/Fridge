package com.example.fridgeproject.data.model

import com.example.fridgeproject.model.Notification
import com.example.fridgeproject.model.enums.NotificationType

data class FirestoreNotification(
    val userId: String = "",
    val type: String = NotificationType.NEW_FOLLOWER.name,
    val triggerUserId: String = "",
    val recipeId: String? = null,
    val timestamp: Long = System.currentTimeMillis(),
    val read: Boolean = false
)

fun FirestoreNotification.toDomain(id: String): Notification =
    Notification(
        id = id,
        userId = userId,
        type = type.toNotificationType(),
        triggerUserId = triggerUserId,
        recipeId = recipeId,
        timestamp = timestamp,
        isRead = read
    )

fun Notification.toFirestore(): FirestoreNotification =
    FirestoreNotification(
        userId = userId,
        type = type.name,
        triggerUserId = triggerUserId,
        recipeId = recipeId,
        timestamp = timestamp,
        read = isRead
    )

private fun String.toNotificationType(): NotificationType =
    try {
        NotificationType.valueOf(uppercase())
    } catch (e: Exception) {
        NotificationType.NEW_FOLLOWER
    }
