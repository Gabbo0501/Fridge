package com.example.fridgeproject.navigation.utils

import com.example.fridgeproject.navigation.*

import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.fridgeproject.data.repository.auth.SessionManagerFacade

@Composable
fun AuthenticatedRoute(
    navActions: AppNavigationActions,
    content: @Composable () -> Unit
) {
    val currentUserId by SessionManagerFacade.currentUserStateFlow.collectAsStateWithLifecycle()
    val isLoggedIn = currentUserId != null

    if (!isLoggedIn) {
        LaunchedEffect(Unit) {
            navActions.navigateToAuth()
        }
    } else {
        content()
    }
}

@Composable
fun ProfileRouteGuard(
    profileUserId: String?,
    navActions: AppNavigationActions,
    content: @Composable () -> Unit
) {
    val currentUserId by SessionManagerFacade.currentUserStateFlow.collectAsStateWithLifecycle()
    val isLoggedIn = currentUserId != null

    if (profileUserId.isNullOrBlank() && !isLoggedIn) {
        LaunchedEffect(Unit) {
            navActions.navigateToAuth()
        }
    } else {
        content()
    }
}
