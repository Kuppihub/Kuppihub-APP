package org.kuppihub.app.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.dp
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
import org.kuppihub.app.ui.theme.White
import org.kuppihub.app.viewmodel.DashboardViewModel

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
    val dashboardViewModel = remember { DashboardViewModel() }

    Scaffold(
        bottomBar = {
            // Recommendation: White is clean and provides good contrast against Blue50 background.
            // We add a subtle top border (Blue100 / primaryContainer) to tie it into the theme.
            val borderColor = MaterialTheme.colorScheme.primaryContainer

            NavigationBar(
                containerColor = White,
                tonalElevation = 8.dp,
                modifier = Modifier.drawBehind {
                    drawLine(
                        color = borderColor,
                        start = Offset(0f, 0f),
                        end = Offset(size.width, 0f),
                        strokeWidth = 1.dp.toPx()
                    )
                }
            ) {
                BottomTab.allTabs.forEach { tab ->
                    val isSelected = currentDestination?.hasRoute(tab.route::class) == true
                    NavigationBarItem(
                        icon = { Icon(tab.icon, contentDescription = tab.label) },
                        label = { Text(tab.label) },
                        selected = isSelected,
                        colors = NavigationBarItemDefaults.colors(
                            indicatorColor = MaterialTheme.colorScheme.primaryContainer, // Highlight color
                            selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant
                        ),
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
            // 1. DASHBOARD
            composable<DashboardRoute> {
                // ✅ ALWAYS show the dashboard.
                // We pass 'currentUser?.id' which might be null (Guest) or "123" (Logged In).
                DashboardScreen(
                    viewModel = dashboardViewModel,
                    userId = currentUser?.id,
                    onModuleClick = { moduleId, code ->
                        navController.navigate(KuppiListRoute(moduleId, code))
                    },
                    onAddModuleClick = {
                        navController.navigate(AddModulesRoute) {
                            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }// ✅ CRITICAL: This bracket closes DashboardRoute

            // 2. ADD MODULES
            composable<AddModulesRoute> {
                AddModulesScreen(onFacultyClick = { navController.navigate(LevelOneRoute(it)) })
            }

            // 3. HIERARCHY SCREENS
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

            // 4. PROFILE
            composable<ProfileRoutes> {
                ProfileScreen(
                    user = currentUser,
                    onLoginClick = onGoogleLoginClick,
                    onLogoutClick = onLogoutClick,
                    authService = authService
                )
            }

            // 5. KUPPI LIST
            composable<KuppiListRoute> { backStackEntry ->
                val route = backStackEntry.toRoute<KuppiListRoute>()
                KuppiListScreen(route.moduleId, route.moduleCode, onBackClick = { navController.popBackStack() })
            }
        }
    }
}
