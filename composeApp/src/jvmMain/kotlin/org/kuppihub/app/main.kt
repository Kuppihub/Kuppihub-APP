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
import org.kuppihub.app.data.NotificationRepository
import java.util.UUID // 👈 Import UUID
import kotlinx.serialization.json.Json

fun main() = application {
    val desktopAuth = DesktopGoogleAuth()

    // 1. Setup Dependencies
    val httpClient = remember {
        HttpClient {
            install(ContentNegotiation) {
                // This configuration tells Kotlin to ignore "pagination"
                json(Json {
                    ignoreUnknownKeys = true
                    prettyPrint = true
                    isLenient = true
                })
            }
        }
    }
    val repo = remember { NotificationRepository(httpClient) }

    var desktopUser by remember { mutableStateOf<KuppiUser?>(null) }

    // Generate a unique ID for this Desktop Session
    val desktopDeviceId = remember { UUID.randomUUID().toString() }
    val scope = rememberCoroutineScope()

    // 2. Initialize Notifications
    LaunchedEffect(Unit) {
        NotifierManager.initialize(
            NotificationPlatformConfiguration.Desktop(showPushNotification = true)
        )
    }

    // 3. 🔄 SYNC & POLLING LOGIC
    LaunchedEffect(desktopUser) {
        if (desktopUser != null) {
            val token = desktopUser!!.idToken

            // A. 🔴 REGISTER DEVICE (Fixes "Not registered" issue)
            try {
                println("🖥️ Registering Desktop Device: $desktopDeviceId")
                repo.registerDevice(
                    token = desktopDeviceId, // Use UUID as token for Desktop
                    firebaseUid = desktopUser!!.id,
                    idToken = token
                )
            } catch (e: Exception) {
                println("❌ Registration Failed: ${e.message}")
            }

            // B. Polling Loop
            while (isActive) {
                try {
                    val notifications = repo.getNotifications(1, token)
                    val unread = notifications.firstOrNull { !it.is_read }

                    if (unread != null) {
                        val notifier = NotifierManager.getLocalNotifier()
                        notifier.notify(title = unread.title, body = unread.body)
                    }
                } catch (e: Exception) {
                    println("Polling error: ${e.message}")
                }
                delay(2 * 60 * 1000) // Poll every 2 mins
            }
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
                        if (realUser != null) desktopUser = realUser
                    }
                }
            },
            onLoginSuccess = { user -> desktopUser = user },
            onLogoutClick = { desktopUser = null }
        )
    }
}