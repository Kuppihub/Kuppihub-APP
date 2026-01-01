package org.kuppihub.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import org.kuppihub.app.auth.AndroidGoogleAuth
import org.kuppihub.app.ui.MainScreen

class MainActivity : ComponentActivity() {

    // 1. Create our Auth Helper
    private lateinit var googleAuth: AndroidGoogleAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        googleAuth = AndroidGoogleAuth(this)

        // 2. Setup the "Launcher" to handle the result
        val launcher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK && result.data != null) {
                // User picked an account, let's sign into Firebase
                lifecycleScope.launch {
                    val user = googleAuth.handleLoginResult(result.data!!)
                    if (user != null) {
                        println("LOGIN SUCCESS: ${user.displayName}")
                        // TODO: Refresh your UI or navigate
                    }
                }
            }
        }

        setContent {
            // 3. Pass a "onLoginClick" function to your app
            MainScreen(
                onGoogleLoginClick = {
                    // This connects the click to the Android Launcher
                    val intent = googleAuth.getSignInIntent()
                    launcher.launch(intent)
                },
                authService = googleAuth
            )
        }
    }
}

@Preview
@Composable
fun AppAndroidPreview() {
    App()
}