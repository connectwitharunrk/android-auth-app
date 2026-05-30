package com.example.authapp.presentation.screens.auth.signup

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.authapp.presentation.navigation.Screen
import kotlinx.coroutines.flow.collectLatest

@Composable
fun SignupScreen(
    navController: NavController,
    viewModel: SignupViewModel = hiltViewModel()
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    val state by viewModel.state.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.event.collectLatest { event ->
            when (event) {
                SignupEvent.NavigateToHome -> {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Signup.route) { inclusive = true }
                    }
                }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Header
        Text(
            text = "Create Account",
            style = MaterialTheme.typography.headlineLarge,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Text(
            text = "Sign up to get started",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 32.dp),
            textAlign = TextAlign.Center
        )

        // Email Field
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            leadingIcon = { Icon(Icons.Filled.Email, contentDescription = "Email") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            singleLine = true
        )

        // Password Field
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            leadingIcon = { Icon(Icons.Filled.Lock, contentDescription = "Password") },
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            singleLine = true
        )

        // Confirm Password Field
        OutlinedTextField(
            value = confirmPassword,
            onValueChange = { confirmPassword = it },
            label = { Text("Confirm Password") },
            leadingIcon = { Icon(Icons.Filled.Lock, contentDescription = "Confirm Password") },
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp),
            singleLine = true
        )

        // Sign Up Button
        Button(
            onClick = {
                viewModel.processIntent(
                    SignupIntent.SignUp(email, password, confirmPassword)
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            enabled = state !is SignupState.Loading &&
                    email.isNotEmpty() &&
                    password.isNotEmpty() &&
                    confirmPassword.isNotEmpty()
        ) {
            if (state is SignupState.Loading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = MaterialTheme.colorScheme.onPrimary
                )
            } else {
                Text("Create Account", style = MaterialTheme.typography.labelLarge)
            }
        }

        // Error Message
        if (state is SignupState.Error) {
            Text(
                text = (state as SignupState.Error).message,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(top = 16.dp),
                textAlign = TextAlign.Center
            )
        }

        // Back to Login
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 32.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            Text("Already have an account? ")
            TextButton(
                onClick = { navController.popBackStack() }
            ) {
                Text("Sign In")
            }
        }
    }
}
