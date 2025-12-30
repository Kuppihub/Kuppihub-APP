package org.kuppihub.app.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.kuppihub.app.data.KuppiRepository
import org.kuppihub.app.model.KuppiResponse

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KuppiListScreen(moduleId: Int, moduleCode: String,onBackClick: () -> Unit) {
    var kuppis by remember { mutableStateOf<List<KuppiResponse>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    // Accordion State: Tracks which card ID is currently open
    var expandedId by remember { mutableStateOf<Int?>(null) }

    val uriHandler = LocalUriHandler.current

    LaunchedEffect(moduleId) {
        val result = KuppiRepository.getKuppis(moduleId)
        kuppis = result
        // Auto-expand the first video like in your React code
        if (result.isNotEmpty()) expandedId = result.first().id
        isLoading = false
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("$moduleCode Videos") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
                 },
        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f) // Light background
    ) { p ->
        Box(modifier = Modifier.padding(p).fillMaxSize()) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else if (kuppis.isEmpty()) {
                Text("No videos found.", modifier = Modifier.align(Alignment.Center))
            } else {
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

@Composable
fun KuppiExpandableCard(
    kuppi: KuppiResponse,
    isExpanded: Boolean,
    onHeaderClick: () -> Unit,
    uriHandler: androidx.compose.ui.platform.UriHandler
) {
    // Rotation Animation for the Arrow
    val rotationState by animateFloatAsState(targetValue = if (isExpanded) 180f else 0f)

    Card(
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column {
            // --- HEADER ROW (Always Visible) ---
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
                                .background(Color(0xFFDBEAFE), RoundedCornerShape(50)) // Blue-100
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "Kuppi",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(0xFF1D4ED8), // Blue-700
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Icon(
                    imageVector = Icons.Default.KeyboardArrowDown,
                    contentDescription = "Expand",
                    modifier = Modifier.rotate(rotationState),
                    tint = Color(0xFF3B82F6) // Blue-500
                )
            }

            // --- EXPANDABLE CONTENT ---
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
                    Divider(color = Color(0xFFDBEAFE), thickness = 1.dp) // Blue-100 line
                    Spacer(modifier = Modifier.height(16.dp))

                    // 1. Description
                    if (!kuppi.description.isNullOrBlank()) {
                        Text(
                            text = kuppi.description,
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Gray
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    // 2. Owner Info Row
                    if (kuppi.owner != null) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .background(Color(0xFFEFF6FF), RoundedCornerShape(8.dp)) // Blue-50
                                .padding(8.dp)
                                .fillMaxWidth()
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .background(Color(0xFFBFDBFE), CircleShape), // Blue-200
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Person, null, tint = Color(0xFF2563EB), modifier = Modifier.size(16.dp))
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = kuppi.owner.name,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium
                            )
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    // 3. Tags (Language, Date)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        kuppi.languageCode?.let { lang ->
                            TagChip(text = "Language: ${lang.uppercase()}", bgColor = Color(0xFFDBEAFE), textColor = Color(0xFF1D4ED8))
                        }
                        TagChip(text = "Uploaded: ${kuppi.createdAt.take(10)}", bgColor = Color(0xFFF3F4F6), textColor = Color(0xFF4B5563))
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // 4. ACTION BUTTONS
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {

                        // YouTube Buttons (Red)
                        kuppi.youtubeLinks?.forEachIndexed { index, url ->
                            CustomActionButton(
                                text = "Watch Video From Youtube${if ((kuppi.youtubeLinks.size) > 1) " ${index + 1}" else ""}",
                                icon = Icons.Default.PlayArrow,
                                backgroundColor = Color(0xFFEF4444), // Red-500
                                onClick = { uriHandler.openUri(url) }
                            )
                        }

                        // Telegram Buttons (Blue)
                        kuppi.telegramLinks?.forEachIndexed { index, url ->
                            CustomActionButton(
                                text = "Download Video From Telegram${if ((kuppi.telegramLinks.size) > 1) " ${index + 1}" else ""}",
                                icon = Icons.Default.Share, // Using Share icon for TG
                                backgroundColor = Color(0xFF3B82F6), // Blue-500
                                onClick = { uriHandler.openUri(url) }
                            )
                        }

                        // OneDrive Buttons (Sky)
                        kuppi.onedriveLinks?.forEachIndexed { index, url ->
                            CustomActionButton(
                                text = "OneDrive Video${if ((kuppi.onedriveLinks.size) > 1) " ${index + 1}" else ""}",
                                icon = Icons.Default.PlayArrow,
                                backgroundColor = Color(0xFF0EA5E9), // Sky-500
                                onClick = { uriHandler.openUri(url) }
                            )
                        }

                        // Google Drive Buttons (Green)
                        kuppi.gdriveLinks?.forEachIndexed { index, url ->
                            CustomActionButton(
                                text = "Google Drive Video${if ((kuppi.gdriveLinks.size) > 1) " ${index + 1}" else ""}",
                                icon = Icons.Default.PlayArrow,
                                backgroundColor = Color(0xFF22C55E), // Green-500
                                onClick = { uriHandler.openUri(url) }
                            )
                        }

                        // Materials (Gray)
                        kuppi.materialLinks?.forEachIndexed { index, url ->
                            CustomActionButton(
                                text = "Material (PDF)${if ((kuppi.materialLinks.size) > 1) " ${index + 1}" else ""}",
                                icon = Icons.Default.KeyboardArrowDown, // Placeholder icon
                                backgroundColor = Color(0xFF6B7280), // Gray-500
                                onClick = { uriHandler.openUri(url) }
                            )
                        }
                    }
                }
            }
        }
    }
}

// --- HELPER COMPONENTS ---

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
fun CustomActionButton(
    text: String,
    icon: ImageVector,
    backgroundColor: Color,
    onClick: () -> Unit
) {
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