package org.kuppihub.app.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

object KuppiGradients {

    // "Header gradient (left to right)"
    // from-blue-100 via-purple-200 to-blue-500
    val MainHeader = Brush.horizontalGradient(
        colors = listOf(Blue100, Purple200, Blue500)
    )

    // "Page background gradient (bottom-right diagonal)"
    // from-blue-50 to-indigo-100
    val PageBackground = Brush.linearGradient(
        colors = listOf(Blue50, Indigo100)
    )

    // "Button gradients"
    // blue-500 to indigo-500
    val PrimaryButton = Brush.horizontalGradient(
        colors = listOf(Blue500, Indigo500)
    )

    // Hover state: blue-600 to indigo-600
    val PrimaryButtonHover = Brush.horizontalGradient(
        colors = listOf(Blue600, Indigo600)
    )

    // "Text gradient (heading accents)"
    val TextGradient = Brush.horizontalGradient(
        colors = listOf(Blue600, Indigo600)
    )
}