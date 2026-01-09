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
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.unit.dp
import kuppihubappnew.composeapp.generated.resources.*
import org.jetbrains.compose.resources.stringResource
import org.kuppihub.app.auth.GoogleAuthService
import org.kuppihub.app.model.KuppiUser
import org.kuppihub.app.ui.components.KuppiLogo
import org.kuppihub.app.ui.components.ProfileCard
import org.kuppihub.app.ui.components.ProfileMenu
import org.kuppihub.app.ui.theme.KuppiGradients
import org.kuppihub.app.viewmodel.UpdateViewModel

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
    val updateViewModel = remember { UpdateViewModel() }
    val updateInfo by updateViewModel.updateInfo.collectAsState()
    val downloadUrl by updateViewModel.downloadUrl.collectAsState()
    val isLoadingUpdates by updateViewModel.isLoading.collectAsState()
    val updateError by updateViewModel.error.collectAsState()
    
    val uriHandler = LocalUriHandler.current
    val currentVersion = stringResource(Res.string.version)

    // Update Dialog
    if (updateInfo != null) {
        AlertDialog(
            onDismissRequest = { updateViewModel.clearUpdateInfo() },
            title = { Text(stringResource(Res.string.update_available_title)) },
            text = { Text(stringResource(Res.string.update_available_message, updateInfo!!.tag_name)) },
            confirmButton = {
                Button(onClick = {
                    val urlToOpen = downloadUrl ?: updateInfo!!.html_url
                    uriHandler.openUri(urlToOpen)
                    updateViewModel.clearUpdateInfo()
                }) {
                    Text(stringResource(Res.string.update_now))
                }
            },
            dismissButton = {
                TextButton(onClick = { updateViewModel.clearUpdateInfo() }) {
                    Text(stringResource(Res.string.cancel))
                }
            }
        )
    }

    val snackbarHostState = remember { SnackbarHostState() }
    
    LaunchedEffect(updateError) {
        if (updateError == "no_update") {
            snackbarHostState.showSnackbar(currentVersion + " is the latest version")
            updateViewModel.clearUpdateInfo()
        } else if (updateError != null) {
            snackbarHostState.showSnackbar(updateError!!)
            updateViewModel.clearUpdateInfo()
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(KuppiGradients.MainHeader)
            ) {
                Box(modifier = Modifier.height(20.dp))
            }
        }
    ) { p ->
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
                            onCheckUpdatesClick = {
                                updateViewModel.checkForUpdates(currentVersion)
                            }
                        )
                    }
                }

                if (isLoadingUpdates) {
                    Spacer(modifier = Modifier.height(16.dp))
                    CircularProgressIndicator(modifier = Modifier.size(24.dp))
                    Text(stringResource(Res.string.checking_updates), style = MaterialTheme.typography.labelSmall)
                }

                Spacer(modifier = Modifier.weight(1f))

                // 1. BRANDING HEADER
                Spacer(modifier = Modifier.height(32.dp))
                KuppiLogo(modifier = Modifier.scale(1.2f))
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = stringResource(Res.string.version),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.secondary
                )

                Spacer(modifier = Modifier.height(32.dp))

                // 4. FOOTER
                Text(
                    text = stringResource(Res.string.footer_text),
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.Gray
                )
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}
