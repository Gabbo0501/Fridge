package com.example.fridgeproject.data.repository.auth

sealed interface AuthState {
    object Registering : AuthState
    object Authenticated : AuthState
    object Unauthenticated : AuthState
}