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
import org.kuppihub.app.navigation.AddModulesRoute
import org.kuppihub.app.navigation.BottomTab
import org.kuppihub.app.navigation.DashboardRoute
import org.kuppihub.app.navigation.LevelOneRoute
import org.kuppihub.app.navigation.LevelThreeRoute
import org.kuppihub.app.navigation.LevelTwoRoute
import org.kuppihub.app.navigation.LoginRoute
import org.kuppihub.app.screens.AddModulesScreen
import org.kuppihub.app.screens.DashboardScreen
import org.kuppihub.app.screens.LevelOneScreen
import org.kuppihub.app.screens.LevelTwoScreen
import org.kuppihub.app.screens.LevelThreeScreen
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


        // Inside ui/MainScreen.kt

        NavHost(
            navController = navController,
            startDestination = DashboardRoute,
            modifier = Modifier.padding(innerPadding)
        ) {

            composable<DashboardRoute> {
                DashboardScreen()
            }


            // 1. Dashboard (The Root)
            composable<AddModulesRoute> {
                AddModulesScreen(
                    onFacultyClick = { facultyId ->
                        // "Go to the next level" (whatever that level is named)
                        navController.navigate(LevelOneRoute(facultyId))
                    }
                )
            }

            // 2. Generic Level One (Years, Departments, etc.)
            composable<LevelOneRoute> { backStackEntry ->
                val route = backStackEntry.toRoute<LevelOneRoute>()
                LevelOneScreen(
                    facultyId = route.facultyId,
                    onItemClick = { childId ->
                        // "Go to the final level" (Semesters, Terms)
                        navController.navigate(LevelTwoRoute(route.facultyId, childId))
                    }
                )
            }

            // 3. Generic Level Two (Semesters, Terms)
            composable<LevelTwoRoute> { backStackEntry ->
                val route = backStackEntry.toRoute<LevelTwoRoute>()
                LevelTwoScreen(
                    facultyId = route.facultyId,
                    childId = route.childId,
                    // 👇 YOU MUST ADD THIS BLOCK
                    onItemClick = { semesterId ->
                        navController.navigate(
                            LevelThreeRoute(route.facultyId, route.childId, semesterId)
                        )
                    }
                )
            }


            composable<LevelThreeRoute> { backStackEntry ->
                val route = backStackEntry.toRoute<LevelThreeRoute>()
                LevelThreeScreen(
                    facultyId = route.facultyId,
                    childId = route.childId,
                    semesterId = route.semesterId
                )
            }

            // Login (Keep as is)
            composable<LoginRoute> { LoginScreen {} }
        }
    }
}

