package org.kuppihub.app.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import org.kuppihub.app.ui.components.KuppiLogo
import org.kuppihub.app.ui.theme.KuppiGradients
import org.kuppihub.app.ui.theme.Blue50
import org.kuppihub.app.ui.theme.White
import org.kuppihub.app.viewmodel.DashboardViewModel

@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel,
    userId: String?,
    onModuleClick: (Int, String) -> Unit,
    onAddModuleClick: () -> Unit
) {
    // ✅ FIX 1: Observe the ViewModel's state instead of creating local state
    // This ensures that when ViewModel updates (e.g. after delete), the UI updates automatically.
    val displayModules by viewModel.dashboardModules.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    // ✅ FIX 2: Only trigger the load function. Do NOT fetch data manually here.
    LaunchedEffect(userId) {
        // This handles everything: Guest mode, Sync, and Loading
        viewModel.loadUserDashboard(userId)
    }
    LaunchedEffect(Unit) {
        viewModel.snackbarEvent.collect { message ->
            snackbarHostState.showSnackbar(message)
        }
    }

    Scaffold(
        containerColor = Blue50,
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
                        .padding(horizontal = 16.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    KuppiLogo()
                }
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddModuleClick,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = White
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Module")
            }
        }
    ) { p ->
        Box(modifier = Modifier.padding(p).fillMaxSize()) {

            // 1. Loading State
            if (isLoading && displayModules.isEmpty()) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }

            // 2. Empty State
            else if (displayModules.isEmpty()) {
                Column(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "No modules added yet.",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Start by adding your first module.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(24.dp))

                    Button(
                        onClick = onAddModuleClick,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.Transparent
                        ),
                        contentPadding = PaddingValues(),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .background(KuppiGradients.PrimaryButton)
                                .padding(horizontal = 24.dp, vertical = 12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("Browse Modules", color = White)
                        }
                    }
                }
            }

            // 3. List of Modules
            else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Show error banner if sync failed (but we still show cached data)
                    if (errorMessage != null) {
                        item {
                            Text(
                                text = "⚠️ Offline Mode: Showing cached data",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.error,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                        }
                    }

                    items(displayModules) { item ->
                        Box(
                            modifier = Modifier
                                .clickable { onModuleClick(item.module.id, item.module.code) }
                        ) {
                            // Make sure your ModuleCard is using the correct imports
                            ModuleCard(
                                item = item,
                                isAdded = true,
                                isInDashboardScreen = true,
                                onActionButtonClick = {
                                    // ✅ FIX 3: This now updates the ViewModel,
                                    // and because of FIX 1, the UI will disappear instantly!
                                    viewModel.removeModule(item.module.id, userId)
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}