package com.app.freshcart.presentation.auth

import com.app.freshcart.common.UserRole
import com.app.freshcart.domain.model.User

data class LoginState(
    val selectedRole: UserRole = UserRole.USER,
    val email: String = "",
    val emailError: String? = null,
    val password: String = "",
    val passwordError: String? = null,
    val isPasswordVisible: Boolean = false,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val loggedInUser: User? = null
)
