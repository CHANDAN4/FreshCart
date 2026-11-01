package com.app.freshcart.presentation.user.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.app.freshcart.presentation.common.FreshButton
import com.app.freshcart.presentation.common.FreshTextField
import com.app.freshcart.presentation.common.LoadingView
import com.app.freshcart.ui.theme.FreshGreenContainer
import com.app.freshcart.ui.theme.FreshGreenDark
import com.app.freshcart.ui.theme.FreshGreenPrimary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreen(
    onNavigateBack: () -> Unit,
    viewModel: EditProfileViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(key1 = true) {
        viewModel.snackbarEvent.collect { message ->
            snackbarHostState.showSnackbar(message)
        }
    }

    LaunchedEffect(key1 = true) {
        viewModel.saveSuccessEvent.collect {
            onNavigateBack()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Edit Profile",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = FreshGreenDark
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = FreshGreenDark
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { paddingValues ->
        if (state.isLoading && state.user == null) {
            LoadingView(message = "Loading profile...")
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 20.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Spacer(modifier = Modifier.height(4.dp))

                // Avatar preview
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(FreshGreenContainer),
                    contentAlignment = Alignment.Center
                ) {
                    if (state.profileImageUrl.isNotBlank()) {
                        AsyncImage(
                            model = state.profileImageUrl,
                            contentDescription = "Avatar",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            tint = FreshGreenPrimary,
                            modifier = Modifier.size(44.dp)
                        )
                    }
                }

                // Name field
                FreshTextField(
                    value = state.name,
                    onValueChange = { viewModel.onNameChanged(it) },
                    label = "Full Name *",
                    placeholder = "Enter your full name",
                    leadingIcon = {
                        Icon(Icons.Default.Person, contentDescription = null, tint = FreshGreenDark)
                    },
                    isError = state.nameError != null,
                    errorMessage = state.nameError,
                    modifier = Modifier.fillMaxWidth()
                )

                // Read-only email
                OutlinedTextField(
                    value = state.user?.email ?: "",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Email (Cannot be modified)") },
                    leadingIcon = {
                        Icon(Icons.Default.Email, contentDescription = null, tint = FreshGreenDark)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp)
                )

                // Phone field
                FreshTextField(
                    value = state.phone,
                    onValueChange = { viewModel.onPhoneChanged(it) },
                    label = "Mobile Number *",
                    placeholder = "Enter 10-digit mobile number",
                    leadingIcon = {
                        Icon(Icons.Default.Phone, contentDescription = null, tint = FreshGreenDark)
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    isError = state.phoneError != null,
                    errorMessage = state.phoneError,
                    modifier = Modifier.fillMaxWidth()
                )

                // Image URL field
                FreshTextField(
                    value = state.profileImageUrl,
                    onValueChange = { viewModel.onImageUrlChanged(it) },
                    label = "Profile Image URL (Optional)",
                    placeholder = "Paste image link",
                    leadingIcon = {
                        Icon(Icons.Default.Image, contentDescription = null, tint = FreshGreenDark)
                    },
                    modifier = Modifier.fillMaxWidth()
                )

                // Delivery Address
                OutlinedTextField(
                    value = state.address,
                    onValueChange = { viewModel.onAddressChanged(it) },
                    label = { Text("Delivery Address *") },
                    placeholder = { Text("Flat/House No., Street, Landmark, City, Pincode") },
                    leadingIcon = {
                        Icon(Icons.Default.LocationOn, contentDescription = null, tint = FreshGreenDark)
                    },
                    isError = state.addressError != null,
                    supportingText = state.addressError?.let { { Text(it) } },
                    maxLines = 4,
                    minLines = 3,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                FreshButton(
                    text = "Save Changes",
                    onClick = { viewModel.saveProfile() },
                    isLoading = state.isSaving,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}
