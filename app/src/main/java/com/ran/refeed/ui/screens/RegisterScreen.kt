// ui/screens/RegisterScreen.kt
package com.ran.refeed.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ran.refeed.R
import com.ran.refeed.data.state.AuthState
import com.ran.refeed.viewmodels.AuthViewModel

@Composable
fun RegisterScreen(
    onNavigateToLogin: () -> Unit,
    onRegisterSuccess: () -> Unit,
    viewModel: AuthViewModel
) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isPasswordVisible by remember { mutableStateOf(false) } // For password visibility toggle

    val authState by viewModel.authState.collectAsStateWithLifecycle()

    // Navigate on successful registration *and* reset the state.
    LaunchedEffect(authState) {
        if (authState is AuthState.Success) {
            onRegisterSuccess()
            // No need to explicitly reset to Idle; AuthState.Initial is the default
        }
    }
    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = R.drawable.login_background),
            contentDescription = "Background Image",
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()), // Add vertical scroll for smaller screens
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color.White)
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Create Account",
                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                        color = Color(0xFF006400)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Sign up to get started",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray
                    )
                    Spacer(modifier = Modifier.height(24.dp))

                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Full Name") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(8.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("Email") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(8.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))


                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("Password") },
                        visualTransformation = if (isPasswordVisible) {
                            androidx.compose.ui.text.input.VisualTransformation.None
                        } else {
                            PasswordVisualTransformation()
                        },
                        trailingIcon = {
                            IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                                Icon(
                                    painter = painterResource(
                                        id = if (isPasswordVisible) R.drawable.ic_eye else R.drawable.ic_eye
                                    ),
                                    contentDescription = if (isPasswordVisible) "Hide password" else "Show password"
                                )
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(8.dp)
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Button(
                        onClick = {
                            if (name.isNotBlank() && email.isNotBlank() && password.isNotBlank()) {
                                viewModel.register(name, email, password, onRegisterSuccess)
                            } //No need to call onRegisterSuccess twice.
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        enabled = authState !is AuthState.Loading,
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF006400))
                    ) {
                        if (authState is AuthState.Loading) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White)
                        } else {
                            Text("Register", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 16.sp)
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))

                    TextButton(onClick = onNavigateToLogin) {
                        Text("Already have an account? Login", color = Color(0xFF006400), fontSize = 14.sp)
                    }

                    // Display error message, if any
                    if (authState is AuthState.Error) {
                        Text(
                            text = (authState as AuthState.Error).message,
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                }
            }
        }
    }
}