package org.kuppihub.app.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kuppihubappnew.composeapp.generated.resources.*
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.resources.getString
import org.kuppihub.app.data.KuppiRepository
import org.kuppihub.app.data.LocalDashboardRepo
import org.kuppihub.app.model.KuppiResponse
import org.kuppihub.app.ui.components.KuppiLogo
import org.kuppihub.app.ui.theme.KuppiGradients
import org.kuppihub.app.ui.theme.White

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KuppiListScreen(moduleId: Int, moduleCode: String, onBackClick: () -> Unit) {
    // 1. STATE: Load cached data immediately for instant UI
    var kuppis by remember { mutableStateOf(LocalDashboardRepo.getCachedKuppis(moduleId)) }
    var isLoading by remember { mutableStateOf(kuppis.isEmpty()) }
    var isOffline by remember { mutableStateOf(false) }

    // Accordion State: Tracks which card ID is currently open
    var expandedId by remember { mutableStateOf<Int?>(null) }

    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val uriHandler = LocalUriHandler.current

    // 2. DATA FETCHING: Try to get fresh data from network
    LaunchedEffect(moduleId) {
        try {
            val freshData = KuppiRepository.getKuppis(moduleId)

            // 👇 FIX: Only update if we actually got data!
            if (freshData.isNotEmpty()) {
                kuppis = freshData
                LocalDashboardRepo.saveKuppis(moduleId, freshData)
                isOffline = false
            } else {
                // If freshData is empty, it might be a network error that was "swallowed".
                // So we do NOTHING. We keep showing the cached 'kuppis'.
                println("DEBUG: Fresh data was empty, keeping cached data.")
            }

        } catch (e: Exception) {
            e.printStackTrace()
            isOffline = true
            // If we have cached data, tell the user
            if (kuppis.isNotEmpty()) {
                scope.launch { 
                    val offlineMsg = getString(Res.string.offline_mode_message)
                    snackbarHostState.showSnackbar(offlineMsg) 
                }
            }
        } finally {
            isLoading = false
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
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
                        .height(48.dp)
                        .padding(horizontal = 4.dp), // Less padding for navigation icon
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
                        Column {
                            Text("$moduleCode Videos", style = MaterialTheme.typography.titleMedium)
                            if (isOffline) {
                                Text(stringResource(Res.string.offline_mode_message), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.error)
                            }
                        }
                    }
                    
                    if (isOffline) {
                         Box(modifier = Modifier.align(Alignment.CenterEnd).padding(end = 16.dp)) {
                             Icon(Icons.Default.WifiOff, "Offline", tint = MaterialTheme.colorScheme.error)
                         }
                    }
                }
            }
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { p ->
        // Background Gradient
        Box(
            modifier = Modifier
                .padding(p)
                .fillMaxSize()
                .background(KuppiGradients.PageBackground)
        ) {

            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }
            else if (kuppis.isEmpty()) {
                Column(modifier = Modifier.align(Alignment.Center), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(stringResource(Res.string.no_kuppis_found))
                    if(isOffline) Text(stringResource(Res.string.search_failed_message), style = MaterialTheme.typography.bodySmall)
                }
            }
            else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(kuppis) { kuppi ->
                        KuppiExpandableCard(
                            kuppi = kuppi,
                            isExpanded = expandedId == kuppi.id,
                            onHeaderClick = {
                                // Toggle logic: Close if open, Open if closed
                                expandedId = if (expandedId == kuppi.id) null else kuppi.id
                            },
                            uriHandler = uriHandler
                        )
                    }
                }
            }
        }
    }
}

// --- YOUR UI COMPONENTS (Kept exactly as you designed them) ---

@Composable
fun KuppiExpandableCard(
    kuppi: KuppiResponse,
    isExpanded: Boolean,
    onHeaderClick: () -> Unit,
    uriHandler: androidx.compose.ui.platform.UriHandler
) {
    val rotationState by animateFloatAsState(targetValue = if (isExpanded) 180f else 0f)

    Card(
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp), // Slightly lower elevation is cleaner
        colors = CardDefaults.cardColors(containerColor = White), // White for card bg
        modifier = Modifier.fillMaxWidth()
    ) {
        Column {
            // HEADER
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onHeaderClick() }
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Text(
                        text = kuppi.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    if (kuppi.isKuppi) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Box(
                            modifier = Modifier
                                .background(Color(0xFFDBEAFE), RoundedCornerShape(50))
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text("Kuppi", style = MaterialTheme.typography.labelSmall, color = Color(0xFF1D4ED8), fontWeight = FontWeight.Bold)
                        }
                    }
                }
                Icon(
                    imageVector = Icons.Default.KeyboardArrowDown,
                    contentDescription = "Expand",
                    modifier = Modifier.rotate(rotationState),
                    tint = Color(0xFF3B82F6)
                )
            }

            // EXPANDABLE CONTENT
            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp, end = 16.dp, bottom = 16.dp)
                ) {
                    HorizontalDivider(color = Color(0xFFDBEAFE), thickness = 1.dp)
                    Spacer(modifier = Modifier.height(16.dp))

                    if (!kuppi.description.isNullOrBlank()) {
                        Text(kuppi.description, style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    if (kuppi.owner != null) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .background(Color(0xFFEFF6FF), RoundedCornerShape(8.dp))
                                .padding(8.dp)
                                .fillMaxWidth()
                        ) {
                            Box(
                                modifier = Modifier.size(32.dp).background(Color(0xFFBFDBFE), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Person, null, tint = Color(0xFF2563EB), modifier = Modifier.size(16.dp))
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(kuppi.owner.name, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    // TAGS
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        kuppi.languageCode?.let { lang ->
                            TagChip(text = "Lang: ${lang.uppercase()}", bgColor = Color(0xFFDBEAFE), textColor = Color(0xFF1D4ED8))
                        }
                        // Helper to safely format date if string is long
                        val dateText = if(kuppi.createdAt.length >= 10) kuppi.createdAt.take(10) else kuppi.createdAt
                        TagChip(text = dateText, bgColor = Color(0xFFF3F4F6), textColor = Color(0xFF4B5563))
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // BUTTONS
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        kuppi.youtubeLinks?.forEachIndexed { index, url ->
                            CustomActionButton("Watch on YouTube ${index + 1}", Icons.Default.PlayArrow, Color(0xFFEF4444)) { uriHandler.openUri(url) }
                        }
                        kuppi.telegramLinks?.forEachIndexed { index, url ->
                            CustomActionButton("Download from Telegram ${index + 1}", Icons.Default.Share, Color(0xFF3B82F6)) { uriHandler.openUri(url) }
                        }
                        kuppi.onedriveLinks?.forEachIndexed { index, url ->
                            CustomActionButton("OneDrive Video ${index + 1}", Icons.Default.PlayArrow, Color(0xFF0EA5E9)) { uriHandler.openUri(url) }
                        }
                        kuppi.gdriveLinks?.forEachIndexed { index, url ->
                            CustomActionButton("Google Drive Video ${index + 1}", Icons.Default.PlayArrow, Color(0xFF22C55E)) { uriHandler.openUri(url) }
                        }
                        kuppi.materialLinks?.forEachIndexed { index, url ->
                            CustomActionButton("Course Material (PDF) ${index + 1}", Icons.Default.Menu, Color(0xFF6B7280)) { uriHandler.openUri(url) }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TagChip(text: String, bgColor: Color, textColor: Color) {
    Box(
        modifier = Modifier
            .background(bgColor, RoundedCornerShape(6.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(text = text, style = MaterialTheme.typography.labelSmall, color = textColor)
    }
}

@Composable
fun CustomActionButton(text: String, icon: ImageVector, backgroundColor: Color, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(containerColor = backgroundColor),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth().height(48.dp),
        contentPadding = PaddingValues(horizontal = 16.dp)
    ) {
        Icon(imageVector = icon, contentDescription = null, modifier = Modifier.size(20.dp), tint = Color.White)
        Spacer(modifier = Modifier.width(8.dp))
        Text(text = text, color = Color.White, style = MaterialTheme.typography.labelLarge)
    }
}
