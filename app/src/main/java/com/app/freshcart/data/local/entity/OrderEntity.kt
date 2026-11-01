package com.app.freshcart.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.app.freshcart.common.OrderStatus
import com.app.freshcart.common.PaymentMethod
import com.app.freshcart.common.PaymentStatus

@Entity(
    tableName = "orders",
    foreignKeys = [
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["id"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["userId"]),
        Index(value = ["createdAt"])
    ]
)
data class OrderEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val userId: Long,
    val customerName: String,
    val customerPhone: String,
    val deliveryAddress: String,
    val totalAmount: Double,
    val paymentMethod: PaymentMethod = PaymentMethod.COD,
    val paymentStatus: PaymentStatus = PaymentStatus.PENDING,
    val orderStatus: OrderStatus = OrderStatus.PENDING,
    val rejectionReason: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)
