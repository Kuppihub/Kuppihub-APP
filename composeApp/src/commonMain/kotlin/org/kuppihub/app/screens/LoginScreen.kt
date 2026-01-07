package org.kuppihub.app.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import org.kuppihub.app.model.KuppiUser
import org.kuppihub.app.viewmodel.LoginViewModel

@Composable
fun LoginScreen(
    onLoginSuccess: (KuppiUser) -> Unit, // Callback when ANY login works
    onGoogleLoginClick: () -> Unit,      // Callback for existing Google Logic
    onBackClick: () -> Unit
) {
    val viewModel = remember { LoginViewModel() }

    // State
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMsg by viewModel.errorMessage.collectAsState()
    val successUser by viewModel.loginSuccessUser.collectAsState()

    // Watch for success
    LaunchedEffect(successUser) {
        successUser?.let { user ->
            onLoginSuccess(user)
        }
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Welcome to KuppiHub", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(32.dp))

        // --- EMAIL & PASSWORD FIELDS ---
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))

        // --- ERROR MESSAGE ---
        if (errorMsg != null) {
            Text(errorMsg!!, color = MaterialTheme.colorScheme.error)
            Spacer(modifier = Modifier.height(8.dp))
        }

        // --- BUTTONS ---
        if (isLoading) {
            CircularProgressIndicator()
        } else {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = { viewModel.loginWithEmail(email, password) },
                    modifier = Modifier.weight(1f)
                ) { Text("Login") }

                OutlinedButton(
                    onClick = { viewModel.signUpWithEmail(email, password) },
                    modifier = Modifier.weight(1f)
                ) { Text("Sign Up") }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
        Divider()
        Spacer(modifier = Modifier.height(24.dp))

        // --- GOOGLE LOGIN (EXISTING) ---
        Button(
            onClick = onGoogleLoginClick,
            colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Continue with Google")
        }

        Spacer(modifier = Modifier.height(16.dp))
        TextButton(onClick = onBackClick) { Text("Not now, continue as Guest") }
    }
}