package org.kuppihub.app.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.ui.graphics.vector.ImageVector

// 1. Define what a "Tab" looks like
sealed class BottomTab(
    val label: String,
    val icon: ImageVector,
    val route: Any // The Serializable route object
) {
    // 2. Create the specific tabs
    data object Dashboard : BottomTab("Dashboard", Icons.Default.Home, DashboardRoute)
    data object Profile : BottomTab("Profile", Icons.Default.Person, ProfileRoutes)

    object AddModules : BottomTab(
        route = AddModulesRoute,
        icon = Icons.Default.AddCircle, // Choose a suitable icon
        label = "Add Modules"
    )

    // Helper list to loop through in the UI
    companion object {
        val allTabs = listOf(Dashboard, AddModules, Profile)
    }
}