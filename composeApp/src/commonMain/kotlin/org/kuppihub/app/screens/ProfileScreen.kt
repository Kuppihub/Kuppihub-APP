package org.kuppihub.app.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.auth.auth
import org.kuppihub.app.auth.GoogleAuthService
import org.kuppihub.app.ui.components.KuppiLogo // 👈 Your custom logo


@Composable
fun ProfileScreen(
    onLoginClick: () -> Unit,
    authService: GoogleAuthService?
) {
    // Mock State: In a real app, you'd get this from your Auth Repository
    val currentUser = remember { Firebase.auth.currentUser }
    val isLoggedIn = currentUser != null

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        // 1. BRANDING HEADER
        Spacer(modifier = Modifier.height(24.dp))
        KuppiLogo(modifier = Modifier.scale(1.2f)) // Bigger Logo
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "KuppiHub v1.0.0",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.secondary
        )

        Spacer(modifier = Modifier.height(32.dp))

        // 2. ACCOUNT CARD (Login or User Profile)
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (isLoggedIn) {
                    // LOGGED IN VIEW
                    Icon(
                        imageVector = Icons.Default.AccountCircle,
                        contentDescription = "User",
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(currentUser?.displayName ?: "Unknown User")
                    Text("Sangeeth", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Text("student@kuppihub.org", style = MaterialTheme.typography.bodyMedium)
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = { /* Handle Logout */ },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Text("Log Out", color = MaterialTheme.colorScheme.onSurface)
                    }
                } else {
                    // GUEST VIEW (Login Prompt)
                    Text(
                        "Join the Community",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Log in to sync your modules across devices.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = onLoginClick,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Text("Login / Sign Up")
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // 3. SETTINGS & INFO MENU
        Text(
            "General",
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.align(Alignment.Start).padding(bottom = 8.dp)
        )

        MenuOptionItem(icon = Icons.Default.Settings, title = "App Settings", onClick = {})
        MenuOptionItem(icon = Icons.Default.Info, title = "About KuppiHub", onClick = {})
        MenuOptionItem(icon = Icons.Default.Share, title = "Share App", onClick = {})

        Spacer(modifier = Modifier.weight(1f)) // Pushes content up

        // 4. FOOTER
        Text("Made with ❤️ by UOM Engineering", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
        Spacer(modifier = Modifier.height(16.dp))
    }
}

// Helper Component for Menu Items
@Composable
fun MenuOptionItem(icon: ImageVector, title: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
        }
        Spacer(modifier = Modifier.width(16.dp))
        Text(title, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
        Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(20.dp))
    }
}

// Helper to scale modifier
fun Modifier.scale(scale: Float): Modifier = this.then(Modifier.graphicsLayer(scaleX = scale, scaleY = scale))
