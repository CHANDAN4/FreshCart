package com.app.freshcart.data.repository

import com.app.freshcart.common.Resource
import com.app.freshcart.data.local.dao.CartDao
import com.app.freshcart.data.local.entity.CartItemEntity
import com.app.freshcart.data.mapper.toDomain
import com.app.freshcart.domain.model.CartItem
import com.app.freshcart.domain.model.Product
import com.app.freshcart.domain.repository.CartRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CartRepositoryImpl @Inject constructor(
    private val cartDao: CartDao
) : CartRepository {

    override fun getCartItems(userId: Long): Flow<List<CartItem>> {
        return cartDao.getCartWithProductsByUser(userId).map { list -> list.map { it.toDomain() } }
    }

    override fun getCartCount(userId: Long): Flow<Int> {
        return cartDao.getCartCount(userId)
    }

    override suspend fun addToCart(userId: Long, product: Product, quantity: Int): Resource<Unit> {
        return try {
            val existing = cartDao.getCartItem(userId, product.id)
            if (existing != null) {
                cartDao.updateCartItem(existing.copy(quantity = existing.quantity + quantity))
            } else {
                cartDao.insertCartItem(
                    CartItemEntity(
                        userId = userId,
                        productId = product.id,
                        quantity = quantity
                    )
                )
            }
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Failed to add to cart", e)
        }
    }

    override suspend fun updateQuantity(cartItemId: Long, userId: Long, newQuantity: Int): Resource<Unit> {
        return try {
            if (newQuantity <= 0) {
                cartDao.deleteCartItemById(cartItemId)
            } else {
                cartDao.updateQuantityById(cartItemId, newQuantity)
            }
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Failed to update quantity", e)
        }
    }

    override suspend fun removeFromCart(cartItemId: Long): Resource<Unit> {
        return try {
            cartDao.deleteCartItemById(cartItemId)
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Failed to remove item", e)
        }
    }

    override suspend fun clearCart(userId: Long): Resource<Unit> {
        return try {
            cartDao.clearCartByUser(userId)
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Failed to clear cart", e)
        }
    }
}
