package org.kuppihub.app.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun ProfileMenu(
    onTutorsClick: () -> Unit,      // 👈 New
    onAddKuppiClick: () -> Unit,    // 👈 New
    onSettingsClick: () -> Unit,
    onAboutClick: () -> Unit,
    onShareClick: () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {

        // --- SECTION 1: Community ---
        Text(
            "Community",
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        MenuOptionItem(
            icon = Icons.Default.School, // Icon for Tutors
            title = "Find Tutors",
            onClick = onTutorsClick
        )
        MenuOptionItem(
            icon = Icons.Default.AddCircle, // Icon for Adding
            title = "Add New Kuppi",
            onClick = onAddKuppiClick
        )

        // --- SECTION 2: General ---
        Text(
            "General",
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
        )

        MenuOptionItem(icon = Icons.Default.Settings, title = "App Settings", onClick = onSettingsClick)
        MenuOptionItem(icon = Icons.Default.Info, title = "About KuppiHub", onClick = onAboutClick)
        MenuOptionItem(icon = Icons.Default.Share, title = "Share App", onClick = onShareClick)
    }
}