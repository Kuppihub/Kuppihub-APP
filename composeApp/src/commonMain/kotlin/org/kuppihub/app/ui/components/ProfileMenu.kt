package org.kuppihub.app.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kuppihubappnew.composeapp.generated.resources.*
import org.jetbrains.compose.resources.stringResource

@Composable
fun ProfileMenu(
    onTutorsClick: () -> Unit,
    onAddKuppiClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onAboutClick: () -> Unit,
    onShareClick: () -> Unit,
    onCheckUpdatesClick: () -> Unit,
    isUpdateLoading: Boolean = false
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
            icon = Icons.Default.School,
            title = stringResource(Res.string.tutors_title),
            onClick = onTutorsClick
        )
        MenuOptionItem(
            icon = Icons.Default.AddCircle,
            title = stringResource(Res.string.add_kuppi_button),
            onClick = onAddKuppiClick
        )

        // --- SECTION 2: General ---
        Text(
            "General",
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
        )

        MenuOptionItem(icon = Icons.Default.Settings, title = stringResource(Res.string.settings_title), onClick = onSettingsClick)
        
        // Update Item with integrated Loading state
        MenuOptionItem(
            icon = Icons.Default.SystemUpdate, 
            title = if (isUpdateLoading) stringResource(Res.string.checking_updates) else stringResource(Res.string.check_updates), 
            onClick = onCheckUpdatesClick,
            isLoading = isUpdateLoading // 👈 This now handles the arrow-to-round replacement
        )

        MenuOptionItem(icon = Icons.Default.Info, title = stringResource(Res.string.about_title), onClick = onAboutClick)
        MenuOptionItem(icon = Icons.Default.Share, title = stringResource(Res.string.share_app), onClick = onShareClick)
    }
}
