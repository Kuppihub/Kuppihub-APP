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

    var desktopUser by remember { mutableStateOf<org.kuppihub.app.model.KuppiUser?>(null) }
    val scope = rememberCoroutineScope()

    Window(onCloseRequest = ::exitApplication, title = "KuppiHub") {
        MainScreen(
            currentUser = desktopUser,
            onGoogleLoginClick = {
                scope.launch {
                    println("🚀 Starting Login...")

                    // 1. Open Browser & Get Code
                    val code = desktopAuth.signIn()

                    if (code != null) {
                        println("✅ Code received! Fetching Profile...")

                        // 2. Fetch the REAL User Info (Name, Email, Photo)
                        val realUser = desktopAuth.getRealUser(code)

                        if (realUser != null) {
                            println("✅ Hello, ${realUser.name}")
                            desktopUser = realUser
                        } else {
                            println("❌ Failed to get profile")
                        }
                    }
                }
            },
            onLogoutClick = {
                desktopUser = null
            }
        )
    }
}