package org.kuppihub.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.*
import androidx.lifecycle.lifecycleScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.launch
import org.kuppihub.app.auth.AndroidGoogleAuth
import org.kuppihub.app.model.KuppiUser
import org.kuppihub.app.ui.MainScreen

class MainActivity : ComponentActivity() {

    private lateinit var googleAuth: AndroidGoogleAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        googleAuth = AndroidGoogleAuth(this)
        enableEdgeToEdge()


        val launcher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK && result.data != null) {
                lifecycleScope.launch {
                    val user = googleAuth.handleLoginResult(result.data!!)
                    if (user != null) {
                        println("LOGIN SUCCESS: ${user.displayName}")
                    }
                }
            }
        }

        setContent {
            // 1. Use Native Android Firebase Classes (Fixes Type Mismatch)
            var firebaseUser by remember { mutableStateOf<FirebaseUser?>(FirebaseAuth.getInstance().currentUser) }

            // 2. Setup the Listener correctly using Native SDK
            DisposableEffect(Unit) {
                val auth = FirebaseAuth.getInstance()
                val listener = FirebaseAuth.AuthStateListener { firebaseAuth ->
                    firebaseUser = firebaseAuth.currentUser
                }

                auth.addAuthStateListener(listener)

                onDispose {
                    auth.removeAuthStateListener(listener)
                }
            }

            // 3. Convert Native User to Shared KuppiUser
            val kuppiUser = remember(firebaseUser) {
                firebaseUser?.let { user ->
                    KuppiUser(
                        name = user.displayName ?: "User",
                        email = user.email ?: "",
                        // Native Android returns a Uri, so we must convert to String
                        photoUrl = user.photoUrl?.toString()
                    )
                }
            }

            MainScreen(
                currentUser = kuppiUser,
                onGoogleLoginClick = {
                    launcher.launch(googleAuth.getSignInIntent())
                },
                onLogoutClick = {
                    lifecycleScope.launch {
                        googleAuth.signOut()
                        FirebaseAuth.getInstance().signOut()
                    }
                }
            )
        }
    }
}