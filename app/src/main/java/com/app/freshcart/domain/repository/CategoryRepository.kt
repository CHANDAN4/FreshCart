package com.app.freshcart.domain.repository

import com.app.freshcart.common.Resource
import com.app.freshcart.domain.model.Category
import kotlinx.coroutines.flow.Flow

interface CategoryRepository {
    fun getAllCategories(): Flow<List<Category>>
    fun getCategoryById(id: Long): Flow<Category?>
    suspend fun addCategory(category: Category): Resource<Long>
    suspend fun updateCategory(category: Category): Resource<Unit>
    suspend fun deleteCategory(categoryId: Long): Resource<Unit>
    suspend fun seedInitialCategoriesIfEmpty()
}
