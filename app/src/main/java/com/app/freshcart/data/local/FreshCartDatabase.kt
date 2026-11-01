package com.app.freshcart.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.app.freshcart.data.local.dao.CartDao
import com.app.freshcart.data.local.dao.CategoryDao
import com.app.freshcart.data.local.dao.OrderDao
import com.app.freshcart.data.local.dao.ProductDao
import com.app.freshcart.data.local.dao.UserDao
import com.app.freshcart.data.local.entity.CartItemEntity
import com.app.freshcart.data.local.entity.CategoryEntity
import com.app.freshcart.data.local.entity.OrderEntity
import com.app.freshcart.data.local.entity.OrderItemEntity
import com.app.freshcart.data.local.entity.ProductEntity
import com.app.freshcart.data.local.entity.UserEntity

@Database(
    entities = [
        UserEntity::class,
        CategoryEntity::class,
        ProductEntity::class,
        CartItemEntity::class,
        OrderEntity::class,
        OrderItemEntity::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class FreshCartDatabase : RoomDatabase() {
    abstract val userDao: UserDao
    abstract val categoryDao: CategoryDao
    abstract val productDao: ProductDao
    abstract val cartDao: CartDao
    abstract val orderDao: OrderDao
}
