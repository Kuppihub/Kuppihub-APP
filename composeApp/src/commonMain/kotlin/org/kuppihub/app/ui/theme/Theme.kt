package org.kuppihub.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColors = lightColorScheme(
    primary = Blue500,
    onPrimary = White,
    primaryContainer = Blue100,
    onPrimaryContainer = Blue700,

    secondary = Indigo500,
    onSecondary = White,
    secondaryContainer = Indigo100,
    onSecondaryContainer = Indigo600,

    background = Blue50, // <-- Setting your "normal background" color here
    onBackground = Blue700, // Text on background

    surface = White,
    onSurface = Blue700, // Text on cards
    surfaceVariant = Blue50, // Slightly darker backgrounds

    error = DangerRed
)

@Composable
fun KuppiTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = LightColors,
        content = content
    )
}