package com.example.fridgeproject.viewmodel.auth

data class RegistrationUiState(
    val email: String = "",
    val firstName: String = "",
    val lastName: String = "",
    val nickname: String = "",
    val isProfileSaved: Boolean = false,
    val errorMessage: String? = null
)