package org.kuppihub.app.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.kuppihub.app.data.KuppiRepository
import org.kuppihub.app.model.KuppiResponse

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KuppiListScreen(moduleId: Int, moduleCode: String) {
    var kuppis by remember { mutableStateOf<List<KuppiResponse>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    // Helper to open links in Browser/YouTube App
    val uriHandler = LocalUriHandler.current

    LaunchedEffect(moduleId) {
        kuppis = KuppiRepository.getKuppis(moduleId)
        isLoading = false
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("$moduleCode Videos") }) }
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
                        KuppiCard(kuppi, uriHandler)
                    }
                }
            }
        }
    }
}

@Composable
fun KuppiCard(kuppi: KuppiResponse, uriHandler: androidx.compose.ui.platform.UriHandler) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // 1. Title
            Text(
                text = kuppi.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            // 2. Owner & Date
            Spacer(modifier = Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "${kuppi.owner?.name ?: "Unknown"} • ${kuppi.createdAt.take(10)}",
                    style = MaterialTheme.typography.labelSmall
                )
            }

            // 3. Description (Optional)
            if (!kuppi.description.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = kuppi.description, style = MaterialTheme.typography.bodySmall, maxLines = 2)
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 4. Action Buttons (YouTube, Telegram, Materials)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                // YouTube Button
                kuppi.youtubeLinks?.firstOrNull()?.let { url ->
                    Button(
                        onClick = { uriHandler.openUri(url) },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF0000)),
                        contentPadding = PaddingValues(horizontal = 12.dp)
                    ) {
                        Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Watch")
                    }
                }

                // Telegram Button
                kuppi.telegramLinks?.firstOrNull()?.let { url ->
                    OutlinedButton(onClick = { uriHandler.openUri(url) }) {
                        Text("Telegram")
                    }
                }

                // Materials Button
                kuppi.materialLinks?.firstOrNull()?.let { url ->
                    OutlinedButton(onClick = { uriHandler.openUri(url) }) {
                        Text("PDF")
                    }
                }
            }
        }
    }
}