package com.example.fridgeproject.navigation

import com.example.fridgeproject.navigation.utils.ExitRequestManager

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hasRoute
import com.example.fridgeproject.data.repository.auth.SessionManagerFacade
import com.example.fridgeproject.viewmodel.notification.GlobalNotificationViewModel
import kotlinx.coroutines.launch

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val navActions = remember(navController) { AppNavigationActions(navController) }
    val exitRequestManager = remember { ExitRequestManager() }
    val snackbarHostState = remember { SnackbarHostState() }
    val snackbarScope = rememberCoroutineScope()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    val currentUserId by SessionManagerFacade.currentUserStateFlow.collectAsStateWithLifecycle()
    val isLoggedIn = currentUserId != null

    val isRegistrationScreen =
    currentDestination?.hasRoute(RegistrationRoute::class) == true
    val isOnboardingScreen =
        currentDestination?.hasRoute(OnboardingRoute::class) == true
    val showBars = !isRegistrationScreen && !isOnboardingScreen

    val globalNotificationViewModel: GlobalNotificationViewModel = viewModel(
        factory = GlobalNotificationViewModel.Factory
    )

    val unreadNotificationCount by globalNotificationViewModel.unreadNotification.collectAsStateWithLifecycle()

    LaunchedEffect(isLoggedIn) {
        if (isLoggedIn) {
            globalNotificationViewModel.snackbarEvent.collect { message ->
                snackbarHostState.showSnackbar(
                    message = message,
                    duration = SnackbarDuration.Short
                )
            }
        }
    }

    Scaffold(
        topBar = {
            if(showBars) {
                AppTopBar(
                    currentDestination,
                    onBackHome = { 
                        exitRequestManager.requestExitBefore {
                            navActions.navigateHome() 
                        } 
                    },
                    onNavigateToNotification = {
                        if (!isLoggedIn) {
                            navActions.navigateTo(LogInRoute)
                        } else {
                            exitRequestManager.requestExitBefore {
                                navActions.navigateTo(NotificationRoute(currentUserId!!))
                            }
                        }
                    },
                    onNavigateToSettings = {
                        if (!isLoggedIn) {
                            navActions.navigateTo(LogInRoute)
                        } else {
                            exitRequestManager.requestExitBefore {
                                navActions.navigateToSettings(currentUserId!!)
                            }
                        }
                    },
                    onFilterClick = { 
                        navActions.navigateTo(FilterRoute)
                    },
                    showNotificationAction = isLoggedIn,
                    unreadNotificationCount = unreadNotificationCount,
                    showSettingsAction = navActions.isViewingOwnProfile(currentUserId)
                )
            }
         },
        bottomBar = {
            if(showBars) {
                AppNavigationBar(
                    navController = navController,
                    currentDestination = currentDestination,
                    exitRequestManager = exitRequestManager
                )
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        AppNavHost(
            navController = navController,
            navActions = navActions,
            exitRequestManager = exitRequestManager,
            modifier = Modifier.padding(paddingValues),
            onShowSnackbar = { message ->
                snackbarScope.launch {
                    snackbarHostState.showSnackbar(message)
                }
            }
        )
    }
}