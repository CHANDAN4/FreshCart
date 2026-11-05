package com.app.freshcart.presentation.registration.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.freshcart.common.Constants
import com.app.freshcart.common.Resource
import com.app.freshcart.common.UserRole
import com.app.freshcart.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginState())
    val uiState: StateFlow<LoginState> = _uiState.asStateFlow()

    fun onRoleSelected(role: UserRole) {
        _uiState.update {
            it.copy(
                selectedRole = role,
                email = if (role == UserRole.ADMIN) Constants.ADMIN_EMAIL else Constants.DEMO_USER_EMAIL,
                password = if (role == UserRole.ADMIN) Constants.ADMIN_PASSWORD else Constants.DEMO_USER_PASSWORD,
                emailError = null,
                passwordError = null,
                errorMessage = null
            )
        }
    }

    fun onEmailChanged(email: String) {
        _uiState.update {
            it.copy(
                email = email,
                emailError = null,
                errorMessage = null
            )
        }
    }

    fun onPasswordChanged(password: String) {
        _uiState.update {
            it.copy(
                password = password,
                passwordError = null,
                errorMessage = null
            )
        }
    }

    fun togglePasswordVisibility() {
        _uiState.update { it.copy(isPasswordVisible = !it.isPasswordVisible) }
    }

    fun fillDemoCredentials() {
        val role = _uiState.value.selectedRole
        _uiState.update {
            it.copy(
                email = if (role == UserRole.ADMIN) Constants.ADMIN_EMAIL else Constants.DEMO_USER_EMAIL,
                password = if (role == UserRole.ADMIN) Constants.ADMIN_PASSWORD else Constants.DEMO_USER_PASSWORD,
                emailError = null,
                passwordError = null
            )
        }
    }

    fun login() {
        val currentState = _uiState.value
        val email = currentState.email.trim()
        val password = currentState.password

        var hasError = false
        var emailError: String? = null
        var passwordError: String? = null

        if (email.isBlank()) {
            emailError = "Email or mobile number is required"
            hasError = true
        }

        if (password.isBlank()) {
            passwordError = "Password is required"
            hasError = true
        }

        if (hasError) {
            _uiState.update {
                it.copy(emailError = emailError, passwordError = passwordError)
            }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            val result = authRepository.login(
                email = email,
                password = password,
                expectedRole = currentState.selectedRole
            )
            when (result) {
                is Resource.Success -> {
                    _uiState.update {
                        it.copy(isLoading = false, loggedInUser = result.data)
                    }
                }
                is Resource.Error -> {
                    _uiState.update {
                        it.copy(isLoading = false, errorMessage = result.message)
                    }
                }
                is Resource.Loading -> {
                    _uiState.update { it.copy(isLoading = true) }
                }
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}
