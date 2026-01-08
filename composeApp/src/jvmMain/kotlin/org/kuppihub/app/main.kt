package org.kuppihub.app

import androidx.compose.runtime.*
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.mmk.kmpnotifier.notification.NotifierManager
import com.mmk.kmpnotifier.notification.configuration.NotificationPlatformConfiguration
import io.ktor.client.HttpClient
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import org.kuppihub.app.auth.DesktopGoogleAuth
import org.kuppihub.app.model.KuppiUser
import org.kuppihub.app.ui.MainScreen

// 🔴 ADD THIS MISSING IMPORT
import org.kuppihub.app.data.NotificationRepository

fun main() = application {
    val desktopAuth = DesktopGoogleAuth()

    // 1. Setup Dependencies
    val httpClient = remember {
        HttpClient {
            install(ContentNegotiation) { json() }
        }
    }
    // 🔴 Now this line will work because we imported NotificationRepository
    val repo = remember { NotificationRepository(httpClient) }

    var desktopUser by remember { mutableStateOf<KuppiUser?>(null) }
    val scope = rememberCoroutineScope()

    // 2. Initialize Notifications for Desktop
    LaunchedEffect(Unit) {
        NotifierManager.initialize(
            NotificationPlatformConfiguration.Desktop(
                showPushNotification = true
            )
        )
    }

    // 3. 🔄 POLLING LOGIC (Simulates Push on Desktop)
    LaunchedEffect(desktopUser) {
        while (isActive && desktopUser != null) {
            try {
                // Fetch notifications
                val notifications = repo.getNotifications(1, desktopUser!!.idToken)

                // If unread, show popup
                val unread = notifications.firstOrNull { !it.is_read }

                // 🔴 These calls (it.title, it.body) work now because 'notifications' is valid
                if (unread != null) {
                    val notifier = NotifierManager.getLocalNotifier()
                    notifier.notify(
                        title = unread.title,
                        body = unread.body
                    )
                }
            } catch (e: Exception) {
                println("Polling error: ${e.message}")
            }
            delay(2 * 60 * 1000) // Wait 2 minutes
        }
    }

    Window(onCloseRequest = ::exitApplication, title = "KuppiHub") {
        MainScreen(
            currentUser = desktopUser,

            onGoogleLoginClick = {
                scope.launch {
                    val code = desktopAuth.signIn()
                    if (code != null) {
                        val realUser = desktopAuth.getRealUser(code)
                        if (realUser != null) {
                            desktopUser = realUser
                        }
                    }
                }
            },
            onLoginSuccess = { user -> desktopUser = user },
            onLogoutClick = { desktopUser = null }
        )
    }
}