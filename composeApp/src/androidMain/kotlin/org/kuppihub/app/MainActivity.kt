package org.kuppihub.app

import android.os.Bundle
import android.content.Intent
// 🔴 ADD THESE TWO IMPORTS
import android.os.Build
import android.Manifest

import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.*
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine
import org.kuppihub.app.auth.AndroidGoogleAuth
import org.kuppihub.app.data.KuppiRepository
import org.kuppihub.app.model.KuppiUser
import org.kuppihub.app.ui.MainScreen
import com.mmk.kmpnotifier.notification.NotifierManager
import com.mmk.kmpnotifier.notification.configuration.NotificationPlatformConfiguration
import org.kuppihub.app.data.NotificationRepository
import io.ktor.client.HttpClient
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json

class MainActivity : ComponentActivity() {

    private lateinit var googleAuth: AndroidGoogleAuth
    private val _currentUserState = MutableStateFlow<KuppiUser?>(null)

    // Permission launcher to handle the user's response
    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            println("✅ Notification Permission Granted")
        } else {
            println("❌ Notification Permission Denied")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        NotifierManager.initialize(
            configuration = NotificationPlatformConfiguration.Android(
                notificationIconResId = android.R.drawable.ic_dialog_info,
                showPushNotification = true
            )
        )

        // 2. ASK FOR PERMISSION IMMEDIATELY ON STARTUP (Android 13+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }

        val httpClient = HttpClient { install(ContentNegotiation) { json() } }
        val notificationRepo = NotificationRepository(httpClient)

        NotifierManager.addListener(object : NotifierManager.Listener {
            override fun onNewToken(token: String) {
                println("🔥 FCM Token (Refreshed): $token")
            }
        })

        googleAuth = AndroidGoogleAuth(this, _currentUserState)
        enableEdgeToEdge()

        val launcher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK && result.data != null) {
                lifecycleScope.launch {
                    try {
                        googleAuth.handleLoginResult(result.data!!)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        }

        setContent {
            var firebaseUser by remember { mutableStateOf<FirebaseUser?>(FirebaseAuth.getInstance().currentUser) }
            var currentIdToken by remember { mutableStateOf("") }

            DisposableEffect(Unit) {
                val auth = FirebaseAuth.getInstance()
                val listener = FirebaseAuth.AuthStateListener { firebaseAuth ->
                    firebaseUser = firebaseAuth.currentUser
                }
                auth.addAuthStateListener(listener)
                onDispose { auth.removeAuthStateListener(listener) }
            }

            // 🔍 MASTER SYNC BLOCK
            LaunchedEffect(firebaseUser) {
                // 1. Get Token & Sync User Profile
                if (firebaseUser != null) {
                    try {
                        val result = firebaseUser!!.getIdToken(false).await()
                        currentIdToken = result.token ?: ""

                        val user = KuppiUser(
                            id = firebaseUser!!.uid,
                            name = firebaseUser!!.displayName ?: "User",
                            email = firebaseUser!!.email ?: "",
                            photoUrl = firebaseUser!!.photoUrl?.toString(),
                            idToken = currentIdToken
                        )
                        KuppiRepository.syncUserToBackend(user)
                        println("✅ User Profile Synced")
                    } catch (e: Exception) {
                        println("Error fetching token: ${e.message}")
                        currentIdToken = ""
                    }
                } else {
                    currentIdToken = ""
                }

                // 2. SYNC NOTIFICATION (With Auth Header)
                try {
                    val notifier = NotifierManager.getPushNotifier()
                    val token = notifier.getToken()

                    if (token != null) {
                        val userId = firebaseUser?.uid
                        val authToken = if (currentIdToken.isNotBlank()) currentIdToken else null

                        notificationRepo.registerDevice(token, userId, authToken)
                        println("📲 Notification Device Registered (User: $userId)")
                    }
                } catch (e: Exception) {
                    println("❌ Failed to register for notifications: ${e.message}")
                }
            }

            val kuppiUser = remember(firebaseUser, currentIdToken) {
                firebaseUser?.let { user ->
                    KuppiUser(
                        id = user.uid,
                        name = user.displayName ?: "User",
                        email = user.email ?: "",
                        photoUrl = user.photoUrl?.toString(),
                        idToken = currentIdToken
                    )
                }
            }

            MainScreen(
                currentUser = kuppiUser,
                onGoogleLoginClick = { launcher.launch(googleAuth.getSignInIntent()) },
                onLoginSuccess = { },
                onLogoutClick = {
                    lifecycleScope.launch {
                        googleAuth.signOut()
                        FirebaseAuth.getInstance().signOut()
                        currentIdToken = ""
                    }
                }
            )
        }
    }
}

suspend fun <T> Task<T>.await(): T = suspendCoroutine { continuation ->
    addOnSuccessListener { result -> continuation.resume(result) }
    addOnFailureListener { exception -> continuation.resumeWithException(exception) }
}