package com.app.freshcart.data.repository

import com.app.freshcart.common.Constants
import com.app.freshcart.common.Resource
import com.app.freshcart.common.UserRole
import com.app.freshcart.data.local.dao.UserDao
import com.app.freshcart.data.local.entity.UserEntity
import com.app.freshcart.data.mapper.toDomain
import com.app.freshcart.domain.model.User
import com.app.freshcart.domain.repository.AuthRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val userDao: UserDao
) : AuthRepository {

    private val _currentUser = MutableStateFlow<User?>(null)

    override fun getCurrentUser(): Flow<User?> = _currentUser.asStateFlow()

    override suspend fun login(email: String, password: String, expectedRole: UserRole): Resource<User> {
        val trimmedEmail = email.trim().lowercase()
        val userEntity = userDao.getUserByEmail(trimmedEmail)
            ?: return Resource.Error("User not found with email: $email")

        if (userEntity.passwordHash != password) {
            return Resource.Error("Invalid password. Please check your credentials.")
        }

        if (userEntity.role != expectedRole) {
            return Resource.Error("Unauthorized access. This account cannot log in as ${expectedRole.name.lowercase().replaceFirstChar { it.uppercase() }}.")
        }

        val domainUser = userEntity.toDomain()
        _currentUser.value = domainUser
        return Resource.Success(domainUser)
    }

    override suspend fun register(user: User, password: String): Resource<User> {
        val trimmedEmail = user.email.trim().lowercase()
        val existing = userDao.getUserByEmail(trimmedEmail)
        if (existing != null) {
            return Resource.Error("An account with this email already exists.")
        }

        val entity = UserEntity(
            email = trimmedEmail,
            passwordHash = password,
            name = user.name.trim(),
            phone = user.phone.trim(),
            address = user.address.trim(),
            profileImageUrl = user.profileImageUrl,
            role = user.role
        )
        val id = userDao.insertUser(entity)
        val registeredUser = user.copy(id = id, email = trimmedEmail)
        _currentUser.value = registeredUser
        return Resource.Success(registeredUser)
    }

    override suspend fun updateProfile(user: User): Resource<User> {
        val existing = userDao.getUserById(user.id)
            ?: return Resource.Error("User profile not found.")

        val updatedEntity = existing.copy(
            name = user.name.trim(),
            phone = user.phone.trim(),
            address = user.address.trim(),
            profileImageUrl = user.profileImageUrl
        )
        userDao.updateUser(updatedEntity)
        val domainUser = updatedEntity.toDomain()
        _currentUser.value = domainUser
        return Resource.Success(domainUser)
    }

    override suspend fun logout() {
        _currentUser.value = null
    }

    override suspend fun seedInitialUsersIfEmpty() {
        if (userDao.getUserCount() == 0) {
            // Seed Admin
            userDao.insertUser(
                UserEntity(
                    email = Constants.ADMIN_EMAIL,
                    passwordHash = Constants.ADMIN_PASSWORD,
                    name = Constants.ADMIN_NAME,
                    phone = Constants.ADMIN_PHONE,
                    address = "FreshCart Central HQ, Indiranagar, Bengaluru",
                    profileImageUrl = "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=400",
                    role = UserRole.ADMIN
                )
            )

            // Seed Demo User
            userDao.insertUser(
                UserEntity(
                    email = Constants.DEMO_USER_EMAIL,
                    passwordHash = Constants.DEMO_USER_PASSWORD,
                    name = Constants.DEMO_USER_NAME,
                    phone = Constants.DEMO_USER_PHONE,
                    address = Constants.DEMO_USER_ADDRESS,
                    profileImageUrl = "https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?w=400",
                    role = UserRole.USER
                )
            )
        }
    }
}
