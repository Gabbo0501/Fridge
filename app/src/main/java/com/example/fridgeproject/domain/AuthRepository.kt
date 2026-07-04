package com.example.fridgeproject.domain

import android.content.Context
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

interface AuthRepository {
    val currentUserId: String?
    val currentUserEmail: String?
    val currentUserDisplayName: String?
    val currentUserState: Flow<String?>
    val currentUserStateFlow: StateFlow<String?>
    suspend fun signIn(context: Context): Result<FirebaseUser?>
    val isLoggedIn: Boolean
    suspend fun logOut()
}