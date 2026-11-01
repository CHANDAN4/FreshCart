package com.app.freshcart.data.repository

import com.app.freshcart.common.Resource
import com.app.freshcart.data.local.dao.CategoryDao
import com.app.freshcart.data.local.entity.CategoryEntity
import com.app.freshcart.data.mapper.toDomain
import com.app.freshcart.data.mapper.toEntity
import com.app.freshcart.domain.model.Category
import com.app.freshcart.domain.repository.CategoryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CategoryRepositoryImpl @Inject constructor(
    private val categoryDao: CategoryDao
) : CategoryRepository {

    override fun getAllCategories(): Flow<List<Category>> {
        return categoryDao.getAllCategories().map { list -> list.map { it.toDomain() } }
    }

    override fun getCategoryById(id: Long): Flow<Category?> {
        return categoryDao.observeCategoryById(id).map { it?.toDomain() }
    }

    override suspend fun addCategory(category: Category): Resource<Long> {
        return try {
            val id = categoryDao.insertCategory(category.toEntity())
            Resource.Success(id)
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Failed to add category", e)
        }
    }

    override suspend fun updateCategory(category: Category): Resource<Unit> {
        return try {
            categoryDao.updateCategory(category.toEntity())
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Failed to update category", e)
        }
    }

    override suspend fun deleteCategory(categoryId: Long): Resource<Unit> {
        return try {
            categoryDao.deleteCategoryById(categoryId)
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Failed to delete category", e)
        }
    }

    override suspend fun seedInitialCategoriesIfEmpty() {
        if (categoryDao.getCategoryCount() == 0) {
            val initialCategories = listOf(
                CategoryEntity(
                    id = 1,
                    name = "Fresh Fruits",
                    description = "Organic seasonal and exotic farm fresh fruits",
                    imageUrl = "https://images.unsplash.com/photo-1610832958506-aa56368176cf?w=600"
                ),
                CategoryEntity(
                    id = 2,
                    name = "Fresh Vegetables",
                    description = "Farm fresh greens, root vegetables and herbs",
                    imageUrl = "https://images.unsplash.com/photo-1540420773420-3366772f4999?w=600"
                ),
                CategoryEntity(
                    id = 3,
                    name = "Dairy & Eggs",
                    description = "Farm fresh milk, butter, cheese, paneer and eggs",
                    imageUrl = "https://images.unsplash.com/photo-1628088062854-d1870b4553da?w=600"
                ),
                CategoryEntity(
                    id = 4,
                    name = "Bakery & Breads",
                    description = "Artisanal breads, cookies and bakery delights",
                    imageUrl = "https://images.unsplash.com/photo-1509440159596-0249088772ff?w=600"
                ),
                CategoryEntity(
                    id = 5,
                    name = "Beverages",
                    description = "Fresh juices, healthy shakes, organic tea and coffee",
                    imageUrl = "https://images.unsplash.com/photo-1534353473418-4cfa6c56fd38?w=600"
                ),
                CategoryEntity(
                    id = 6,
                    name = "Snacks & Munchies",
                    description = "Healthy nuts, roasted seeds and savory snacks",
                    imageUrl = "https://images.unsplash.com/photo-1599490659213-e2b9527bd087?w=600"
                )
            )
            categoryDao.insertCategories(initialCategories)
        }
    }
}
