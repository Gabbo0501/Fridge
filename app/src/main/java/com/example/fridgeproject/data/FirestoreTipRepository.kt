package com.example.fridgeproject.data

import com.example.fridgeproject.domain.Collections
import com.example.fridgeproject.domain.TipRepository
import com.example.fridgeproject.data.model.FirestoreTip
import com.example.fridgeproject.data.model.toDomain
import com.example.fridgeproject.data.model.toFirestore
import com.example.fridgeproject.domain.NotificationRepository
import com.example.fridgeproject.model.Notification
import com.example.fridgeproject.model.Tip
import com.example.fridgeproject.model.enums.NotificationType
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

class FirestoreTipRepository(
    private val firestore: FirebaseFirestore,
    private val notificationRepository: NotificationRepository
) : TipRepository {

    private val tipsCollection = firestore.collection(Collections.TIPS)

    override fun getTipById(id: String): Flow<Tip?> =
        tipsCollection.document(id)
            .snapshots()
            .map { snapshot ->
                snapshot.toObject(FirestoreTip::class.java)?.toDomain(snapshot.id)
            }

    override fun getTipsByRecipe(recipeId: String): Flow<List<Tip>> =
        tipsFlow("recipeId", recipeId)

    override fun getTipsByUser(userId: String): Flow<List<Tip>> =
        tipsFlow("userId", userId)

    override fun getTipsCountByUser(userId: String): Flow<Int> =
        tipsCollection
            .whereEqualTo("userId", userId)
            .snapshots()
            .map { snapshot -> snapshot.size() }

    override suspend fun saveTip(tip: Tip) {
        val existing = tipsCollection
            .whereEqualTo("recipeId", tip.recipeId)
            .whereEqualTo("userId", tip.userId)
            .limit(1)
            .get()
            .await()
            .documents
            .firstOrNull()

        val docRef = when {
            tip.id.isNotBlank() -> tipsCollection.document(tip.id)
            existing != null -> existing.reference
            else -> tipsCollection.document()
        }
        docRef.set(tip.copy(id = docRef.id).toFirestore()).await()
        try {
            val recipeDoc = firestore.collection(Collections.RECIPES)
                .document(tip.recipeId)
                .get()
                .await()
            val recipeAuthorId = recipeDoc.getString("authorId")
            val deterministicNotificationId = "tip_${tip.userId}_${tip.recipeId}"
            if (recipeAuthorId != null && recipeAuthorId != tip.userId) {
                val authorDoc = firestore.collection(Collections.USERS).document(recipeAuthorId).get().await()
                val globalEnabled = authorDoc.getBoolean("receiveNotification") ?: true
                val tipEnabled = authorDoc.getBoolean("receiveTipNotification") ?: true
                if (globalEnabled && tipEnabled) {
                    val notification = Notification(
                        id = deterministicNotificationId,
                        userId = recipeAuthorId,
                        type = NotificationType.TIP,
                        triggerUserId = tip.userId,
                        recipeId = tip.recipeId,
                        timestamp = System.currentTimeMillis(),
                        isRead = false
                    )
                    notificationRepository.insertNotification(notification)
                }
            }
        } catch (e: Exception) {
            println("Errore durante la generazione della notifica: ${e.message}")
        }
    }

    override suspend fun deleteTip(id: String) {
        try {
            val tipDoc = tipsCollection.document(id).get().await()
            val userId = tipDoc.getString("userId")
            val recipeId = tipDoc.getString("recipeId")
            tipsCollection.document(id).delete().await()
            if (!userId.isNullOrBlank() && !recipeId.isNullOrBlank()) {
                val deterministicNotificationId = "tip_${userId}_${recipeId}"
                notificationRepository.deleteNotification(deterministicNotificationId)
            }
        } catch (e: Exception) {
            println("Errore durante l'eliminazione del tip o della relativa notifica: ${e.message}")
        }
    }

    override suspend fun deleteTipsByRecipe(recipeId: String) {
        val tips = tipsCollection
            .whereEqualTo("recipeId", recipeId)
            .get()
            .await()
            .documents

        tips.forEach { document ->
            val userId = document.getString("userId")
            document.reference.delete().await()
            if (!userId.isNullOrBlank()) {
                val deterministicNotificationId = "tip_${userId}_${recipeId}"
                notificationRepository.deleteNotification(deterministicNotificationId)
            }
        }
    }

    private fun tipsFlow(field: String, value: String): Flow<List<Tip>> =
        tipsCollection
            .whereEqualTo(field, value)
            .snapshots()
            .map { snapshot ->
                snapshot.documents
                    .mapNotNull { document ->
                        document.toObject(FirestoreTip::class.java)?.toDomain(document.id)
                    }
                    .sortedByDescending { it.date }
            }
}