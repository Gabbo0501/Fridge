package com.example.fridgeproject.data.repository.auth

import android.content.Context
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.example.fridgeproject.R
import com.example.fridgeproject.domain.AuthRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.tasks.await

object SessionManagerFacade : AuthRepository {

    private val auth: FirebaseAuth get() = FirebaseAuth.getInstance()

    private val _currentUser = MutableStateFlow(auth.currentUser?.uid)

    override val currentUserStateFlow: StateFlow<String?> = _currentUser.asStateFlow()

    override val currentUserState: Flow<String?> = _currentUser.asStateFlow()

    init {
        auth.addAuthStateListener { a ->
            _currentUser.value = a.currentUser?.uid
        }
    }

    override val currentUserId: String?
        get() = _currentUser.value // null se non logged

    override val currentUserEmail: String?
        get() = auth.currentUser?.email

    override val currentUserDisplayName: String?
        get() = auth.currentUser?.displayName

    override val isLoggedIn: Boolean
        get() = _currentUser.value != null


    override suspend fun signIn(context: Context): Result<FirebaseUser?> {
        return try {
            val googleIdOption = GetSignInWithGoogleOption.Builder(
                context.getString(R.string.default_web_client_id)
            ).build()

            val request = GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build()

            val result = CredentialManager.create(context).getCredential(context, request)
            val googleIdTokenCredential =
                GoogleIdTokenCredential.createFrom(result.credential.data)
            val firebaseCredential =
                GoogleAuthProvider.getCredential(googleIdTokenCredential.idToken, null)

            val authResult = auth.signInWithCredential(firebaseCredential).await()

            Result.success(authResult.user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun saveFcmToken(userId: String) {
        try {
            val token = FirebaseMessaging.getInstance().token.await()
            FirebaseFirestore.getInstance()
                .collection("users")
                .document(userId)
                .set(mapOf("fcmToken" to token), com.google.firebase.firestore.SetOptions.merge())
                .await()
        } catch (e: Exception) {
            println("Error saving FCM token: ${e.message}")
        }
    }

    override suspend fun logOut() {
        auth.signOut()
    }
}