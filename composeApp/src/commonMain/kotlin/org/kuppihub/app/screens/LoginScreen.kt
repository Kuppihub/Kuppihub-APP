package org.kuppihub.app.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons // 👈 Added Import
import androidx.compose.material.icons.filled.MarkEmailRead // 👈 Added Import
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import org.kuppihub.app.model.KuppiUser
import org.kuppihub.app.viewmodel.LoginViewModel

// ... imports ...

@Composable
fun LoginScreen(
    onLoginSuccess: (KuppiUser) -> Unit,
    onGoogleLoginClick: () -> Unit,
    onBackClick: () -> Unit
) {
    val viewModel = remember { LoginViewModel() }

    // State
    var isSignUpMode by remember { mutableStateOf(false) } // 🆕 Toggle Mode
    var name by remember { mutableStateOf("") } // 🆕 Name Field
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    val isLoading by viewModel.isLoading.collectAsState()
    val errorMsg by viewModel.errorMessage.collectAsState()
    val successUser by viewModel.loginSuccessUser.collectAsState()
    val isVerificationMode by viewModel.isVerificationMode.collectAsState()

    LaunchedEffect(successUser) {
        successUser?.let { onLoginSuccess(it) }
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        if (!isVerificationMode) {
            Text(
                if (isSignUpMode) "Create Account" else "Welcome Back",
                style = MaterialTheme.typography.headlineMedium
            )
            Spacer(modifier = Modifier.height(32.dp))
        }

        if (isVerificationMode) {
            // ... (Your Verification UI Code from before) ...
            Icon(
                imageVector = Icons.Default.MarkEmailRead,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text("Verify your Email", style = MaterialTheme.typography.headlineMedium)
            Spacer(modifier = Modifier.height(8.dp))
            Text("We sent a link to your email. Click it and then press 'Done' below.")
            Spacer(modifier = Modifier.height(24.dp))
            Button(onClick = { viewModel.checkVerificationStatus() }, modifier = Modifier.fillMaxWidth()) {
                Text("I have verified (Done)")
            }
            Spacer(modifier = Modifier.height(8.dp))
            TextButton(onClick = { viewModel.resendVerification() }) { Text("Resend Email") }
            Spacer(modifier = Modifier.height(8.dp))
            TextButton(onClick = { viewModel.resetToLogin() }) { Text("Back to Login") }
            if (errorMsg != null) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(errorMsg!!, color = MaterialTheme.colorScheme.error)
            }
        } else {
            // 🆕 NAME FIELD (Only visible in Sign Up mode)
            if (isSignUpMode) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Full Name") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

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

            if (errorMsg != null) {
                Text(errorMsg!!, color = MaterialTheme.colorScheme.error)
                Spacer(modifier = Modifier.height(8.dp))
            }

            if (isLoading) {
                CircularProgressIndicator()
            } else {
                // 🆕 DYNAMIC BUTTON
                Button(
                    onClick = {
                        if (isSignUpMode) {
                            viewModel.signUpWithEmail(email, password, name)
                        } else {
                            viewModel.loginWithEmail(email, password)
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(if (isSignUpMode) "Sign Up" else "Login")
                }
            }

            // 🆕 TOGGLE BUTTON
            TextButton(onClick = { isSignUpMode = !isSignUpMode }) {
                Text(if (isSignUpMode) "Already have an account? Login" else "Don't have an account? Sign Up")
            }

            Spacer(modifier = Modifier.height(16.dp))
            Divider()
            Spacer(modifier = Modifier.height(16.dp))

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
}