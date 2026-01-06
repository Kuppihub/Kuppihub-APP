package org.kuppihub.app

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.kuppihub.app.ui.MainScreen

@Composable
@Preview
fun App() {

    MaterialTheme {
        MainScreen(
            currentUser = null, // Default to Guest for the root app preview
            onGoogleLoginClick = {
                println("Login clicked in App wrapper")
            },
            onLogoutClick = {
                println("Logout clicked in App wrapper")
            }
        )
    }
}