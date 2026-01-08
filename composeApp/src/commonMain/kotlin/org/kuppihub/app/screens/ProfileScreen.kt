package org.kuppihub.app.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import org.kuppihub.app.auth.GoogleAuthService
import org.kuppihub.app.model.KuppiUser
import org.kuppihub.app.ui.components.KuppiLogo
import org.kuppihub.app.ui.components.ProfileCard
import org.kuppihub.app.ui.components.ProfileMenu
import org.kuppihub.app.ui.theme.KuppiGradients

@Composable
fun ProfileScreen(
    user: KuppiUser?,
    onLoginClick: () -> Unit,
    onLogoutClick: () -> Unit,
    authService: GoogleAuthService? = null,
    desktopUser: String? = null,
    onTutorsClick: () -> Unit,
    onAddKuppiClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onAboutClick: () -> Unit,
    onShareClick: () -> Unit
) {
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(KuppiGradients.MainHeader)
            ) {
                // TIGHT TOOLBAR (Empty as per design, just the gradient header)
                Box(modifier = Modifier.height(20.dp))
            }
        }
    ) { p ->
        // Using scroll state in case content overflows on small screens
        val scrollState = rememberScrollState()
        
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(KuppiGradients.PageBackground)
                .padding(p)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                // 2. ACCOUNT CARD
                ProfileCard(
                    user = user,
                    onLoginClick = onLoginClick,
                    onLogoutClick = onLogoutClick
                )

                Spacer(modifier = Modifier.height(24.dp))

                // 3. SETTINGS & INFO MENU
                // We wrap it in a surface/card for better grouping on the gradient background
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = MaterialTheme.shapes.large,
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        ProfileMenu(
                            onSettingsClick = onSettingsClick,
                            onAboutClick = onAboutClick,
                            onShareClick = onShareClick,
                            onTutorsClick = onTutorsClick,
                            onAddKuppiClick = onAddKuppiClick,
                        )
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                // 1. BRANDING HEADER
                Spacer(modifier = Modifier.height(32.dp))
                KuppiLogo(modifier = Modifier.scale(1.2f))
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "KuppiHub v1.1.2",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.secondary
                )

                Spacer(modifier = Modifier.height(32.dp))

                // 4. FOOTER
                Text("Made with ❤️ by UOM Engineering", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}
