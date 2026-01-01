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
import androidx.navigation.toRoute
import org.kuppihub.app.navigation.*
import org.kuppihub.app.screens.*
import org.kuppihub.app.auth.GoogleAuthService
import org.kuppihub.app.model.KuppiUser

@Composable
fun MainScreen(
    onGoogleLoginClick: () -> Unit,
    onLogoutClick: () -> Unit,
    authService: GoogleAuthService? = null,
    currentUser: KuppiUser?
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    Scaffold(
        bottomBar = {
            NavigationBar {
                BottomTab.allTabs.forEach { tab ->
                    val isSelected = currentDestination?.hasRoute(tab.route::class) == true
                    NavigationBarItem(
                        icon = { Icon(tab.icon, contentDescription = tab.label) },
                        label = { Text(tab.label) },
                        selected = isSelected,
                        onClick = {
                            navController.navigate(tab.route) {
                                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = DashboardRoute,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable<DashboardRoute> {
                DashboardScreen(
                    onModuleClick = { moduleId, code -> navController.navigate(KuppiListRoute(moduleId, code)) },
                    onAddModuleClick = {
                        navController.navigate(AddModulesRoute) {
                            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }
            composable<AddModulesRoute> {
                AddModulesScreen(onFacultyClick = { navController.navigate(LevelOneRoute(it)) })
            }
            composable<LevelOneRoute> { backStackEntry ->
                val route = backStackEntry.toRoute<LevelOneRoute>()
                LevelOneScreen(route.facultyId, onItemClick = { navController.navigate(LevelTwoRoute(route.facultyId, it)) }, onBackClick = { navController.popBackStack() })
            }
            composable<LevelTwoRoute> { backStackEntry ->
                val route = backStackEntry.toRoute<LevelTwoRoute>()
                LevelTwoScreen(route.facultyId, route.childId, onItemClick = { navController.navigate(LevelThreeRoute(route.facultyId, route.childId, it)) }, onBackClick = { navController.popBackStack() })
            }
            composable<LevelThreeRoute> { backStackEntry ->
                val route = backStackEntry.toRoute<LevelThreeRoute>()
                LevelThreeScreen(route.facultyId, route.childId, route.semesterId, onBackClick = { navController.popBackStack() })
            }

            // 👇 THIS IS THE PART THAT USES YOUR ACCOUNT CARD
            composable<ProfileRoutes> {
                ProfileScreen(
                    user = currentUser,          // Passing the unified user
                    onLoginClick = onGoogleLoginClick,
                    onLogoutClick = onLogoutClick,
                    authService = authService
                )
            }

            composable<KuppiListRoute> { backStackEntry ->
                val route = backStackEntry.toRoute<KuppiListRoute>()
                KuppiListScreen(route.moduleId, route.moduleCode, onBackClick = { navController.popBackStack() })
            }
        }
    }
}