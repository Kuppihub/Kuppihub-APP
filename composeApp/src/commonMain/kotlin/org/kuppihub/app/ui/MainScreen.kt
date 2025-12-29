package org.kuppihub.app.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import org.kuppihub.app.navigation.BottomTab
import org.kuppihub.app.navigation.DashboardRoute
import org.kuppihub.app.navigation.LoginRoute
import org.kuppihub.app.screens.DashboardScreen
import org.kuppihub.app.screens.LoginScreen

@Composable
fun MainScreen() {
    val navController = rememberNavController()

    // Get current route to highlight correct tab
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    Scaffold(
        bottomBar = {
            NavigationBar {
                // Loop through the list we defined in Step 1
                BottomTab.allTabs.forEach { tab ->
                    val isSelected = currentDestination?.hasRoute(tab.route::class) == true

                    NavigationBarItem(
                        icon = { Icon(tab.icon, contentDescription = tab.label) },
                        label = { Text(tab.label) },
                        selected = isSelected,
                        onClick = {
                            navController.navigate(tab.route) {
                                // Standard Bottom Navigation logic
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        // The Container for your screens
        NavHost(
            navController = navController,
            startDestination = DashboardRoute,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable<DashboardRoute> { DashboardScreen() }
            composable<LoginRoute> { LoginScreen(onLoginClick = {}) }
        }
    }
}