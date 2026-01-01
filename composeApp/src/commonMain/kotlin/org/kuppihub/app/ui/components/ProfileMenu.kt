package org.kuppihub.app.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun ProfileMenu(
    onSettingsClick: () -> Unit = {},
    onAboutClick: () -> Unit = {},
    onShareClick: () -> Unit = {}
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            "General",
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        MenuOptionItem(icon = Icons.Default.Settings, title = "App Settings", onClick = onSettingsClick)
        MenuOptionItem(icon = Icons.Default.Info, title = "About KuppiHub", onClick = onAboutClick)
        MenuOptionItem(icon = Icons.Default.Share, title = "Share App", onClick = onShareClick)
    }
}
