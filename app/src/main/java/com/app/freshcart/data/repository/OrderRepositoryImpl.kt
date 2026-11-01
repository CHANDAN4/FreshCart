package com.app.freshcart.data.repository

import com.app.freshcart.common.OrderStatus
import com.app.freshcart.common.PaymentMethod
import com.app.freshcart.common.PaymentStatus
import com.app.freshcart.common.Resource
import com.app.freshcart.data.local.dao.CartDao
import com.app.freshcart.data.local.dao.OrderDao
import com.app.freshcart.data.local.dao.ProductDao
import com.app.freshcart.data.local.entity.OrderEntity
import com.app.freshcart.data.local.entity.OrderItemEntity
import com.app.freshcart.data.mapper.toDomain
import com.app.freshcart.domain.model.CartItem
import com.app.freshcart.domain.model.Order
import com.app.freshcart.domain.repository.CartRepository
import com.app.freshcart.domain.repository.OrderRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OrderRepositoryImpl @Inject constructor(
    private val orderDao: OrderDao,
    private val cartDao: CartDao,
    private val productDao: ProductDao,
    private val cartRepository: CartRepository
) : OrderRepository {

    override fun getAllOrders(): Flow<List<Order>> {
        return orderDao.getAllOrders().map { orderEntities ->
            orderEntities.map { entity ->
                val items = orderDao.getOrderItems(entity.id).map { it.toDomain() }
                entity.toDomain(items)
            }
        }
    }

    override fun getOrdersByStatus(status: OrderStatus): Flow<List<Order>> {
        return orderDao.getOrdersByStatus(status).map { orderEntities ->
            orderEntities.map { entity ->
                val items = orderDao.getOrderItems(entity.id).map { it.toDomain() }
                entity.toDomain(items)
            }
        }
    }

    override fun getOrdersByUser(userId: Long): Flow<List<Order>> {
        return orderDao.getOrdersByUser(userId).map { orderEntities ->
            orderEntities.map { entity ->
                val items = orderDao.getOrderItems(entity.id).map { it.toDomain() }
                entity.toDomain(items)
            }
        }
    }

    override fun getOrdersByUserAndStatus(userId: Long, status: OrderStatus): Flow<List<Order>> {
        return orderDao.getOrdersByUserAndStatus(userId, status).map { orderEntities ->
            orderEntities.map { entity ->
                val items = orderDao.getOrderItems(entity.id).map { it.toDomain() }
                entity.toDomain(items)
            }
        }
    }

    override suspend fun getOrderById(orderId: Long): Order? {
        val entity = orderDao.getOrderById(orderId) ?: return null
        val items = orderDao.getOrderItems(orderId).map { it.toDomain() }
        return entity.toDomain(items)
    }

    override fun getOngoingOrdersCount(): Flow<Int> = orderDao.getOngoingOrdersCount()

    override fun getCompletedOrdersCount(): Flow<Int> = orderDao.getCompletedOrdersCount()

    override fun getPastOrdersCount(): Flow<Int> = orderDao.getPastOrdersCount()

    override suspend fun placeOrder(
        userId: Long,
        customerName: String,
        customerPhone: String,
        deliveryAddress: String,
        cartItems: List<CartItem>,
        deliveryCharge: Double
    ): Resource<Long> {
        if (cartItems.isEmpty()) {
            return Resource.Error("Cart is empty. Add items before placing an order.")
        }

        return try {
            val itemsSubtotal = cartItems.sumOf { it.subtotal }
            val totalAmount = itemsSubtotal + deliveryCharge

            val orderEntity = OrderEntity(
                userId = userId,
                customerName = customerName.trim(),
                customerPhone = customerPhone.trim(),
                deliveryAddress = deliveryAddress.trim(),
                totalAmount = totalAmount,
                paymentMethod = PaymentMethod.COD,
                paymentStatus = PaymentStatus.PENDING,
                orderStatus = OrderStatus.PENDING,
                createdAt = System.currentTimeMillis()
            )

            val orderItemEntities = cartItems.map { cartItem ->
                OrderItemEntity(
                    orderId = 0, // Assigned in transaction
                    productId = cartItem.product.id,
                    productName = cartItem.product.name,
                    productPrice = cartItem.product.effectivePrice,
                    quantity = cartItem.quantity,
                    unit = "${cartItem.product.unitQuantity} ${cartItem.product.unit.displayName}",
                    imageUrl = cartItem.product.imageUrl
                )
            }

            val orderId = orderDao.createOrderWithItemsAndClearCart(
                order = orderEntity,
                items = orderItemEntities,
                clearCart = { cartDao.clearCartByUser(userId) }
            )

            Resource.Success(orderId)
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Failed to place order", e)
        }
    }

    override suspend fun acceptOrder(orderId: Long): Resource<Unit> {
        val order = orderDao.getOrderById(orderId)
            ?: return Resource.Error("Order not found with ID: #$orderId")

        // Business rule: Admin can accept an order only when payment is confirmed as received
        if (order.paymentStatus != PaymentStatus.CONFIRMED) {
            return Resource.Error(
                "Payment has not been confirmed for this order. Please confirm payment before accepting."
            )
        }

        return try {
            orderDao.updateOrderStatus(orderId, OrderStatus.ACCEPTED)
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Failed to accept order", e)
        }
    }

    override suspend fun rejectOrder(orderId: Long, reason: String): Resource<Unit> {
        val trimmedReason = reason.trim()
        if (trimmedReason.isBlank()) {
            return Resource.Error("Please provide a rejection reason.")
        }

        return try {
            orderDao.rejectOrder(orderId, OrderStatus.REJECTED, trimmedReason)
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Failed to reject order", e)
        }
    }

    override suspend fun completeOrder(orderId: Long): Resource<Unit> {
        return try {
            orderDao.updateOrderStatus(orderId, OrderStatus.COMPLETED)
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Failed to complete order", e)
        }
    }

    override suspend fun markPaymentConfirmed(orderId: Long): Resource<Unit> {
        return try {
            orderDao.updatePaymentStatus(orderId, PaymentStatus.CONFIRMED)
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Failed to confirm payment", e)
        }
    }

    override suspend fun reorder(userId: Long, orderId: Long): Resource<Unit> {
        return try {
            val pastItems = orderDao.getOrderItems(orderId)
            if (pastItems.isEmpty()) {
                return Resource.Error("No items found to reorder.")
            }

            for (item in pastItems) {
                val latestProduct = productDao.getProductById(item.productId)
                if (latestProduct != null && latestProduct.isAvailable) {
                    cartRepository.addToCart(
                        userId = userId,
                        product = latestProduct.toDomain(),
                        quantity = item.quantity
                    )
                }
            }
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Failed to reorder items", e)
        }
    }
}
