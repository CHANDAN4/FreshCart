package com.app.freshcart.presentation.registration.signUp

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.ShoppingBasket
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.app.freshcart.common.UserRole
import com.app.freshcart.presentation.common.FreshButton
import com.app.freshcart.presentation.common.FreshTextField
import com.app.freshcart.ui.theme.FreshBackground
import com.app.freshcart.ui.theme.FreshCardBorder
import com.app.freshcart.ui.theme.FreshGreenContainer
import com.app.freshcart.ui.theme.FreshGreenPrimary
import com.app.freshcart.ui.theme.FreshTextPrimary
import com.app.freshcart.ui.theme.FreshTextSecondary

@Composable
fun SignUpScreen(
    onNavigateToLoginScreen: () -> Unit,
    viewModel: SignUpViewModel = hiltViewModel()
) {
    //val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    var user by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var pwd by remember { mutableStateOf("") }
    var cpwd by remember { mutableStateOf("") }


   /* LaunchedEffect(uiState.errorMessage) {
        val error = uiState.errorMessage
        if (error != null) {
            snackbarHostState.showSnackbar(error)
            viewModel.clearError()
        }
    }*/

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = FreshBackground
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(20.dp))

            // App Brand Logo
            Box(
                modifier = Modifier
                    .size(76.dp)
                    .clip(CircleShape)
                    .background(FreshGreenContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.ShoppingBasket,
                    contentDescription = "FreshCart",
                    tint = FreshGreenPrimary,
                    modifier = Modifier.size(42.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Sign-Up",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 26.sp,
                    color = FreshTextPrimary
                )
            )

            Text(
                text = "Sign-Up to continue grocery shopping",
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = FreshTextSecondary,
                    fontSize = 14.sp
                ),
                modifier = Modifier.padding(top = 4.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Login Card Form
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, FreshCardBorder),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {

                    Spacer(modifier = Modifier.height(18.dp))

                    // Email / Mobile Field
                    FreshTextField(
                        value = user,
                        onValueChange = {
                            user=it
                            //viewModel.onEmailChanged(it)
                        },
                        label = "User Name",
                        placeholder = "Please enter user name",
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Email,
                                contentDescription = null,
                                tint = FreshGreenPrimary
                            )
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text)
                    )

                    Spacer(modifier = Modifier.height(16.dp))
                    // Email / Mobile Field
                    FreshTextField(
                        value = email,
                        onValueChange = {
                            email=it
                            //viewModel.onEmailChanged(it)
                        },
                        label = "Email or Mobile Number",
                        placeholder = "e.g. user@freshcart.com",
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Email,
                                contentDescription = null,
                                tint = FreshGreenPrimary
                            )
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Password Field
                    FreshTextField(
                        value = pwd,
                        onValueChange = { pwd=it },
                        label = "Password",
                        placeholder = "Enter your password",
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = null,
                                tint = FreshGreenPrimary
                            )
                        },
                        trailingIcon = {
                            IconButton(onClick = {
                                //viewModel.togglePasswordVisibility()
                            }) {
                                Icon(
                                    imageVector = /*if (uiState.isPasswordVisible) Icons.Default.Visibility else */Icons.Default.VisibilityOff,
                                    contentDescription = "Toggle Password",
                                    tint = FreshTextSecondary
                                )
                            }
                        },
                        /*isError = uiState.passwordError != null,
                        errorMessage = uiState.passwordError,
                        visualTransformation = if (uiState.isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),*/
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
                    )


                    Spacer(modifier = Modifier.height(16.dp))

                    // Confirm Password Field
                    FreshTextField(
                        value = cpwd,
                        onValueChange = { cpwd=it },
                        label = "Confirm Password",
                        placeholder = "Enter your confirm password",
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = null,
                                tint = FreshGreenPrimary
                            )
                        },
                        trailingIcon = {
                            IconButton(onClick = {
                                //viewModel.togglePasswordVisibility()
                            }) {
                                Icon(
                                    imageVector = /*if (uiState.isPasswordVisible) Icons.Default.Visibility else */Icons.Default.VisibilityOff,
                                    contentDescription = "Toggle Password",
                                    tint = FreshTextSecondary
                                )
                            }
                        },
                        /*isError = uiState.passwordError != null,
                        errorMessage = uiState.passwordError,
                        visualTransformation = if (uiState.isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),*/
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
                    )


                    Spacer(modifier = Modifier.height(40.dp))

                    // Submit Button
                    FreshButton(
                        text = "Submit",
                        onClick = { onNavigateToLoginScreen() }
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                }
            }


            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}




