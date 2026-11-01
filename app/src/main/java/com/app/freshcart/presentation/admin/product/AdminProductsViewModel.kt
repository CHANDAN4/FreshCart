package com.app.freshcart.presentation.admin.product

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.freshcart.common.Resource
import com.app.freshcart.common.UnitType
import com.app.freshcart.domain.model.Category
import com.app.freshcart.domain.model.Product
import com.app.freshcart.domain.repository.CategoryRepository
import com.app.freshcart.domain.repository.ProductRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AdminProductsState(
    val products: List<Product> = emptyList(),
    val filteredProducts: List<Product> = emptyList(),
    val categories: List<Category> = emptyList(),
    val selectedCategoryId: Long? = null,
    val searchQuery: String = "",
    val isLoading: Boolean = false,
    val selectedProduct: Product? = null,
    val isAddEditOpen: Boolean = false,
    val deleteConfirmationProduct: Product? = null
)

@HiltViewModel
class AdminProductsViewModel @Inject constructor(
    private val productRepository: ProductRepository,
    private val categoryRepository: CategoryRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AdminProductsState())
    val uiState: StateFlow<AdminProductsState> = _uiState.asStateFlow()

    private val _snackbarEvent = MutableSharedFlow<String>()
    val snackbarEvent: SharedFlow<String> = _snackbarEvent.asSharedFlow()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            combine(
                productRepository.getAllProducts(),
                categoryRepository.getAllCategories()
            ) { products, categories ->
                Pair(products, categories)
            }.collect { (products, categories) ->
                _uiState.update { current ->
                    val filtered = applyFilter(products, current.selectedCategoryId, current.searchQuery)
                    current.copy(
                        products = products,
                        filteredProducts = filtered,
                        categories = categories,
                        isLoading = false
                    )
                }
            }
        }
    }

    private fun applyFilter(products: List<Product>, categoryId: Long?, query: String): List<Product> {
        return products.filter { product ->
            val matchesCategory = categoryId == null || product.categoryId == categoryId
            val matchesQuery = query.isBlank() || product.name.contains(query, ignoreCase = true) ||
                    product.description.contains(query, ignoreCase = true)
            matchesCategory && matchesQuery
        }
    }

    fun selectCategory(categoryId: Long?) {
        _uiState.update { current ->
            val filtered = applyFilter(current.products, categoryId, current.searchQuery)
            current.copy(selectedCategoryId = categoryId, filteredProducts = filtered)
        }
    }

    fun onSearchQueryChanged(query: String) {
        _uiState.update { current ->
            val filtered = applyFilter(current.products, current.selectedCategoryId, query)
            current.copy(searchQuery = query, filteredProducts = filtered)
        }
    }

    fun openAddProduct() {
        _uiState.update { it.copy(selectedProduct = null, isAddEditOpen = true) }
    }

    fun openEditProduct(product: Product) {
        _uiState.update { it.copy(selectedProduct = product, isAddEditOpen = true) }
    }

    fun dismissAddEdit() {
        _uiState.update { it.copy(selectedProduct = null, isAddEditOpen = false) }
    }

    fun saveProduct(
        name: String,
        description: String,
        categoryId: Long,
        price: Double,
        discountPrice: Double?,
        unitQuantity: Double,
        unit: UnitType,
        imageUrl: String,
        isAvailable: Boolean
    ) {
        val trimmedName = name.trim()
        if (trimmedName.isBlank()) {
            viewModelScope.launch { _snackbarEvent.emit("Product name cannot be empty") }
            return
        }
        if (price <= 0.0) {
            viewModelScope.launch { _snackbarEvent.emit("Price must be greater than zero") }
            return
        }

        viewModelScope.launch {
            val selected = _uiState.value.selectedProduct
            val finalImage = imageUrl.trim().ifBlank {
                "https://images.unsplash.com/photo-1542838132-92c53300491e?w=500&q=80"
            }

            if (selected == null) {
                val newProduct = Product(
                    id = 0,
                    categoryId = categoryId,
                    name = trimmedName,
                    description = description.trim(),
                    price = price,
                    discountPrice = discountPrice,
                    unitQuantity = unitQuantity,
                    unit = unit,
                    imageUrl = finalImage,
                    isAvailable = isAvailable
                )
                when (val result = productRepository.addProduct(newProduct)) {
                    is Resource.Success -> {
                        _snackbarEvent.emit("Product '$trimmedName' added successfully")
                        dismissAddEdit()
                    }
                    is Resource.Error -> {
                        _snackbarEvent.emit(result.message)
                    }
                    is Resource.Loading -> Unit
                }
            } else {
                val updatedProduct = selected.copy(
                    categoryId = categoryId,
                    name = trimmedName,
                    description = description.trim(),
                    price = price,
                    discountPrice = discountPrice,
                    unitQuantity = unitQuantity,
                    unit = unit,
                    imageUrl = finalImage,
                    isAvailable = isAvailable
                )
                when (val result = productRepository.updateProduct(updatedProduct)) {
                    is Resource.Success -> {
                        _snackbarEvent.emit("Product updated successfully")
                        dismissAddEdit()
                    }
                    is Resource.Error -> {
                        _snackbarEvent.emit(result.message)
                    }
                    is Resource.Loading -> Unit
                }
            }
        }
    }

    fun confirmDeleteProduct(product: Product) {
        _uiState.update { it.copy(deleteConfirmationProduct = product) }
    }

    fun dismissDeleteConfirmation() {
        _uiState.update { it.copy(deleteConfirmationProduct = null) }
    }

    fun deleteProduct() {
        val productToDelete = _uiState.value.deleteConfirmationProduct ?: return
        viewModelScope.launch {
            when (val result = productRepository.deleteProduct(productToDelete.id)) {
                is Resource.Success -> {
                    _snackbarEvent.emit("Product '${productToDelete.name}' deleted")
                    dismissDeleteConfirmation()
                }
                is Resource.Error -> {
                    _snackbarEvent.emit(result.message)
                }
                is Resource.Loading -> Unit
            }
        }
    }
}
