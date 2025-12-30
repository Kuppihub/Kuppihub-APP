package org.kuppihub.app.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch // Needed for the Snackbar popup
import org.kuppihub.app.data.KuppiRepository
import org.kuppihub.app.data.LocalDashboardRepo
import org.kuppihub.app.model.ModuleResponse

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LevelThreeScreen(facultyId: String, childId: String, semesterId: String) {
    // 1. Setup the popup system (Snackbar) instead of Toast
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    var modules by remember { mutableStateOf<List<ModuleResponse>>(emptyList()) }
    var title by remember { mutableStateOf("Loading...") }
    var isLoading by remember { mutableStateOf(true) }
    var debugText by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        println("DEBUG: Level 3 Started for Faculty: $facultyId, Dept: $childId, Sem: $semesterId")

        val semester = KuppiRepository.getSemester(facultyId, childId, semesterId)

        if (semester == null) {
            debugText = "Error: Semester data not found in cache."
            isLoading = false
            return@LaunchedEffect
        }

        title = semester.name
        val moduleIds = semester.modules

        if (moduleIds.isEmpty()) {
            debugText = "No modules listed for this semester."
        } else {
            try {
                val result = KuppiRepository.getModulesByIds(moduleIds)
                if (result.isEmpty()) debugText = "API returned 0 modules."
                modules = result
            } catch (e: Exception) {
                e.printStackTrace()
                debugText = "API Error: ${e.message}"
            }
        }
        isLoading = false
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text(title) }) },
        // 2. Add the Snackbar Host here so the popup can appear
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { p ->
        Box(modifier = Modifier.padding(p).fillMaxSize()) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else if (modules.isEmpty()) {
                Column(modifier = Modifier.align(Alignment.Center), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("No modules found.")
                    Spacer(Modifier.height(8.dp))
                    Text(debugText, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
                }
            } else {

                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(modules) { item ->
                        ModuleCard(
                            item = item,
                            onActionButtonClick = {
                                // 3. Save to Database (No Context needed now)
                                LocalDashboardRepo.addModule(item)

                                // 4. Show "Added" message using Snackbar
                                scope.launch {
                                    snackbarHostState.showSnackbar("Added ${item.module.code} to Dashboard!")
                                }
                            },
                            isAdded = false
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ModuleCard(item: ModuleResponse, onActionButtonClick: () -> Unit = {}, isAdded: Boolean = false) {
    Card(
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left Side: Module Code Bubble
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.primaryContainer)
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Text(
                    text = item.module.code,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Center: Name and Video Count
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.module.name,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )

                if (item.video_count > 0) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = "Videos",
                            modifier = Modifier.size(14.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${item.video_count} Videos",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            // Right Side: Add/Remove Button
            IconButton(onClick = onActionButtonClick) {
                Icon(
                    imageVector = if (isAdded) Icons.Default.Delete else Icons.Default.AddCircle,
                    contentDescription = if (isAdded) "Remove" else "Add",
                    tint = if (isAdded) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}