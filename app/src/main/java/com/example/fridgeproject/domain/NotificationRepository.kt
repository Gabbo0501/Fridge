package com.example.fridgeproject.domain

import com.example.fridgeproject.model.Notification
import kotlinx.coroutines.flow.Flow

interface NotificationRepository {
    fun getNotificationsForUser(userId: String): Flow<List<Notification>>
    fun observeNotifications(userId: String): Flow<Notification>

    fun observeUnreadCount(userId: String): Flow<Int>
    suspend fun insertNotification(notification: Notification)
    suspend fun markAsRead(notificationId: String)
    suspend fun deleteNotification(notificationId: String)
    suspend fun clearAllForUser(userId: String)
}