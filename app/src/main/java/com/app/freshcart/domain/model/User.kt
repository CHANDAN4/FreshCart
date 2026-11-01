package com.app.freshcart.domain.model

import com.app.freshcart.common.UserRole

data class User(
    val id: Long = 0,
    val email: String,
    val name: String,
    val phone: String,
    val address: String,
    val profileImageUrl: String? = null,
    val role: UserRole = UserRole.USER
)
