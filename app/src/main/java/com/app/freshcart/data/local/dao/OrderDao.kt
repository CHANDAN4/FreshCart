package com.app.freshcart.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.app.freshcart.common.OrderStatus
import com.app.freshcart.common.PaymentStatus
import com.app.freshcart.data.local.entity.OrderEntity
import com.app.freshcart.data.local.entity.OrderItemEntity
import kotlinx.coroutines.flow.Flow

data class OrderWithItems(
    val order: OrderEntity,
    val items: List<OrderItemEntity>
)

@Dao
interface OrderDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrder(order: OrderEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrderItems(items: List<OrderItemEntity>)

    @Update
    suspend fun updateOrder(order: OrderEntity)

    @Query("SELECT * FROM orders WHERE id = :id LIMIT 1")
    suspend fun getOrderById(id: Long): OrderEntity?

    @Query("SELECT * FROM order_items WHERE orderId = :orderId")
    suspend fun getOrderItems(orderId: Long): List<OrderItemEntity>

    @Query("SELECT * FROM orders ORDER BY createdAt DESC")
    fun getAllOrders(): Flow<List<OrderEntity>>

    @Query("SELECT * FROM orders WHERE orderStatus = :status ORDER BY createdAt DESC")
    fun getOrdersByStatus(status: OrderStatus): Flow<List<OrderEntity>>

    @Query("SELECT * FROM orders WHERE userId = :userId ORDER BY createdAt DESC")
    fun getOrdersByUser(userId: Long): Flow<List<OrderEntity>>

    @Query("SELECT * FROM orders WHERE userId = :userId AND orderStatus = :status ORDER BY createdAt DESC")
    fun getOrdersByUserAndStatus(userId: Long, status: OrderStatus): Flow<List<OrderEntity>>

    @Query("UPDATE orders SET orderStatus = :orderStatus WHERE id = :orderId")
    suspend fun updateOrderStatus(orderId: Long, orderStatus: OrderStatus)

    @Query("UPDATE orders SET paymentStatus = :paymentStatus WHERE id = :orderId")
    suspend fun updatePaymentStatus(orderId: Long, paymentStatus: PaymentStatus)

    @Query("UPDATE orders SET orderStatus = :orderStatus, rejectionReason = :reason WHERE id = :orderId")
    suspend fun rejectOrder(orderId: Long, orderStatus: OrderStatus = OrderStatus.REJECTED, reason: String)

    @Query("SELECT COUNT(*) FROM orders WHERE orderStatus IN ('PENDING', 'ACCEPTED')")
    fun getOngoingOrdersCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM orders WHERE orderStatus = 'COMPLETED'")
    fun getCompletedOrdersCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM orders WHERE orderStatus IN ('COMPLETED', 'REJECTED', 'CANCELLED')")
    fun getPastOrdersCount(): Flow<Int>

    @Transaction
    suspend fun createOrderWithItemsAndClearCart(
        order: OrderEntity,
        items: List<OrderItemEntity>,
        clearCart: suspend () -> Unit
    ): Long {
        val orderId = insertOrder(order)
        val itemsWithId = items.map { it.copy(orderId = orderId) }
        insertOrderItems(itemsWithId)
        clearCart()
        return orderId
    }
}
