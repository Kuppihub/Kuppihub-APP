package org.kuppihub.app.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
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
import org.kuppihub.app.ui.theme.Blue50
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
        containerColor = Blue50,
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(KuppiGradients.MainHeader)
            ) {
                // TIGHT TOOLBAR
            }
        }
    ) { p ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(p)
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
            ProfileMenu(
                onSettingsClick = onSettingsClick,
                onAboutClick = onAboutClick,
                onShareClick = onShareClick,
                onTutorsClick = onTutorsClick,
                onAddKuppiClick = onAddKuppiClick,
            )

            Spacer(modifier = Modifier.weight(1f))

            // 1. BRANDING HEADER
            Spacer(modifier = Modifier.height(24.dp))
            KuppiLogo(modifier = Modifier.scale(1.2f))
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "KuppiHub v1.0.5",
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
