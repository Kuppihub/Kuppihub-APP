package org.kuppihub.app.screens.addkuppi

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import kotlinx.coroutines.launch
import org.kuppihub.app.ui.theme.KuppiGradients
import org.kuppihub.app.viewmodel.AddKuppiViewModel
// For opening URIs
import androidx.compose.ui.platform.LocalUriHandler


// KMP Firebase Imports
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.auth.auth

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddKuppiScreen(
    moduleId: Int,
    userId: String,
    onBackClick: () -> Unit
) {
    val viewModel = remember { AddKuppiViewModel() }
    val scrollState = rememberScrollState()
    val scope = rememberCoroutineScope()
    val uriHandler = LocalUriHandler.current


    // ViewModel State
    val title by viewModel.title.collectAsState()
    val description by viewModel.description.collectAsState()
    val languageCode by viewModel.languageCode.collectAsState()
    val indexNo by viewModel.indexNo.collectAsState()
    val isKuppi by viewModel.isKuppi.collectAsState()
    
    val youtubeLinks by viewModel.youtubeLinks.collectAsState()
    val telegramLinks by viewModel.telegramLinks.collectAsState()
    val gdriveLinks by viewModel.gdriveLinks.collectAsState()
    val onedriveLinks by viewModel.onedriveLinks.collectAsState()
    val materialLinks by viewModel.materialLinks.collectAsState()
    
    val hasRestriction by viewModel.hasRestriction.collectAsState()
    val allowedDomains by viewModel.allowedDomains.collectAsState()

    val isSubmitting by viewModel.isSubmitting.collectAsState()
    val success by viewModel.submissionSuccess.collectAsState()
    val errorMsg by viewModel.errorMessage.collectAsState()

    // Search State
    val searchQuery by viewModel.searchQuery.collectAsState()
    val searchResults by viewModel.searchResults.collectAsState()
    val selectedModule by viewModel.selectedModule.collectAsState()

    var showSuccessDialog by remember { mutableStateOf(false) }

    LaunchedEffect(success) {
        if (success == true) {
            showSuccessDialog = true
        }
    }

    if (showSuccessDialog) {
        AlertDialog(
            onDismissRequest = { 
                showSuccessDialog = false 
                onBackClick() 
            },
            icon = { 
                Icon(
                    Icons.Default.CheckCircle, 
                    contentDescription = null, 
                    tint = Color(0xFF4CAF50), // Green
                    modifier = Modifier.size(48.dp)
                ) 
            },
            title = { Text("Added Successfully!", style = MaterialTheme.typography.headlineSmall) },
            text = { Text("Your kuppi has been submitted and will be reviewed soon.", style = MaterialTheme.typography.bodyMedium) },
            confirmButton = {
                Button(
                    onClick = {
                        showSuccessDialog = false
                        onBackClick()
                    }
                ) {
                    Text("Done")
                }
            },
            containerColor = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        )
    }

    Scaffold(
        topBar = {
            Column(modifier = Modifier.background(KuppiGradients.MainHeader)) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                        .padding(horizontal = 4.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = onBackClick) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.Black)
                        }
                        Spacer(Modifier.width(8.dp))
                        Text("Add New Kuppi", style = MaterialTheme.typography.titleMedium, color = Color.Black)
                    }
                }
            }
        },
        containerColor = MaterialTheme.colorScheme.background // Theme color
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .background(KuppiGradients.PageBackground) // Background Gradient
                .verticalScroll(scrollState)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Updated Info Card matching user's design
            InfoCard(onTelegramClick = { uriHandler.openUri("https://t.me/KuppihubBot") })

            // SEARCH MODULE FIELD (Only if ID was not passed via Nav)
            if (moduleId == -1) {
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { viewModel.onSearchQueryChange(it) },
                        label = { Text("Search Module (e.g. CS10)") },
                        leadingIcon = { Icon(Icons.Default.Search, null) },
                        trailingIcon = if (selectedModule != null) {
                            { Icon(Icons.Default.Check, null, tint = Color(0xFF4CAF50)) }
                        } else null,
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White
                        )
                    )

                    // Results Dropdown
                    if (searchResults.isNotEmpty()) {
                        ElevatedCard(
                            modifier = Modifier
                                .padding(top = 60.dp)
                                .fillMaxWidth()
                                .heightIn(max = 200.dp)
                                .zIndex(1f),
                            elevation = CardDefaults.elevatedCardElevation(defaultElevation = 6.dp)
                        ) {
                            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                                searchResults.forEach { module ->
                                    ListItem(
                                        headlineContent = { Text(module.code) },
                                        supportingContent = { Text(module.name) },
                                        modifier = Modifier.clickable { viewModel.selectModule(module) }
                                    )
                                    HorizontalDivider()
                                }
                            }
                        }
                    }
                }
            }

            // Basic Details Section
            Card(
                elevation = CardDefaults.cardElevation(2.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Basic Details", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
                    
                    OutlinedTextField(
                        value = title,
                        onValueChange = { viewModel.title.value = it },
                        label = { Text("Title") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )

                    OutlinedTextField(
                        value = description,
                        onValueChange = { viewModel.description.value = it },
                        label = { Text("Description") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3,
                        shape = RoundedCornerShape(12.dp)
                    )
                    
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedTextField(
                            value = indexNo,
                            onValueChange = { viewModel.indexNo.value = it },
                            label = { Text("Index No (Optional)") },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp)
                        )
                    }

                    // Content Type (Kuppi vs Material) - Radio Buttons
                    Column {
                        Text("Content Type", style = MaterialTheme.typography.labelLarge, color = Color(0xFF374151))
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(
                                selected = isKuppi,
                                onClick = { viewModel.isKuppi.value = true }
                            )
                            Text("Kuppi Video", style = MaterialTheme.typography.bodyMedium, modifier = Modifier.clickable { viewModel.isKuppi.value = true })
                            
                            Spacer(modifier = Modifier.width(16.dp))
                            
                            RadioButton(
                                selected = !isKuppi,
                                onClick = { viewModel.isKuppi.value = false }
                            )
                            Text("Notes / Papers / Materials", style = MaterialTheme.typography.bodyMedium, modifier = Modifier.clickable { viewModel.isKuppi.value = false })
                        }
                    }
                    
                    // Language
                     Column {
                        Text("Language", style = MaterialTheme.typography.labelLarge)
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            val languages = listOf("si" to "Sinhala", "en" to "English", "ta" to "Tamil")
                            languages.forEach { (code, label) ->
                                FilterChip(
                                    selected = languageCode == code,
                                    onClick = { viewModel.languageCode.value = code },
                                    label = { Text(label) },
                                    leadingIcon = if (languageCode == code) { { Icon(Icons.Default.Check, null) } } else null
                                )
                            }
                        }
                    }
                }
            }
            
            // Domain Restrictions
             Card(
                elevation = CardDefaults.cardElevation(2.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                 shape = RoundedCornerShape(16.dp)
            ) {
                 Column(modifier = Modifier.padding(16.dp)) {
                     Row(verticalAlignment = Alignment.CenterVertically) {
                         Icon(Icons.Default.Lock, null, tint = MaterialTheme.colorScheme.primary)
                         Spacer(modifier = Modifier.width(8.dp))
                         Text("Access Restriction", style = MaterialTheme.typography.titleMedium)
                         Spacer(modifier = Modifier.weight(1f))
                         Switch(checked = hasRestriction, onCheckedChange = { viewModel.hasRestriction.value = it })
                     }
                     
                     if (hasRestriction) {
                         Spacer(modifier = Modifier.height(12.dp))
                         Text("Allowed Domains:", style = MaterialTheme.typography.labelMedium)
                         Spacer(modifier = Modifier.height(8.dp))
                         
                             // Using the domain options provided by user
                             val domains = listOf(
                                 "@uom.lk" to "University of Moratuwa (@uom.lk)",
                                 "@cse.mrt.ac.lk" to "CSE Department (@cse.mrt.ac.lk)",
                                 "@gmail.com" to "Gmail Users (@gmail.com)"
                             )
                             // We use Column for better readability instead of squeezing into one Row
                             Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                 domains.forEach { (value, label) ->
                                     FilterChip(
                                         selected = allowedDomains.contains(value),
                                         onClick = { viewModel.toggleDomain(value) },
                                         label = { Text(label) },
                                         leadingIcon = if (allowedDomains.contains(value)) { { Icon(Icons.Default.Check, null) } } else null
                                     )
                                 }
                             }
                         
                     }
                 }
            }

            HorizontalDivider()
            Text("Video & Material Links", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text("Add at least one link.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline)

            // Links Sections - Using the configuration from user snippet to style them
            
            // YouTube
            LinkSectionCard(
                title = "YouTube Links", 
                icon = Icons.Default.SmartDisplay, // Closest to YouTubeIcon
                color = Color(0xFFDC2626),
                bgColor = Color(0xFFFEF2F2),
                borderColor = Color(0xFFFECACA),
                placeholder = "https://www.youtube.com/watch?v=...",
                links = youtubeLinks, 
                onAdd = { viewModel.addLink(viewModel.youtubeLinks) }, 
                onRemove = { viewModel.removeLink(viewModel.youtubeLinks, it) }, 
                onUpdate = { i, s -> viewModel.updateLink(viewModel.youtubeLinks, i, s) }
            )
            
            // Telegram
            LinkSectionCard(
                title = "Telegram Links", 
                icon = Icons.AutoMirrored.Filled.Send, // Closest to TelegramIcon
                color = Color(0xFF0088CC),
                bgColor = Color(0xFFEFF6FF),
                borderColor = Color(0xFFBFDBFE),
                placeholder = "https://t.me/...",
                links = telegramLinks, 
                onAdd = { viewModel.addLink(viewModel.telegramLinks) }, 
                onRemove = { viewModel.removeLink(viewModel.telegramLinks, it) }, 
                onUpdate = { i, s -> viewModel.updateLink(viewModel.telegramLinks, i, s) }
            )

            // GDrive
             LinkSectionCard(
                title = "Google Drive Links", 
                icon = Icons.Default.CloudQueue, // CloudIcon
                color = Color(0xFF16A34A),
                bgColor = Color(0xFFF0FDF4),
                borderColor = Color(0xFFBBF7D0),
                placeholder = "https://drive.google.com/...",
                links = gdriveLinks, 
                onAdd = { viewModel.addLink(viewModel.gdriveLinks) }, 
                onRemove = { viewModel.removeLink(viewModel.gdriveLinks, it) }, 
                onUpdate = { i, s -> viewModel.updateLink(viewModel.gdriveLinks, i, s) }
            )

            // OneDrive
             LinkSectionCard(
                title = "OneDrive Links", 
                icon = Icons.Default.CloudCircle, // CloudIcon variant
                color = Color(0xFF0078D4),
                bgColor = Color(0xFFF0F9FF),
                borderColor = Color(0xFFBAE6FD),
                placeholder = "https://onedrive.live.com/...",
                links = onedriveLinks, 
                onAdd = { viewModel.addLink(viewModel.onedriveLinks) }, 
                onRemove = { viewModel.removeLink(viewModel.onedriveLinks, it) }, 
                onUpdate = { i, s -> viewModel.updateLink(viewModel.onedriveLinks, i, s) }
            )
            
            // Materials
             LinkSectionCard(
                title = "Material Links (PDF, Docs)", 
                icon = Icons.Default.Folder, // FolderIcon
                color = Color(0xFF6B7280),
                bgColor = Color(0xFFF9FAFB),
                borderColor = Color(0xFFE5E7EB),
                placeholder = "Direct link to PDF or document",
                links = materialLinks, 
                onAdd = { viewModel.addLink(viewModel.materialLinks) }, 
                onRemove = { viewModel.removeLink(viewModel.materialLinks, it) }, 
                onUpdate = { i, s -> viewModel.updateLink(viewModel.materialLinks, i, s) }
            )

            if (errorMsg != null) {
                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)) {
                    Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Error, null, tint = MaterialTheme.colorScheme.error)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(errorMsg!!, color = MaterialTheme.colorScheme.onErrorContainer)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Submit Button
            Button(
                onClick = {
                    scope.launch {
                        var tokenToUse = userId
                        try {
                            val currentUser = Firebase.auth.currentUser
                            if (currentUser != null) {
                                val freshToken = currentUser.getIdToken(false)
                                if (freshToken != null) tokenToUse = freshToken
                            }
                        } catch (_: Exception) { }
                        viewModel.submitKuppi(moduleId, tokenToUse)
                    }
                },
                enabled = !isSubmitting && (moduleId != -1 || selectedModule != null),
                modifier = Modifier.fillMaxWidth().height(50.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                if (isSubmitting) CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp)) 
                else {
                    Icon(Icons.Default.Add, null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Submit Kuppi")
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun InfoCard(onTelegramClick: () -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFFE5F6FD)), // Light Blue
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        // FIXED: Removed crossAxisAlignment which does not exist. Changed to verticalAlignment.
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.Top) {
            Icon(Icons.Default.Info, null, tint = Color(0xFF0288D1))
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text("How to upload your Kuppi:", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Medium)
                Spacer(modifier = Modifier.height(4.dp))
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    // Make Telegram bot clickable and styled
                    Row {
                         Text("1. Upload your video/materials to ", style = MaterialTheme.typography.bodySmall)
                         Text(
                             "@KuppihubBot", 
                             style = MaterialTheme.typography.bodySmall.copy(
                                 color = Color(0xFF0288D1), 
                                 textDecoration = TextDecoration.Underline,
                                 fontWeight = FontWeight.Bold
                             ),
                             modifier = Modifier.clickable { onTelegramClick() }
                         )
                         Text(" on Telegram", style = MaterialTheme.typography.bodySmall)
                    }
                    Text("2. Or upload to YouTube, Google Drive, or OneDrive", style = MaterialTheme.typography.bodySmall)
                    Text("3. Copy the share links and paste them below", style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}

@Composable
fun LinkSectionCard(
    title: String,
    icon: ImageVector,
    color: Color,
    bgColor: Color,
    borderColor: Color,
    placeholder: String,
    links: List<String>,
    onAdd: () -> Unit,
    onRemove: (Int) -> Unit,
    onUpdate: (Int, String) -> Unit
) {
    // We construct a custom container to match the design from user
    Surface(
        color = bgColor,
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, borderColor),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
            ) {
                Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(22.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Medium)
                Spacer(modifier = Modifier.weight(1f))
                
                TextButton(
                    onClick = onAdd,
                    colors = ButtonDefaults.textButtonColors(contentColor = color),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                    modifier = Modifier.height(32.dp)
                ) {
                    Icon(Icons.Default.Add, null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Add", style = MaterialTheme.typography.labelMedium)
                }
            }

            // List
            if (links.isEmpty()) {
                Text(
                    "No links added yet. Click \"Add\" to add one.",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray,
                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            } else {
                links.forEachIndexed { index, url ->
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "${index + 1}.",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray,
                            modifier = Modifier.width(24.dp)
                        )
                        
                        OutlinedTextField(
                            value = url,
                            onValueChange = { onUpdate(index, it) },
                            modifier = Modifier.weight(1f),
                            placeholder = { Text(placeholder, style = MaterialTheme.typography.bodySmall, color = Color.Gray) },
                            singleLine = true,
                            textStyle = MaterialTheme.typography.bodyMedium,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = Color.White,
                                unfocusedContainerColor = Color.White,
                                focusedBorderColor = borderColor,
                                unfocusedBorderColor = borderColor
                            ),
                            shape = RoundedCornerShape(8.dp)
                        )
                        
                        IconButton(
                            onClick = { onRemove(index) },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(Icons.Default.Delete, "Remove", tint = Color(0xFFEF4444), modifier = Modifier.size(18.dp))
                        }
                    }
                }
            }
        }
    }
}
