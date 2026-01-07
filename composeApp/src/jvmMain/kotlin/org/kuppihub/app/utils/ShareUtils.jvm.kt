package org.kuppihub.app.utils

import androidx.compose.runtime.Composable
import java.awt.Desktop
import java.net.URI

@Composable
actual fun rememberShareLauncher(): () -> Unit {
    return {
        try {
            // JVM fallback: Try to open the default mail client or browser if possible
            // Since "share" is a mobile concept, on desktop we might just copy to clipboard
            // or open a mailto link. Let's try opening a mailto link as a simple "Share"
            val desktop = Desktop.getDesktop()
            if (desktop.isSupported(Desktop.Action.BROWSE)) {
                // Just open the website for them to copy the link manually
                desktop.browse(URI("https://kuppihub.org"))
            } else if (desktop.isSupported(Desktop.Action.MAIL)) {
                desktop.mail(URI("mailto:?subject=Check out KuppiHub&body=Check out KuppiHub at https://kuppihub.org"))
            }
        } catch (e: Exception) {
            println("Share failed on JVM: ${e.message}")
        }
    }
}