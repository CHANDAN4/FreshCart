package com.app.freshcart.domain.repository

import com.app.freshcart.common.Resource
import com.app.freshcart.domain.model.Product
import kotlinx.coroutines.flow.Flow

interface ProductRepository {
    fun getAllProducts(): Flow<List<Product>>
    fun getProductsByCategory(categoryId: Long): Flow<List<Product>>
    fun getPopularProducts(limit: Int = 10): Flow<List<Product>>
    fun getRecommendedProducts(limit: Int = 10): Flow<List<Product>>
    fun getProductById(id: Long): Flow<Product?>
    fun searchProducts(query: String): Flow<List<Product>>
    suspend fun addProduct(product: Product): Resource<Long>
    suspend fun updateProduct(product: Product): Resource<Unit>
    suspend fun deleteProduct(productId: Long): Resource<Unit>
    suspend fun seedInitialProductsIfEmpty()
}
