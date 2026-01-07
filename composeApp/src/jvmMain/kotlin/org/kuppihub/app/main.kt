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
import org.kuppihub.app.model.KuppiUser

fun main() = application {
    val desktopAuth = DesktopGoogleAuth()

    var desktopUser by remember { mutableStateOf<KuppiUser?>(null) }
    val scope = rememberCoroutineScope()

    Window(onCloseRequest = ::exitApplication, title = "KuppiHub") {
        MainScreen(
            currentUser = desktopUser,

            // 1. Google Login Handler
            onGoogleLoginClick = {
                scope.launch {
                    println("🚀 Starting Login...")
                    val code = desktopAuth.signIn()

                    if (code != null) {
                        println("✅ Code received! Fetching Profile...")
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

            // 2. 🆕 FIX: Email Login Handler
            onLoginSuccess = { user ->
                // When Email/Password login succeeds, update the desktop user state
                desktopUser = user
            },

            // 3. Logout Handler
            onLogoutClick = {
                desktopUser = null
            }
        )
    }
}