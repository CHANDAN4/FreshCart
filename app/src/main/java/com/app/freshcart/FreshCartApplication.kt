package com.app.freshcart

import android.app.Application
import com.app.freshcart.domain.repository.AuthRepository
import com.app.freshcart.domain.repository.CategoryRepository
import com.app.freshcart.domain.repository.ProductRepository
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltAndroidApp
class FreshCartApplication : Application() {

    @Inject
    lateinit var authRepository: AuthRepository

    @Inject
    lateinit var categoryRepository: CategoryRepository

    @Inject
    lateinit var productRepository: ProductRepository

    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()
        applicationScope.launch {
            authRepository.seedInitialUsersIfEmpty()
            categoryRepository.seedInitialCategoriesIfEmpty()
            productRepository.seedInitialProductsIfEmpty()
        }
    }
}
