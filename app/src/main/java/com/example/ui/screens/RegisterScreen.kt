package com.example.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.NinjaCard
import com.example.ui.components.NinjaHeader
import com.example.ui.components.NinjaPrimaryButton
import com.example.ui.components.NinjaSecondaryButton
import com.example.ui.components.NinjaTextField
import com.example.ui.theme.StatusRed
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun RegisterScreen(
    isLoading: Boolean,
    errorMessage: String?,
    onRegister: (username: String, pass: String, passConfirm: String) -> Unit,
    onNavigateToLogin: () -> Unit
) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var showPassword by remember { mutableStateOf(false) }
    val scrollState = rememberScrollState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
            .imePadding(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 520.dp)
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // TOP
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Start
                ) {
                    IconButton(
                        onClick = onNavigateToLogin,
                        modifier = Modifier.testTag("back_to_login_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = TextSecondary
                        )
                    }
                }
                NinjaHeader(compact = false)
            }

            Spacer(modifier = Modifier.height(18.dp))

            // CENTER CARD
            NinjaCard {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Create Account",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        ),
                        color = TextPrimary
                    )
                    Text(
                        text = "Register unique username for private squad voice & chat",
                        fontSize = 12.sp,
                        color = TextSecondary,
                        modifier = Modifier.padding(top = 4.dp, bottom = 18.dp)
                    )

                    if (!errorMessage.isNullOrBlank()) {
                        Text(
                            text = errorMessage,
                            color = StatusRed,
                            fontSize = 12.sp,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 12.dp)
                                .testTag("register_error_text")
                        )
                    }

                    NinjaTextField(
                        value = username,
                        onValueChange = { username = it },
                        label = "Username (3-20 characters)",
                        placeholder = "e.g. stealth_viper",
                        leadingIcon = Icons.Default.Person,
                        testTag = "reg_username_input"
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    NinjaTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = "Password (min. 6 characters)",
                        placeholder = "••••••••",
                        leadingIcon = Icons.Default.Lock,
                        testTag = "reg_password_input",
                        visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            IconButton(onClick = { showPassword = !showPassword }) {
                                Icon(
                                    imageVector = if (showPassword) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    contentDescription = if (showPassword) "Hide password" else "Show password",
                                    tint = TextMuted
                                )
                            }
                        }
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    NinjaTextField(
                        value = confirmPassword,
                        onValueChange = { confirmPassword = it },
                        label = "Confirm Password",
                        placeholder = "••••••••",
                        leadingIcon = Icons.Default.Lock,
                        testTag = "reg_confirm_password_input",
                        visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation()
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    NinjaPrimaryButton(
                        text = "REGISTER ACCOUNT",
                        onClick = { onRegister(username, password, confirmPassword) },
                        testTag = "register_submit_button",
                        loading = isLoading,
                        enabled = username.isNotBlank() && password.isNotBlank() && confirmPassword.isNotBlank()
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    NinjaSecondaryButton(
                        text = "Already have an account? Sign in",
                        onClick = onNavigateToLogin,
                        testTag = "already_have_account_button"
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // BOTTOM
            Text(
                text = "Accounts are encrypted and salted locally with SHA-256",
                fontSize = 11.sp,
                color = TextMuted,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }
    }
}
