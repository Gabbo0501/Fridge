package com.example.fridgeproject.data

import com.example.fridgeproject.domain.Collections
import com.example.fridgeproject.domain.ImageStorageRepository
import com.example.fridgeproject.domain.CollectionRepository
import com.example.fridgeproject.domain.UserRepository
import com.example.fridgeproject.domain.IngredientRepository
import com.example.fridgeproject.data.model.FirestoreUser
import com.example.fridgeproject.data.model.toDomain
import com.example.fridgeproject.data.model.toFirestore
import com.example.fridgeproject.model.LocalImageInput
import com.example.fridgeproject.model.ProfileImageSource
import com.example.fridgeproject.model.UserProfile
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

class FirestoreUserRepository(
    private val firestore: FirebaseFirestore,
    private val imageStorageRepository: ImageStorageRepository,
    private val collectionRepository: CollectionRepository,
    private val ingredientRepository: IngredientRepository
) : UserRepository {

    private val usersCollection = firestore.collection(Collections.USERS)

    override fun getUserById(id: String): Flow<UserProfile?> {
        val userSnapshotFlow = usersCollection.document(id).snapshots()

        val ingredientsMapFlow = ingredientRepository.getAllIngredients()
            .map { list -> list.associateBy { it.name } }

        return userSnapshotFlow.combine(ingredientsMapFlow) { snapshot, ingredientsMap ->
            snapshot.toObject(FirestoreUser::class.java)?.toDomain(snapshot.id, ingredientsMap)
        }
    }

    override suspend fun saveUser(user: UserProfile, pendingProfileImage: LocalImageInput?) {
        val docRef = if (user.id.isBlank()) {
            usersCollection.document()
        } else {
            usersCollection.document(user.id)
        }

        val existingUser = docRef.get().await()
            .toObject(FirestoreUser::class.java)
            ?.toDomain(docRef.id, emptyMap())

        val profileImageToSave = profileImageForSave(
            userId = docRef.id,
            selectedProfileImage = user.profileImage,
            pendingProfileImage = pendingProfileImage
        )

        docRef.set(user.copy(id = docRef.id, profileImage = profileImageToSave).toFirestore()).await()
        if (existingUser == null) {
            collectionRepository.createSystemCollections(docRef.id)
        }
        if (shouldDeleteProfileImages(existingUser, user.profileImage, pendingProfileImage)) {
            imageStorageRepository.deleteProfileImages(docRef.id)
        }
    }

    override suspend fun deleteUser(id: String) {
        imageStorageRepository.deleteProfileImages(id)
        collectionRepository.deleteCollectionsByUser(id)
        usersCollection.document(id).delete().await()
    }

    private suspend fun profileImageForSave(
        userId: String,
        selectedProfileImage: ProfileImageSource,
        pendingProfileImage: LocalImageInput?
    ): ProfileImageSource {
        val localInput = pendingProfileImage ?: (selectedProfileImage as? ProfileImageSource.Local)?.input
        if (localInput != null) {
            val uploadedImage = imageStorageRepository.uploadProfileImage(
                userId = userId,
                input = localInput
            )
            return ProfileImageSource.Remote(uploadedImage)
        }

        return when (selectedProfileImage) {
            is ProfileImageSource.Remote -> selectedProfileImage
            is ProfileImageSource.Monogram -> ProfileImageSource.Monogram
            is ProfileImageSource.Local -> ProfileImageSource.Monogram
        }
    }

    private fun shouldDeleteProfileImages(
        existingUser: UserProfile?,
        selectedProfileImage: ProfileImageSource,
        pendingProfileImage: LocalImageInput?
    ): Boolean =
        existingUser?.profileImage is ProfileImageSource.Remote &&
                selectedProfileImage is ProfileImageSource.Monogram &&
                pendingProfileImage == null
}
