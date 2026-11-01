package com.app.freshcart.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.app.freshcart.common.UserRole

@Entity(
    tableName = "users",
    indices = [Index(value = ["email"], unique = true)]
)
data class UserEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val email: String,
    val passwordHash: String,
    val name: String,
    val phone: String,
    val address: String,
    val profileImageUrl: String? = null,
    val role: UserRole = UserRole.USER
)
