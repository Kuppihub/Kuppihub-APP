package org.kuppihub.app.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import org.kuppihub.app.ui.components.KuppiLogo
import org.kuppihub.app.ui.theme.KuppiGradients
import org.kuppihub.app.ui.theme.White
import org.kuppihub.app.viewmodel.DashboardViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel,
    userId: String?,
    onModuleClick: (Int, String) -> Unit,
    onAddModuleClick: () -> Unit
) {
    val displayModules by viewModel.dashboardModules.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(userId) {
        viewModel.loadUserDashboard(userId)
    }
    LaunchedEffect(Unit) {
        viewModel.snackbarEvent.collect { message ->
            snackbarHostState.showSnackbar(message)
        }
    }

    var showDeleteDialog by remember { mutableStateOf<Pair<Int, String>?>(null) } // Store ID and Code

    if (showDeleteDialog != null) {
        val (moduleId, moduleCode) = showDeleteDialog!!
        AlertDialog(
            onDismissRequest = { showDeleteDialog = null },
            title = { Text("Remove Module?") },
            text = { Text("Are you sure you want to remove $moduleCode from your dashboard?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.removeModule(moduleId, userId)
                        showDeleteDialog = null
                    }
                ) {
                    Text("Yes, Remove", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = {
             SnackbarHost(
                 hostState = snackbarHostState,
                 snackbar = { data ->
                     Snackbar(
                         containerColor = MaterialTheme.colorScheme.primaryContainer,
                         contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                         snackbarData = data
                     )
                 }
             )
        },
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
        Box(
            modifier = Modifier
                .padding(p)
                .fillMaxSize()
                .background(KuppiGradients.PageBackground) // Background Gradient
        ) {

            if (isLoading && displayModules.isEmpty()) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else if (displayModules.isEmpty()) {
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
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
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

                    items(displayModules, key = { it.module.id }) { item ->
                        val dismissState = rememberSwipeToDismissBoxState(
                            confirmValueChange = {
                                if (it == SwipeToDismissBoxValue.EndToStart || it == SwipeToDismissBoxValue.StartToEnd) {
                                    showDeleteDialog = item.module.id to item.module.code
                                    false // Don't dismiss immediately, wait for dialog
                                } else {
                                    false
                                }
                            }
                        )

                        SwipeToDismissBox(
                            state = dismissState,
                            backgroundContent = {
                                val alignment = when (dismissState.dismissDirection) {
                                    SwipeToDismissBoxValue.StartToEnd -> Alignment.CenterStart
                                    SwipeToDismissBoxValue.EndToStart -> Alignment.CenterEnd
                                    else -> Alignment.CenterEnd
                                }
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(Color.Transparent, RoundedCornerShape(8.dp))
                                        .padding(horizontal = 20.dp),
                                    contentAlignment = alignment
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "Delete",
                                        tint = MaterialTheme.colorScheme.error
                                    )
                                }
                            },
                            content = {
                                Box(
                                    modifier = Modifier
                                        .clickable { onModuleClick(item.module.id, item.module.code) }
                                ) {
                                    ModuleCard(
                                        item = item,
                                        isAdded = true,
                                        isInDashboardScreen = true,
                                        onActionButtonClick = { }
                                    )
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}
