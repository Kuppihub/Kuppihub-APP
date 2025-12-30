package org.kuppihub.app.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// The Blue Color from your Tailwind class (text-blue-600)
val KuppiBlue = Color(0xFF2563EB)

@Composable
fun KuppiLogo(
    modifier: Modifier = Modifier,
    showText: Boolean = true
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 1. The Book Icon (Drawn from your SVG path)
        Icon(
            imageVector = BookIconVector,
            contentDescription = "Kuppi Hub Logo",
            tint = KuppiBlue,
            modifier = Modifier.size(32.dp) // Adjust size as needed
        )

        // 2. The Text (Optional)
        if (showText) {
            Spacer(modifier = Modifier.width(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Kuppi",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface // Black/Dark Gray
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Hub",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = KuppiBlue
                )
            }
        }
    }
}

// This converts your SVG path string into a Compose Vector
private val BookIconVector: ImageVector
    get() = ImageVector.Builder(
        name = "BookIcon",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f
    ).apply {
        path(
            stroke = SolidColor(Color.Black), // We override this with tint later
            strokeLineWidth = 2f,
            strokeLineCap = StrokeCap.Round,
            strokeLineJoin = StrokeJoin.Round,
            pathFillType = androidx.compose.ui.graphics.PathFillType.NonZero
        ) {
            // This is the exact path data from your SVG
            // M12 6.253v13m0-13C10.832 5.477 9.246 5 7.5 5S4.168 5.477 3 6.253v13C4.168 18.477 5.754 18 7.5 18s3.332.477 4.5 1.253m0-13C13.168 5.477 14.754 5 16.5 5c1.747 0 3.332.477 4.5 1.253v13C19.832 18.477 18.247 18 16.5 18c-1.746 0-3.332.477-4.5 1.253
            moveTo(12f, 6.253f)
            verticalLineToRelative(13f)
            moveToRelative(0f, -13f)
            curveTo(10.832f, 5.477f, 9.246f, 5f, 7.5f, 5f)
            reflectiveCurveTo(4.168f, 5.477f, 3f, 6.253f)
            verticalLineToRelative(13f)
            curveTo(4.168f, 18.477f, 5.754f, 18f, 7.5f, 18f)
            reflectiveCurveToRelative(3.332f, 0.477f, 4.5f, 1.253f)
            moveToRelative(0f, -13f)
            curveTo(13.168f, 5.477f, 14.754f, 5f, 16.5f, 5f)
            curveToRelative(1.747f, 0f, 3.332f, 0.477f, 4.5f, 1.253f)
            verticalLineToRelative(13f)
            curveTo(19.832f, 18.477f, 18.247f, 18f, 16.5f, 18f)
            curveToRelative(-1.746f, 0f, -3.332f, 0.477f, -4.5f, 1.253f)
        }
    }.build()