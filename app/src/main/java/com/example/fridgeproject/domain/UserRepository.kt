package com.example.fridgeproject.domain

import kotlinx.coroutines.flow.Flow
import com.example.fridgeproject.model.LocalImageInput
import com.example.fridgeproject.model.UserProfile

interface UserRepository {
    fun getUserById(id: String): Flow<UserProfile?>

    suspend fun saveUser(user: UserProfile, pendingProfileImage: LocalImageInput? = null)

    suspend fun deleteUser(id: String)
}