package com.app.freshcart.di

import android.content.Context
import androidx.room.Room
import com.app.freshcart.common.Constants
import com.app.freshcart.data.local.FreshCartDatabase
import com.app.freshcart.data.local.dao.CartDao
import com.app.freshcart.data.local.dao.CategoryDao
import com.app.freshcart.data.local.dao.OrderDao
import com.app.freshcart.data.local.dao.ProductDao
import com.app.freshcart.data.local.dao.UserDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideFreshCartDatabase(
        @ApplicationContext context: Context
    ): FreshCartDatabase {
        return Room.databaseBuilder(
            context,
            FreshCartDatabase::class.java,
            Constants.DATABASE_NAME
        ).fallbackToDestructiveMigration().build()
    }

    @Provides
    @Singleton
    fun provideUserDao(database: FreshCartDatabase): UserDao = database.userDao

    @Provides
    @Singleton
    fun provideCategoryDao(database: FreshCartDatabase): CategoryDao = database.categoryDao

    @Provides
    @Singleton
    fun provideProductDao(database: FreshCartDatabase): ProductDao = database.productDao

    @Provides
    @Singleton
    fun provideCartDao(database: FreshCartDatabase): CartDao = database.cartDao

    @Provides
    @Singleton
    fun provideOrderDao(database: FreshCartDatabase): OrderDao = database.orderDao
}
