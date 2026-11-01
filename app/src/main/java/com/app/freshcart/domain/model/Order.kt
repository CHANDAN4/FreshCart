package com.app.freshcart.domain.model

import com.app.freshcart.common.OrderStatus
import com.app.freshcart.common.PaymentMethod
import com.app.freshcart.common.PaymentStatus

data class Order(
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
    val createdAt: Long = System.currentTimeMillis(),
    val items: List<OrderItem> = emptyList()
)
