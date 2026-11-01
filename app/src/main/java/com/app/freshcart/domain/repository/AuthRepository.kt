package com.app.freshcart.domain.repository

import com.app.freshcart.common.Resource
import com.app.freshcart.common.UserRole
import com.app.freshcart.domain.model.User
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    fun getCurrentUser(): Flow<User?>
    suspend fun login(email: String, password: String, expectedRole: UserRole): Resource<User>
    suspend fun register(user: User, password: String): Resource<User>
    suspend fun updateProfile(user: User): Resource<User>
    suspend fun logout()
    suspend fun seedInitialUsersIfEmpty()
}
