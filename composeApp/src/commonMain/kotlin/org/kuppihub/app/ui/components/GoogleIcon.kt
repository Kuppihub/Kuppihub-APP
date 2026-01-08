package org.kuppihub.app.ui.components

import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

@Composable
fun GoogleLogoIcon(
    modifier: Modifier = Modifier
) {
    Icon(
        imageVector = GoogleIconVector,
        contentDescription = "Google Logo",
        modifier = modifier.size(24.dp),
        tint = Color.Unspecified // Keep original colors
    )
}

private val GoogleIconVector: ImageVector
    get() = ImageVector.Builder(
        name = "GoogleIcon",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f
    ).apply {
        // Blue
        path(
            fill = SolidColor(Color(0xFF4285F4))
        ) {
            moveTo(22.56f, 12.25f)
            curveTo(22.56f, 11.47f, 22.49f, 10.72f, 22.36f, 10.0f)
            lineTo(12.0f, 10.0f)
            lineTo(12.0f, 14.26f)
            lineTo(17.92f, 14.26f)
            curveTo(17.66f, 15.63f, 16.88f, 16.79f, 15.71f, 17.57f)
            lineTo(15.71f, 20.34f)
            lineTo(19.28f, 20.34f)
            curveTo(21.36f, 18.42f, 22.56f, 15.6f, 22.56f, 12.25f)
            close()
        }
        // Green
        path(
            fill = SolidColor(Color(0xFF34A853))
        ) {
            moveTo(12.0f, 23.0f)
            curveTo(14.97f, 23.0f, 17.46f, 22.02f, 19.28f, 20.34f)
            lineTo(15.71f, 17.57f)
            curveTo(14.73f, 18.23f, 13.48f, 18.63f, 12.0f, 18.63f)
            curveTo(9.14f, 18.63f, 6.71f, 16.7f, 5.84f, 14.09f)
            lineTo(2.18f, 14.09f)
            lineTo(2.18f, 16.93f)
            curveTo(3.99f, 20.53f, 7.7f, 23.0f, 12.0f, 23.0f)
            close()
        }
        // Yellow
        path(
            fill = SolidColor(Color(0xFFFBBC05))
        ) {
            moveTo(5.84f, 14.09f)
            curveTo(5.62f, 13.43f, 5.49f, 12.73f, 5.49f, 12.0f)
            curveTo(5.49f, 11.27f, 5.62f, 10.57f, 5.84f, 9.91f)
            lineTo(5.84f, 7.07f)
            lineTo(2.18f, 7.07f)
            curveTo(1.43f, 8.55f, 1.0f, 10.22f, 1.0f, 12.0f)
            curveTo(1.0f, 13.78f, 1.43f, 15.45f, 2.18f, 16.93f)
            lineTo(5.84f, 14.09f)
            close()
        }
        // Red
        path(
            fill = SolidColor(Color(0xFFEA4335))
        ) {
            moveTo(12.0f, 1.0f)
            curveTo(13.62f, 1.0f, 15.06f, 1.56f, 16.21f, 2.64f)
            lineTo(19.36f, -0.51f) // Warning: Relative moves might be tricky, using absolute coords roughly derived
            // Re-calculating end point roughly or better yet, using standard M12 5.38 path for red top
            // Let's use standard paths from SVG to avoid artifacting
            // M12 5.38c1.62 0 3.06.56 4.21 1.64l3.15-3.15C17.45 2.09 14.97 1 12 1 7.7 1 3.99 3.47 2.18 7.07l3.66 2.84c.87-2.6 3.3-4.53 6.16-4.53z
            moveTo(12.0f, 5.38f)
            curveTo(13.62f, 5.38f, 15.06f, 5.94f, 16.21f, 7.02f)
            lineTo(19.36f, 3.87f)
            curveTo(17.45f, 2.09f, 14.97f, 1.0f, 12.0f, 1.0f)
            curveTo(7.7f, 1.0f, 3.99f, 3.47f, 2.18f, 7.07f)
            lineTo(5.84f, 9.91f)
            curveTo(6.71f, 7.31f, 9.14f, 5.38f, 12.0f, 5.38f)
            close()
        }
    }.build()
