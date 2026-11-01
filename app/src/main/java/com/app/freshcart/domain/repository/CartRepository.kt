package com.app.freshcart.domain.repository

import com.app.freshcart.common.Resource
import com.app.freshcart.domain.model.CartItem
import com.app.freshcart.domain.model.Product
import kotlinx.coroutines.flow.Flow

interface CartRepository {
    fun getCartItems(userId: Long): Flow<List<CartItem>>
    fun getCartCount(userId: Long): Flow<Int>
    suspend fun addToCart(userId: Long, product: Product, quantity: Int): Resource<Unit>
    suspend fun updateQuantity(cartItemId: Long, userId: Long, newQuantity: Int): Resource<Unit>
    suspend fun removeFromCart(cartItemId: Long): Resource<Unit>
    suspend fun clearCart(userId: Long): Resource<Unit>
}
