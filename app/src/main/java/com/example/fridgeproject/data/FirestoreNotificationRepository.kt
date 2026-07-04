package com.example.fridgeproject.data

import com.example.fridgeproject.domain.Collections
import com.example.fridgeproject.domain.NotificationRepository
import com.example.fridgeproject.data.model.FirestoreNotification
import com.example.fridgeproject.data.model.toDomain
import com.example.fridgeproject.data.model.toFirestore
import com.example.fridgeproject.model.Notification
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

class FirestoreNotificationRepository(
    private val firestore: FirebaseFirestore
) : NotificationRepository {

    private val notificationsCollection = firestore.collection(Collections.NOTIFICATIONS)

    override fun getNotificationsForUser(userId: String): Flow<List<Notification>> =
        notificationsCollection
            .whereEqualTo("userId", userId)
            .snapshots()
            .map { snapshot ->
                snapshot.documents
                    .mapNotNull { document ->
                        document.toObject(FirestoreNotification::class.java)?.toDomain(document.id)
                    }
                    .sortedByDescending { it.timestamp }
            }

    override suspend fun insertNotification(notification: Notification) {
        val docRef = if (notification.id.isBlank()) {
            notificationsCollection.document()
        } else {
            notificationsCollection.document(notification.id)
        }

        docRef.set(notification.copy(id = docRef.id).toFirestore()).await()
    }

    override suspend fun markAsRead(notificationId: String) {
        notificationsCollection.document(notificationId).update("read", true).await()
    }

    override suspend fun deleteNotification(notificationId: String) {
        notificationsCollection.document(notificationId).delete().await()
    }

    override suspend fun clearAllForUser(userId: String) {
        val notifications = notificationsCollection
            .whereEqualTo("userId", userId)
            .whereEqualTo("read", false)
            .get()
            .await()

        val batch = firestore.batch()
        notifications.documents.forEach { document ->
            batch.update(document.reference, "read", true)
        }
        batch.commit().await()
    }

    override fun observeNotifications(userId: String): Flow<Notification> = callbackFlow {
        val query = notificationsCollection
            .whereEqualTo("userId", userId)

        val registration = query.addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }

            if (snapshot != null) {
                for (change in snapshot.documentChanges) {
                    if (change.type == DocumentChange.Type.ADDED) {
                        val firestoreNotif = change.document.toObject(FirestoreNotification::class.java)
                        val notification = firestoreNotif.toDomain(id = change.document.id)
                        trySend(notification)
                    }
                }
            }
        }
        awaitClose { registration.remove() }
    }

    override fun observeUnreadCount(userId: String): Flow<Int> =
        notificationsCollection
            .whereEqualTo("userId", userId)
            .whereEqualTo("read", false)
            .snapshots()
            .map { it.size() }
}
