package org.kuppihub.app.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.kuppihub.app.model.KuppiUser
import org.kuppihub.app.ui.theme.Blue50
import org.kuppihub.app.ui.theme.White

@Composable
fun ProfileCard(
    user: KuppiUser?,
    onLoginClick: () -> Unit,
    onLogoutClick: () -> Unit
) {
    val isLoggedIn = user != null
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = White),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (isLoggedIn && user != null) {
                // LOGGED IN VIEW
                Icon(
                    imageVector = Icons.Default.AccountCircle,
                    contentDescription = "User",
                    modifier = Modifier.size(64.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(8.dp))

                Text(user.name, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Text(user.email, style = MaterialTheme.typography.bodyMedium)

                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = onLogoutClick,
                    colors = ButtonDefaults.buttonColors(containerColor = Blue50)
                ) {
                    Text("Log Out", color = MaterialTheme.colorScheme.onSurface)
                }
            } else {
                // GUEST VIEW
                Text(
                    "Join the Community",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "Log in to sync your modules across devices.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
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
}
