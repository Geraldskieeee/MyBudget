package com.example.mybudget.ui.screens.auth

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun RegisterScreen(
    onRegisterSuccess: () -> Unit,
    onNavigateToLogin: () -> Unit,
    viewModel: LoginViewModel
) {
    val signInState by viewModel.signInState.collectAsState()
    
    var username by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var termsAccepted by remember { mutableStateOf(false) }
    var showForgotDialog by remember { mutableStateOf(false) }
    var forgotEmail by remember { mutableStateOf("") }
    val context = androidx.compose.ui.platform.LocalContext.current

    LaunchedEffect(signInState) {
        if (signInState is SignInState.Success) {
            onRegisterSuccess()
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "My Budget",
                fontSize = 42.sp,
                fontFamily = androidx.compose.ui.text.font.FontFamily.Serif,
                color = androidx.compose.ui.graphics.Color(0xFF105436)
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "Join My Budget to manage your finances.",
                style = MaterialTheme.typography.bodyMedium,
                color = androidx.compose.ui.graphics.Color.Gray,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(32.dp))

            OutlinedTextField(
                value = username,
                onValueChange = { username = it },
                label = { Text("Username", color = androidx.compose.ui.graphics.Color.DarkGray) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedContainerColor = androidx.compose.ui.graphics.Color.Transparent,
                    focusedContainerColor = androidx.compose.ui.graphics.Color.Transparent,
                    unfocusedBorderColor = androidx.compose.ui.graphics.Color.Gray,
                    focusedBorderColor = androidx.compose.ui.graphics.Color(0xFF105436),
                ),
                shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp)
            )
            
            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email", color = androidx.compose.ui.graphics.Color.DarkGray) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedContainerColor = androidx.compose.ui.graphics.Color.Transparent,
                    focusedContainerColor = androidx.compose.ui.graphics.Color.Transparent,
                    unfocusedBorderColor = androidx.compose.ui.graphics.Color.Gray,
                    focusedBorderColor = androidx.compose.ui.graphics.Color(0xFF105436),
                ),
                shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp)
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password", color = androidx.compose.ui.graphics.Color.DarkGray) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    val image = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(imageVector = image, contentDescription = "Toggle password visibility", tint = androidx.compose.ui.graphics.Color.DarkGray)
                    }
                },
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedContainerColor = androidx.compose.ui.graphics.Color.Transparent,
                    focusedContainerColor = androidx.compose.ui.graphics.Color.Transparent,
                    unfocusedBorderColor = androidx.compose.ui.graphics.Color.Gray,
                    focusedBorderColor = androidx.compose.ui.graphics.Color(0xFF105436),
                ),
                shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp)
            )

            if (signInState is SignInState.Error) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = (signInState as SignInState.Error).message,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            TextButton(
                onClick = { showForgotDialog = true },
                modifier = Modifier.align(Alignment.End)
            ) {
                Text("Forgot Password?", color = androidx.compose.ui.graphics.Color(0xFF105436), fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { termsAccepted = !termsAccepted }
                    .padding(vertical = 8.dp)
            ) {
                Checkbox(
                    checked = termsAccepted,
                    onCheckedChange = { termsAccepted = it },
                    colors = CheckboxDefaults.colors(checkedColor = MaterialTheme.colorScheme.primary)
                )
                Text(
                    text = "I agree to the Terms and Conditions",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
            
            if (signInState is SignInState.Loading) {
                CircularProgressIndicator()
            } else {
                Button(
                    onClick = { 
                        if (termsAccepted) {
                            if (username.isBlank()) {
                                android.widget.Toast.makeText(context, "Please enter a username.", android.widget.Toast.LENGTH_SHORT).show()
                            } else {
                                viewModel.createAccountWithEmail(email, password, username)
                            }
                        } else {
                            android.widget.Toast.makeText(context, "Please agree to the Terms and Conditions first.", android.widget.Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = androidx.compose.ui.graphics.Color(0xFF105436)),
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp)
                ) {
                    Text(
                        text = "Sign Up",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Normal,
                        color = androidx.compose.ui.graphics.Color.White
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text("Already have an account? ", color = androidx.compose.ui.graphics.Color.Black)
                    Text(
                        text = "Login",
                        color = androidx.compose.ui.graphics.Color(0xFF105436),
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.clickable { onNavigateToLogin() }
                    )
                }
            }
        }
    }

    if (showForgotDialog) {
        AlertDialog(
            onDismissRequest = { showForgotDialog = false },
            title = { Text("Reset Password") },
            text = {
                Column {
                    Text("Enter your registered email address and we'll send you a link to reset your password.")
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedTextField(
                        value = forgotEmail,
                        onValueChange = { forgotEmail = it },
                        label = { Text("Email") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.resetPassword(forgotEmail) { success, msg ->
                        if (success) {
                            android.widget.Toast.makeText(context, "Password reset email sent!", android.widget.Toast.LENGTH_SHORT).show()
                            showForgotDialog = false
                            forgotEmail = ""
                        } else {
                            android.widget.Toast.makeText(context, msg ?: "Failed to send reset email.", android.widget.Toast.LENGTH_SHORT).show()
                        }
                    }
                }) {
                    Text("Send Link")
                }
            },
            dismissButton = {
                TextButton(onClick = { showForgotDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
