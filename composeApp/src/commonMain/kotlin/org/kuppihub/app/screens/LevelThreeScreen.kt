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
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch // Needed for the Snackbar popup
import org.kuppihub.app.data.KuppiRepository
import org.kuppihub.app.data.LocalDashboardRepo
import org.kuppihub.app.model.ModuleResponse
import org.kuppihub.app.ui.components.KuppiLogo
import org.kuppihub.app.ui.theme.Blue50
import org.kuppihub.app.ui.theme.KuppiGradients
import org.kuppihub.app.ui.theme.White

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LevelThreeScreen(facultyId: String, childId: String, semesterId: String,onBackClick: () -> Unit) {
    // 1. Setup the popup system (Snackbar) instead of Toast
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    var modules by remember { mutableStateOf<List<ModuleResponse>>(emptyList()) }
    var title by remember { mutableStateOf("Loading...") }
    var isLoading by remember { mutableStateOf(true) }
    var debugText by remember { mutableStateOf("") }
    var savedIds by remember { mutableStateOf<Set<Int>>(emptySet()) }

    LaunchedEffect(Unit) {
        println("DEBUG: Level 3 Started for Faculty: $facultyId, Dept: $childId, Sem: $semesterId")
        val savedList = LocalDashboardRepo.getSavedModules()
        savedIds = savedList.map { it.module.id }.toSet()

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
        containerColor = Blue50,
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(KuppiGradients.MainHeader)
            ) {
                // TIGHT TOOLBAR
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                        .padding(horizontal = 4.dp), // Less padding for navigation icon
                    contentAlignment = Alignment.CenterStart
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = onBackClick) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.Black)
                        }
                        KuppiLogo(showText = false)
                        Spacer(Modifier.width(12.dp))
                        Text(title, style = MaterialTheme.typography.titleMedium)
                    }
                }
            }
        },
        // 2. Add the Snackbar Host here so the popup can appear
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
        }
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
                        val isAlreadySaved = savedIds.contains(item.module.id)
                        ModuleCard(
                            item = item,
                            isAdded = isAlreadySaved, // Pass the status
                            isInDashboardScreen = false, // We are in Level 3
                            onActionButtonClick = {
                                if (!isAlreadySaved) {
                                    LocalDashboardRepo.addModule(item)

                                    // 4. Update the state immediately so the UI changes to "Added"
                                    savedIds = savedIds + item.module.id

                                    scope.launch {
                                        snackbarHostState.showSnackbar(
                                            message = "Module added successfully",
                                            withDismissAction = true
                                        )
                                    }
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable

fun ModuleCard(
    item: ModuleResponse,
    onActionButtonClick: () -> Unit = {},
    isAdded: Boolean = false, // True if the module is currently in the database
    isInDashboardScreen: Boolean = false // True ONLY if we are on the 'My Dashboard' tab
) {
    Card(
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = White)
    ) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // ... (Code Bubble & Name Column remain exactly the same) ...
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

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.module.name,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
                // ... Video count logic can stay here ...
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

            // --- CHANGED LOGIC HERE ---
            if (isInDashboardScreen) {
                // Case A: We are in Dashboard -> Show Delete Button
                // REMOVED as per user request (Swipe to delete implemented in DashboardScreen)
            } else {
                // Case B: We are in Level 3
                if (isAdded) {
                    // It is already added -> Show "Added" Text
                    Text(
                        text = "Added",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary, // Green-ish usually
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(8.dp)
                    )
                } else {
                    // It is NOT added -> Show Plus Button
                    IconButton(onClick = onActionButtonClick) {
                        Icon(
                            imageVector = Icons.Default.AddCircle,
                            contentDescription = "Add",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }
}
