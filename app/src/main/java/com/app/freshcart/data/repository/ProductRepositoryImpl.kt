package com.app.freshcart.data.repository

import com.app.freshcart.common.Resource
import com.app.freshcart.common.UnitType
import com.app.freshcart.data.local.dao.ProductDao
import com.app.freshcart.data.local.entity.ProductEntity
import com.app.freshcart.data.mapper.toDomain
import com.app.freshcart.data.mapper.toEntity
import com.app.freshcart.domain.model.Product
import com.app.freshcart.domain.repository.ProductRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProductRepositoryImpl @Inject constructor(
    private val productDao: ProductDao
) : ProductRepository {

    override fun getAllProducts(): Flow<List<Product>> {
        return productDao.getAllProducts().map { list -> list.map { it.toDomain() } }
    }

    override fun getProductsByCategory(categoryId: Long): Flow<List<Product>> {
        return productDao.getProductsByCategory(categoryId).map { list -> list.map { it.toDomain() } }
    }

    override fun getPopularProducts(limit: Int): Flow<List<Product>> {
        return productDao.getPopularProducts(limit).map { list -> list.map { it.toDomain() } }
    }

    override fun getRecommendedProducts(limit: Int): Flow<List<Product>> {
        return productDao.getRecommendedProducts(limit).map { list -> list.map { it.toDomain() } }
    }

    override fun getProductById(id: Long): Flow<Product?> {
        return productDao.observeProductById(id).map { it?.toDomain() }
    }

    override fun searchProducts(query: String): Flow<List<Product>> {
        return productDao.searchProducts(query).map { list -> list.map { it.toDomain() } }
    }

    override suspend fun addProduct(product: Product): Resource<Long> {
        return try {
            val id = productDao.insertProduct(product.toEntity())
            Resource.Success(id)
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Failed to add product", e)
        }
    }

    override suspend fun updateProduct(product: Product): Resource<Unit> {
        return try {
            productDao.updateProduct(product.toEntity())
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Failed to update product", e)
        }
    }

    override suspend fun deleteProduct(productId: Long): Resource<Unit> {
        return try {
            productDao.deleteProductById(productId)
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Failed to delete product", e)
        }
    }

    override suspend fun seedInitialProductsIfEmpty() {
        if (productDao.getProductCount() == 0) {
            val initialProducts = listOf(
                // Fruits (Category 1)
                ProductEntity(
                    categoryId = 1,
                    name = "Shimla Royal Apples",
                    description = "Crisp, sweet and juicy apples directly sourced from Himachal orchards.",
                    price = 180.0,
                    discountPrice = 149.0,
                    unitQuantity = 1.0,
                    unit = UnitType.KG,
                    imageUrl = "https://images.unsplash.com/photo-1560806887-1e4cd0b6cbd6?w=600",
                    isAvailable = true
                ),
                ProductEntity(
                    categoryId = 1,
                    name = "Robusta Bananas",
                    description = "Rich in potassium, fresh yellow naturally ripened bananas.",
                    price = 60.0,
                    discountPrice = 45.0,
                    unitQuantity = 1.0,
                    unit = UnitType.KG,
                    imageUrl = "https://images.unsplash.com/photo-1571771894821-ce9b6c11b08e?w=600",
                    isAvailable = true
                ),
                ProductEntity(
                    categoryId = 1,
                    name = "Nagpur Sweet Oranges",
                    description = "Juicy citrus oranges rich in Vitamin C and natural flavor.",
                    price = 120.0,
                    discountPrice = 99.0,
                    unitQuantity = 1.0,
                    unit = UnitType.KG,
                    imageUrl = "https://images.unsplash.com/photo-1582979512210-99b6a53386f9?w=600",
                    isAvailable = true
                ),
                ProductEntity(
                    categoryId = 1,
                    name = "Fresh Pomegranates",
                    description = "Deep red, ruby arils bursting with sweet antioxidants.",
                    price = 220.0,
                    discountPrice = 185.0,
                    unitQuantity = 1.0,
                    unit = UnitType.KG,
                    imageUrl = "https://images.unsplash.com/photo-1541344999736-83eca872f241?w=600",
                    isAvailable = true
                ),

                // Vegetables (Category 2)
                ProductEntity(
                    categoryId = 2,
                    name = "Fresh Farm Tomatoes",
                    description = "Plump, ripe red tomatoes ideal for curries, salads and soups.",
                    price = 40.0,
                    discountPrice = 28.0,
                    unitQuantity = 1.0,
                    unit = UnitType.KG,
                    imageUrl = "https://images.unsplash.com/photo-1592924357228-91a4daadcfea?w=600",
                    isAvailable = true
                ),
                ProductEntity(
                    categoryId = 2,
                    name = "Crisp Green Broccoli",
                    description = "Nutrient-packed fresh broccoli heads harvested this morning.",
                    price = 90.0,
                    discountPrice = 75.0,
                    unitQuantity = 500.0,
                    unit = UnitType.GRAM,
                    imageUrl = "https://images.unsplash.com/photo-1459411621453-7b03977f4bfc?w=600",
                    isAvailable = true
                ),
                ProductEntity(
                    categoryId = 2,
                    name = "Baby Spinach (Palak)",
                    description = "Tender, cleaned organic green spinach leaves rich in iron.",
                    price = 35.0,
                    discountPrice = 25.0,
                    unitQuantity = 250.0,
                    unit = UnitType.GRAM,
                    imageUrl = "https://images.unsplash.com/photo-1576045057995-568f588f82fb?w=600",
                    isAvailable = true
                ),
                ProductEntity(
                    categoryId = 2,
                    name = "Organic Potatoes",
                    description = "Firm, versatile everyday cooking staple from trusted local farms.",
                    price = 35.0,
                    discountPrice = null,
                    unitQuantity = 1.0,
                    unit = UnitType.KG,
                    imageUrl = "https://images.unsplash.com/photo-1518977676601-b53f82aba655?w=600",
                    isAvailable = true
                ),

                // Dairy & Eggs (Category 3)
                ProductEntity(
                    categoryId = 3,
                    name = "Organic Cow Milk",
                    description = "Pasteurized whole milk, rich and creamy straight from grass-fed cows.",
                    price = 72.0,
                    discountPrice = 68.0,
                    unitQuantity = 1.0,
                    unit = UnitType.LITRE,
                    imageUrl = "https://images.unsplash.com/photo-1550583724-b2692b85b150?w=600",
                    isAvailable = true
                ),
                ProductEntity(
                    categoryId = 3,
                    name = "Fresh Malai Paneer",
                    description = "Soft, melt-in-mouth cottage cheese crafted with farm-fresh milk.",
                    price = 110.0,
                    discountPrice = 95.0,
                    unitQuantity = 200.0,
                    unit = UnitType.GRAM,
                    imageUrl = "https://images.unsplash.com/photo-1631452180519-c014fe946bc7?w=600",
                    isAvailable = true
                ),
                ProductEntity(
                    categoryId = 3,
                    name = "Farm Brown Eggs (Pack of 6)",
                    description = "Free-range, high protein brown eggs from healthy hens.",
                    price = 75.0,
                    discountPrice = 64.0,
                    unitQuantity = 6.0,
                    unit = UnitType.PIECE,
                    imageUrl = "https://images.unsplash.com/photo-1506976785307-8732e854ad03?w=600",
                    isAvailable = true
                ),

                // Bakery & Breads (Category 4)
                ProductEntity(
                    categoryId = 4,
                    name = "Whole Wheat Sourdough Bread",
                    description = "Slow-fermented crusty artisan loaf made with 100% whole grain flour.",
                    price = 85.0,
                    discountPrice = 70.0,
                    unitQuantity = 400.0,
                    unit = UnitType.GRAM,
                    imageUrl = "https://images.unsplash.com/photo-1589367920969-ab8e050bbb04?w=600",
                    isAvailable = true
                ),
                ProductEntity(
                    categoryId = 4,
                    name = "Butter Croissants (Pack of 2)",
                    description = "Golden, flaky, buttery Parisian-style croissants freshly baked daily.",
                    price = 130.0,
                    discountPrice = 110.0,
                    unitQuantity = 2.0,
                    unit = UnitType.PIECE,
                    imageUrl = "https://images.unsplash.com/photo-1555507036-ab1f4038808a?w=600",
                    isAvailable = true
                ),

                // Beverages (Category 5)
                ProductEntity(
                    categoryId = 5,
                    name = "Cold-Pressed Valencia Orange Juice",
                    description = "100% pure raw cold-pressed juice with no added sugar or preservatives.",
                    price = 140.0,
                    discountPrice = 119.0,
                    unitQuantity = 1.0,
                    unit = UnitType.LITRE,
                    imageUrl = "https://images.unsplash.com/photo-1613478223719-2ab802602423?w=600",
                    isAvailable = true
                ),

                // Snacks (Category 6)
                ProductEntity(
                    categoryId = 6,
                    name = "Roasted California Almonds",
                    description = "Lightly salted, oven roasted premium jumbo almonds.",
                    price = 280.0,
                    discountPrice = 249.0,
                    unitQuantity = 250.0,
                    unit = UnitType.GRAM,
                    imageUrl = "https://images.unsplash.com/photo-1508061252224-43775270e0a4?w=600",
                    isAvailable = true
                )
            )
            productDao.insertProducts(initialProducts)
        }
    }
}
