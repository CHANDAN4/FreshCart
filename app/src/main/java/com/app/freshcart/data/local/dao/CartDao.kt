package com.app.freshcart.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.app.freshcart.data.local.entity.CartItemEntity
import com.app.freshcart.data.local.entity.ProductEntity
import kotlinx.coroutines.flow.Flow

data class CartItemWithProduct(
    val cartItemId: Long,
    val userId: Long,
    val quantity: Int,
    val productId: Long,
    val categoryId: Long,
    val name: String,
    val description: String,
    val price: Double,
    val discountPrice: Double?,
    val unitQuantity: Double,
    val unit: com.app.freshcart.common.UnitType,
    val imageUrl: String,
    val isAvailable: Boolean
)

@Dao
interface CartDao {
    @Query("""
        SELECT 
            c.id AS cartItemId,
            c.userId AS userId,
            c.quantity AS quantity,
            p.id AS productId,
            p.categoryId AS categoryId,
            p.name AS name,
            p.description AS description,
            p.price AS price,
            p.discountPrice AS discountPrice,
            p.unitQuantity AS unitQuantity,
            p.unit AS unit,
            p.imageUrl AS imageUrl,
            p.isAvailable AS isAvailable
        FROM cart_items c
        INNER JOIN products p ON c.productId = p.id
        WHERE c.userId = :userId
        ORDER BY c.id DESC
    """)
    fun getCartWithProductsByUser(userId: Long): Flow<List<CartItemWithProduct>>

    @Query("SELECT * FROM cart_items WHERE userId = :userId AND productId = :productId LIMIT 1")
    suspend fun getCartItem(userId: Long, productId: Long): CartItemEntity?

    @Query("SELECT COUNT(*) FROM cart_items WHERE userId = :userId")
    fun getCartCount(userId: Long): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCartItem(cartItem: CartItemEntity): Long

    @Update
    suspend fun updateCartItem(cartItem: CartItemEntity)

    @Delete
    suspend fun deleteCartItem(cartItem: CartItemEntity)

    @Query("DELETE FROM cart_items WHERE id = :cartItemId")
    suspend fun deleteCartItemById(cartItemId: Long)

    @Query("DELETE FROM cart_items WHERE userId = :userId")
    suspend fun clearCartByUser(userId: Long)

    @Query("UPDATE cart_items SET quantity = :quantity WHERE id = :cartItemId")
    suspend fun updateQuantityById(cartItemId: Long, quantity: Int)
}
