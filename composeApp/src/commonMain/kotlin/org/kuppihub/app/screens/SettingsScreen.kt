package org.kuppihub.app.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import kuppihubappnew.composeapp.generated.resources.*
import org.jetbrains.compose.resources.stringResource
import org.kuppihub.app.ui.components.KuppiLogo
import org.kuppihub.app.ui.components.MenuOptionItem
import org.kuppihub.app.ui.theme.KuppiGradients

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBackClick: () -> Unit
) {
    val uriHandler = LocalUriHandler.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    
    // Hoist the string resource lookup out of the non-composable onClick context
    val cacheClearedMsg = stringResource(Res.string.cache_cleared)

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
             Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(KuppiGradients.MainHeader)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                        .padding(horizontal = 4.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = onBackClick) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(Res.string.back), tint = Color.Black)
                        }
                        KuppiLogo(showText = false)
                        Spacer(Modifier.width(12.dp))
                        Text(stringResource(Res.string.settings_title), style = MaterialTheme.typography.titleMedium)
                    }
                }
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        val scrollState = rememberScrollState()
        
        Box(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .background(KuppiGradients.PageBackground)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(16.dp),
                horizontalAlignment = Alignment.Start
            ) {
                // Account Actions Section
                Text(
                    text = stringResource(Res.string.account_actions_section),
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = MaterialTheme.shapes.large,
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(8.dp)) {
                        MenuOptionItem(
                            icon = Icons.Default.CleaningServices,
                            title = stringResource(Res.string.clear_cache),
                            onClick = {
                                scope.launch {
                                    snackbarHostState.showSnackbar(cacheClearedMsg)
                                }
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Contribution Section
                Text(
                    text = stringResource(Res.string.contribution_section),
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = MaterialTheme.shapes.large,
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Code,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = stringResource(Res.string.contribute_title),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Text(
                            text = stringResource(Res.string.contribute_message),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        Button(
                            onClick = { uriHandler.openUri("https://github.com/Kuppihub/") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = MaterialTheme.shapes.medium
                        ) {
                            Icon(Icons.Default.OpenInNew, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Open GitHub")
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Support & Legal Section
                Text(
                    text = stringResource(Res.string.support_section),
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = MaterialTheme.shapes.large,
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(8.dp)) {
                        MenuOptionItem(
                            icon = Icons.Default.PrivacyTip,
                            title = stringResource(Res.string.privacy_policy),
                            onClick = { uriHandler.openUri("https://kuppihub.org/privacy") }
                        )
                        MenuOptionItem(
                            icon = Icons.Default.Description,
                            title = stringResource(Res.string.terms_of_service),
                            onClick = { uriHandler.openUri("https://kuppihub.org/terms") }
                        )
                        MenuOptionItem(
                            icon = Icons.Default.BugReport,
                            title = stringResource(Res.string.report_bug),
                            onClick = { uriHandler.openUri("mailto:support@kuppihub.org?subject=Bug Report") }
                        )
                    }
                }
            }
        }
    }
}
