package org.kuppihub.app.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.kamel.image.KamelImage
import io.kamel.image.asyncPainterResource
import kuppihubappnew.composeapp.generated.resources.Res
import kuppihubappnew.composeapp.generated.resources.back
import kuppihubappnew.composeapp.generated.resources.kuppi_count
import org.jetbrains.compose.resources.stringResource
import org.kuppihub.app.model.Tutor
import org.kuppihub.app.ui.components.KuppiLogo
import org.kuppihub.app.ui.theme.KuppiGradients
import org.kuppihub.app.ui.theme.White
import org.kuppihub.app.viewmodel.TutorsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TutorsScreen(onBackClick: () -> Unit) {
    val viewModel = remember { TutorsViewModel() }
    val tutors by viewModel.tutors.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

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
                        .height(54.dp)
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
                        Text("Hall of Fame", style = MaterialTheme.typography.titleMedium)
                    }
                }
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .background(KuppiGradients.PageBackground)
        ) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else {
                Column(modifier = Modifier.fillMaxSize()) {
                    // Header Text
                    Text(
                        text = "Students Who Did Kuppi 💙",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        textAlign = TextAlign.Center
                    )

                    // Grid
                    LazyVerticalGrid(
                        columns = GridCells.Adaptive(minSize = 160.dp), // Responsive Grid
                        contentPadding = PaddingValues(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(tutors) { tutor ->
                            TutorCard(tutor)
                        }
                    }
                }
            }
        }
    }
}


@Composable
fun TutorCard(tutor: Tutor) {
    val uriHandler = LocalUriHandler.current

    Card(
        colors = CardDefaults.cardColors(containerColor = White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 1. Profile Image
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .border(2.dp, MaterialTheme.colorScheme.primaryContainer, CircleShape)
                    .background(Color.LightGray)
            ) {
                if (tutor.imageUrl != null) {
                    // ✅ KAMEL IMPLEMENTATION
                    KamelImage(
                        resource = asyncPainterResource(data = tutor.imageUrl),
                        contentDescription = tutor.name,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize(),
                        onLoading = { progress -> CircularProgressIndicator(progress) },
                        onFailure = {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = null,
                                tint = Color.Gray,
                                modifier = Modifier.padding(20.dp)
                            )
                        }
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        modifier = Modifier.align(Alignment.Center).size(40.dp),
                        tint = Color.Gray
                    )
                }
            }

            // ... (Rest of your code for Name, Count, LinkedIn remains exactly the same) ...
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = tutor.name, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center, maxLines = 2, overflow = TextOverflow.Ellipsis)
            Text(text = stringResource(Res.string.kuppi_count, tutor.videoCount), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(vertical = 4.dp))

            if (tutor.modulesDone.isNotEmpty()) {
                Text(
                    text = tutor.modulesDone.take(2).joinToString(", "),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    fontSize = 10.sp,
                    lineHeight = 12.sp
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (!tutor.linkedinUrl.isNullOrBlank()) {
                Button(
                    onClick = {
                        val url = if (tutor.linkedinUrl?.startsWith("http") == true) tutor.linkedinUrl else "https://${tutor.linkedinUrl}"
                        try { uriHandler.openUri(url) } catch (e: Exception) { e.printStackTrace() }
                    },
                    modifier = Modifier.height(32.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0077B5)),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 0.dp),
                    shape = RoundedCornerShape(50)
                ) {
                    Text("LinkedIn", fontSize = 10.sp)
                }
            }
        }
    }
}
