package com.app.freshcart.domain.repository

import com.app.freshcart.common.OrderStatus
import com.app.freshcart.common.PaymentStatus
import com.app.freshcart.common.Resource
import com.app.freshcart.domain.model.CartItem
import com.app.freshcart.domain.model.Order
import kotlinx.coroutines.flow.Flow

interface OrderRepository {
    fun getAllOrders(): Flow<List<Order>>
    fun getOrdersByStatus(status: OrderStatus): Flow<List<Order>>
    fun getOrdersByUser(userId: Long): Flow<List<Order>>
    fun getOrdersByUserAndStatus(userId: Long, status: OrderStatus): Flow<List<Order>>
    suspend fun getOrderById(orderId: Long): Order?
    fun getOngoingOrdersCount(): Flow<Int>
    fun getCompletedOrdersCount(): Flow<Int>
    fun getPastOrdersCount(): Flow<Int>
    suspend fun placeOrder(
        userId: Long,
        customerName: String,
        customerPhone: String,
        deliveryAddress: String,
        cartItems: List<CartItem>,
        deliveryCharge: Double
    ): Resource<Long>
    suspend fun acceptOrder(orderId: Long): Resource<Unit>
    suspend fun rejectOrder(orderId: Long, reason: String): Resource<Unit>
    suspend fun completeOrder(orderId: Long): Resource<Unit>
    suspend fun markPaymentConfirmed(orderId: Long): Resource<Unit>
    suspend fun reorder(userId: Long, orderId: Long): Resource<Unit>
}
