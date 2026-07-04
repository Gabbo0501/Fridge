package com.example.fridgeproject.navigation

import com.example.fridgeproject.navigation.utils.ExitRequestManager

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import com.example.fridgeproject.data.repository.auth.SessionManagerFacade
import com.example.fridgeproject.ui.theme.FridgeOrange
import com.example.fridgeproject.ui.theme.NavGrey

data class BottomNavItem(
    val label: String,
    val icon: ImageVector,
    val route: Any
)

@Composable
fun AppNavigationBar(
    navController: NavHostController,
    currentDestination: NavDestination?,
    exitRequestManager: ExitRequestManager
) {
    val currentUserId by SessionManagerFacade.currentUserStateFlow.collectAsStateWithLifecycle()

    val items = listOf(
        BottomNavItem("Home", Icons.Filled.Home, HomeRoute),
        BottomNavItem("Explore", Icons.Filled.Explore, ExploreGraph),
        BottomNavItem("Create", Icons.Filled.AddCircleOutline, CreateRecipeRoute),
        BottomNavItem("My Fridge", Icons.Filled.Kitchen, FridgeGraph(currentUserId)),
        BottomNavItem("Profile", Icons.Filled.AccountBox, ProfileGraph(currentUserId))
    )

    NavigationBar (
        containerColor = Color.White
    ) {
        items.forEach { item ->
            NavigationBarItem(
                selected = currentDestination?.hierarchy?.any {
                    it.hasRoute(item.route::class)
                } == true,
                onClick = {
                    exitRequestManager.requestExitBefore {
                        navController.navigate(item.route) {
                            popUpTo(navController.graph.findStartDestination().id)
                            launchSingleTop = true
                        }
                    }
                },
                icon = { Icon(item.icon, contentDescription = item.label) },
                label = { Text(item.label) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = FridgeOrange,
                    selectedTextColor = FridgeOrange,
                    unselectedIconColor = NavGrey,
                    unselectedTextColor = NavGrey,
                    indicatorColor = Color.Transparent
                )
            )
        }
    }
}