package com.app.freshcart.di

import com.app.freshcart.data.repository.AuthRepositoryImpl
import com.app.freshcart.data.repository.CartRepositoryImpl
import com.app.freshcart.data.repository.CategoryRepositoryImpl
import com.app.freshcart.data.repository.OrderRepositoryImpl
import com.app.freshcart.data.repository.ProductRepositoryImpl
import com.app.freshcart.domain.repository.AuthRepository
import com.app.freshcart.domain.repository.CartRepository
import com.app.freshcart.domain.repository.CategoryRepository
import com.app.freshcart.domain.repository.OrderRepository
import com.app.freshcart.domain.repository.ProductRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindAuthRepository(
        impl: AuthRepositoryImpl
    ): AuthRepository

    @Binds
    @Singleton
    abstract fun bindCategoryRepository(
        impl: CategoryRepositoryImpl
    ): CategoryRepository

    @Binds
    @Singleton
    abstract fun bindProductRepository(
        impl: ProductRepositoryImpl
    ): ProductRepository

    @Binds
    @Singleton
    abstract fun bindCartRepository(
        impl: CartRepositoryImpl
    ): CartRepository

    @Binds
    @Singleton
    abstract fun bindOrderRepository(
        impl: OrderRepositoryImpl
    ): OrderRepository
}
