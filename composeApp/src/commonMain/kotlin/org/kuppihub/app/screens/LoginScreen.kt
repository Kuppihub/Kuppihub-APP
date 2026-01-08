package org.kuppihub.app.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MarkEmailRead
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kuppihubappnew.composeapp.generated.resources.*
import org.jetbrains.compose.resources.stringResource
import org.kuppihub.app.model.KuppiUser
import org.kuppihub.app.ui.components.GoogleLogoIcon
import org.kuppihub.app.ui.components.KuppiLogo
import org.kuppihub.app.ui.theme.KuppiGradients
import org.kuppihub.app.viewmodel.LoginViewModel

@Composable
fun LoginScreen(
    onLoginSuccess: (KuppiUser) -> Unit,
    onGoogleLoginClick: () -> Unit,
    onBackClick: () -> Unit
) {
    val viewModel = remember { LoginViewModel() }

    // State
    var isSignUpMode by remember { mutableStateOf(false) }
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") } // 🆕 Confirm Password Field
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) } // 🆕 Visibility for confirm
    
    // UI Validation State
    var passwordMatchError by remember { mutableStateOf<String?>(null) }

    val isLoading by viewModel.isLoading.collectAsState()
    val errorMsg by viewModel.errorMessage.collectAsState()
    val successUser by viewModel.loginSuccessUser.collectAsState()
    val isVerificationMode by viewModel.isVerificationMode.collectAsState()

    LaunchedEffect(successUser) {
        successUser?.let { onLoginSuccess(it) }
    }

    // Full screen background with Gradient
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(KuppiGradients.PageBackground),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Logo Section
            // We scale it up a bit for emphasis
            Box(modifier = Modifier.scale(1.2f)) {
                KuppiLogo()
            }
            Spacer(modifier = Modifier.height(32.dp))

            // Main Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 480.dp), // Max width for tablet/desktop
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                shape = RoundedCornerShape(24.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    if (isVerificationMode) {
                        VerificationContent(
                            viewModel = viewModel,
                            errorMsg = errorMsg
                        )
                    } else {
                        // Title
                        Text(
                            text = if (isSignUpMode) "Create Account" else stringResource(Res.string.login_title),
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = if (isSignUpMode) "Sign up to get started" else "Login to continue",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(24.dp))

                        // Fields
                        if (isSignUpMode) {
                            OutlinedTextField(
                                value = name,
                                onValueChange = { name = it },
                                label = { Text("Full Name") },
                                leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                singleLine = true
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                        }

                        OutlinedTextField(
                            value = email,
                            onValueChange = { email = it },
                            label = { Text("Email Address") },
                            leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true
                        )
                        Spacer(modifier = Modifier.height(16.dp))

                        // PASSWORD
                        OutlinedTextField(
                            value = password,
                            onValueChange = { 
                                password = it 
                                if (isSignUpMode && confirmPassword.isNotEmpty() && it != confirmPassword) {
                                     passwordMatchError = "Passwords do not match"
                                } else {
                                     passwordMatchError = null
                                }
                            },
                            label = { Text("Password") },
                            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                            trailingIcon = {
                                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                    Icon(
                                        imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                        contentDescription = if (passwordVisible) "Hide password" else "Show password"
                                    )
                                }
                            },
                            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        // 🆕 CONFIRM PASSWORD (Only in Sign Up)
                        if (isSignUpMode) {
                            OutlinedTextField(
                                value = confirmPassword,
                                onValueChange = { 
                                    confirmPassword = it
                                    if (password != it) {
                                        passwordMatchError = "Passwords do not match"
                                    } else {
                                        passwordMatchError = null
                                    }
                                },
                                label = { Text("Confirm Password") },
                                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                                trailingIcon = {
                                    IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                                        Icon(
                                            imageVector = if (confirmPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                            contentDescription = if (confirmPasswordVisible) "Hide password" else "Show password"
                                        )
                                    }
                                },
                                visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                singleLine = true,
                                isError = passwordMatchError != null
                            )
                            
                            if (passwordMatchError != null) {
                                Text(
                                    text = passwordMatchError!!,
                                    color = MaterialTheme.colorScheme.error,
                                    style = MaterialTheme.typography.labelSmall,
                                    modifier = Modifier.padding(start = 8.dp, top = 4.dp).align(Alignment.Start)
                                )
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Error Message (From ViewModel)
                        if (errorMsg != null) {
                            Text(
                                text = errorMsg!!,
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodySmall,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(bottom = 16.dp)
                            )
                        }

                        // Action Button
                        if (isLoading) {
                            CircularProgressIndicator()
                        } else {
                            Button(
                                onClick = {
                                    if (isSignUpMode) {
                                        if (password != confirmPassword) {
                                            passwordMatchError = "Passwords do not match"
                                        } else {
                                            viewModel.signUpWithEmail(email, password, name)
                                        }
                                    } else {
                                        viewModel.loginWithEmail(email, password)
                                    }
                                },
                                enabled = !isSignUpMode || (password.isNotEmpty() && password == confirmPassword),
                                modifier = Modifier.fillMaxWidth().height(50.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary
                                )
                            ) {
                                Text(
                                    text = if (isSignUpMode) "Sign Up" else stringResource(Res.string.login_button),
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        // Divider with "Or"
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            HorizontalDivider(modifier = Modifier.weight(1f))
                            Text(
                                " or ",
                                modifier = Modifier.padding(horizontal = 8.dp),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            HorizontalDivider(modifier = Modifier.weight(1f))
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        // Google Button
                        OutlinedButton(
                            onClick = onGoogleLoginClick,
                            modifier = Modifier.fillMaxWidth().height(50.dp),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, Color.LightGray),
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = Color.White
                            )
                        ) {
                            GoogleLogoIcon()
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                stringResource(Res.string.google_login_button),
                                color = Color.Black.copy(alpha = 0.87f),
                                fontSize = 16.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Toggle Mode
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text(
                                if (isSignUpMode) "Already have an account?" else "Don't have an account?",
                                style = MaterialTheme.typography.bodyMedium
                            )
                            TextButton(onClick = { 
                                isSignUpMode = !isSignUpMode 
                                passwordMatchError = null // Clear error when switching
                            }) {
                                Text(
                                    if (isSignUpMode) stringResource(Res.string.login_button) else "Sign Up",
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }

            // Guest option (outside card)
            Spacer(modifier = Modifier.height(24.dp))
            if (!isVerificationMode) {
                TextButton(onClick = onBackClick) {
                    Text(
                        "Continue as Guest",
                        color = MaterialTheme.colorScheme.onBackground // Use onBackground for contrast on gradient
                    )
                }
            }
        }
    }
}

@Composable
fun VerificationContent(viewModel: LoginViewModel, errorMsg: String?) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(
            imageVector = Icons.Default.MarkEmailRead,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text("Verify your Email", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            "We sent a link to your email. Click it and then press 'Done' below.",
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = { viewModel.checkVerificationStatus() },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("I have verified (Done)")
        }
        Spacer(modifier = Modifier.height(8.dp))
        TextButton(onClick = { viewModel.resendVerification() }) { Text("Resend Email") }
        Spacer(modifier = Modifier.height(8.dp))
        TextButton(onClick = { viewModel.resetToLogin() }) { Text("Back to Login") }
        if (errorMsg != null) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(errorMsg, color = MaterialTheme.colorScheme.error)
        }
    }
}
