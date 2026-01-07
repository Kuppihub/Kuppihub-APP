package org.kuppihub.app.utils

import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext

@Composable
actual fun rememberShareLauncher(): () -> Unit {
    val context = LocalContext.current
    return remember(context) {
        {
            val sendIntent: Intent = Intent().apply {
                action = Intent.ACTION_SEND
                putExtra(Intent.EXTRA_TEXT, "Check out KuppiHub! The best place for UOM academic resources: https://kuppihub.org") // Replace with actual link
                type = "text/plain"
            }
            val shareIntent = Intent.createChooser(sendIntent, "Share KuppiHub via")
            context.startActivity(shareIntent)
        }
    }
}