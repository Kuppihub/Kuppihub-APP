package org.kuppihub.app

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import kotlinx.coroutines.launch
import org.kuppihub.app.auth.DesktopGoogleAuth
import org.kuppihub.app.ui.MainScreen

fun main() = application {
    val desktopAuth = DesktopGoogleAuth()

    // 1. Manage State Here
    var desktopUser by remember { mutableStateOf<org.kuppihub.app.model.KuppiUser?>(null) }
    val scope = rememberCoroutineScope()

    Window(onCloseRequest = ::exitApplication, title = "KuppiHub") {
        MainScreen(
            currentUser = desktopUser, // 👈 Pass the state
            onGoogleLoginClick = {
                scope.launch {
                    val code = desktopAuth.signIn()
                    if (code != null) {
                        println("✅ Login Success!")

                        // 2. UPDATE UI INSTANTLY
                        // (In a real app, you'd exchange 'code' for 'name' via API)
                        // For now, let's prove the UI works:
                        desktopUser = org.kuppihub.app.model.KuppiUser(
                            name = "Geeth Nipun",
                            email = "geeth@kuppihub.org"
                        )
                    }
                }
            },
            onLogoutClick = {
                desktopUser = null // Simple logout
            }
        )
    }
}