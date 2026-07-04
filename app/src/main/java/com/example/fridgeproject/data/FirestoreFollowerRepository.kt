package com.example.fridgeproject.data

import com.example.fridgeproject.data.model.FirestoreFollowerData
import com.example.fridgeproject.data.model.FirestoreUser
import com.example.fridgeproject.data.model.toDomain
import com.example.fridgeproject.domain.Collections
import com.example.fridgeproject.domain.FollowerRepository
import com.example.fridgeproject.domain.NotificationRepository
import com.example.fridgeproject.model.Notification
import com.example.fridgeproject.model.UserProfile
import com.example.fridgeproject.model.enums.NotificationType
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.tasks.await
import kotlin.collections.mapNotNull
import kotlin.jvm.java

class FirestoreFollowerRepository (
    private val firestore: FirebaseFirestore,
    private val notificationRepository: NotificationRepository
): FollowerRepository {

    private val followerCollection = firestore.collection(Collections.FOLLOWER)
    private val usersCollection = firestore.collection(Collections.USERS)


    override suspend fun followUser(followerId: String, followedId: String) {
        val deterministicId = "${followerId}_${followedId}"

        val followerData = FirestoreFollowerData(
            followerId = followerId,
            followedId = followedId,
            timestamp = System.currentTimeMillis()
        )

        followerCollection.document(deterministicId).set(followerData).await()

        val notification_id = "follow_${followerId}_${followedId}"
        val notification = Notification(notification_id, followedId, NotificationType.NEW_FOLLOWER, followerId, null,
            System.currentTimeMillis(), false)
        notificationRepository.insertNotification(notification)
    }

    override suspend fun unfollowUser(followerId: String, followedId: String) {
        val deterministicId = "${followerId}_${followedId}"

        followerCollection.document(deterministicId).delete().await()

        val notification_id = "follow_${followerId}_${followedId}"
        try {
            notificationRepository.deleteNotification(notification_id)
        }catch(e : Exception){
            println("Errore durante la generazione della notifica: ${e.message}")
        }
    }

    override suspend fun isFollowing(followerId: String, followedId: String): Boolean {
        val deterministicId = "${followerId}_${followedId}"

        val snapshot = followerCollection.document(deterministicId).get().await()
        return snapshot.exists()
    }

    override fun getFollowersFlow(userId: String): Flow<List<UserProfile>> =
        followFlow(
            query = followerCollection.whereEqualTo("followedId", userId),
            getTargetId = { it.followerId }
        )

    override fun getFollowingFlow(userId: String): Flow<List<UserProfile>> =
        followFlow(
            query = followerCollection.whereEqualTo("followerId", userId),
            getTargetId = { it.followedId }
        )

    private fun followFlow(
        query: Query,
        getTargetId: (FirestoreFollowerData) -> String
    ): Flow<List<UserProfile>> =
        combine(
            query.snapshots(),
            usersCollection.snapshots()
        ) { followSnapshot, userSnapshot ->

            val usersMap = userSnapshot.documents
                .mapNotNull { document ->
                    document.toObject(FirestoreUser::class.java)?.let { firestoreUser ->
                        document.id to firestoreUser.toDomain(id = document.id, ingredientsMap = emptyMap())
                    }
                }
                .toMap()

            followSnapshot.documents
                .mapNotNull { document ->
                    document.toObject(FirestoreFollowerData::class.java)
                }
                .mapNotNull { follow ->
                    val targetUserId = getTargetId(follow)
                    usersMap[targetUserId]
                }
        }
}