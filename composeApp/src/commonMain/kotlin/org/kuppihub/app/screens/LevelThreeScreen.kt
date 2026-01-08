package org.kuppihub.app.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kuppihubappnew.composeapp.generated.resources.*
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.resources.getString
import org.kuppihub.app.data.KuppiRepository
import org.kuppihub.app.data.LocalDashboardRepo
import org.kuppihub.app.model.ModuleResponse
import org.kuppihub.app.ui.components.KuppiLogo
import org.kuppihub.app.ui.theme.KuppiGradients
import org.kuppihub.app.ui.theme.White

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LevelThreeScreen(facultyId: String, childId: String, semesterId: String, onBackClick: () -> Unit) {
    // 1. Setup the popup system (Snackbar) instead of Toast
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    var modules by remember { mutableStateOf<List<ModuleResponse>>(emptyList()) }
    var title by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(true) }
    var debugText by remember { mutableStateOf("") }
    var savedIds by remember { mutableStateOf<Set<Int>>(emptySet()) }
    
    val loadingText = stringResource(Res.string.loading)

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
        containerColor = MaterialTheme.colorScheme.background, // Used theme color
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
                            Icon(Icons.Default.ArrowBack, contentDescription = stringResource(Res.string.back), tint = Color.Black)
                        }
                        KuppiLogo(showText = false)
                        Spacer(Modifier.width(12.dp))
                        Text(if (title.isEmpty()) loadingText else title, style = MaterialTheme.typography.titleMedium)
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
        // Background Gradient for the content area
        Box(
            modifier = Modifier
                .padding(p)
                .fillMaxSize()
                .background(KuppiGradients.PageBackground)
        ) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else if (modules.isEmpty()) {
                Column(modifier = Modifier.align(Alignment.Center), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(stringResource(Res.string.no_modules_found))
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
                                        val addedSuccessfullyText = "Module added successfully" // Consider adding to strings.xml
                                        snackbarHostState.showSnackbar(
                                            message = addedSuccessfullyText,
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
        colors = CardDefaults.cardColors(containerColor = White),
        shape = RoundedCornerShape(16.dp) // Softer corners
    ) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Code Bubble
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
                // Video count logic
                if (item.video_count > 0) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = stringResource(Res.string.modules_count, item.video_count).replace("Modules", "Videos"), // Temp hack or add 'videos_count' to strings.xml
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

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
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(8.dp)
                    )
                } else {
                    // It is NOT added -> Show Plus Button
                    // Using FilledIconButton for better visibility as per new UI request
                    FilledIconButton(
                        onClick = onActionButtonClick,
                        colors = IconButtonDefaults.filledIconButtonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Icon(
                            imageVector = Icons.Default.AddCircle,
                            contentDescription = stringResource(Res.string.add_module),
                            tint = Color.White
                        )
                    }
                }
            }
        }
    }
}
