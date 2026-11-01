package com.app.freshcart.presentation.user.category

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.freshcart.common.Resource
import com.app.freshcart.domain.model.Product
import com.app.freshcart.domain.model.User
import com.app.freshcart.domain.repository.AuthRepository
import com.app.freshcart.domain.repository.CartRepository
import com.app.freshcart.domain.repository.ProductRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.net.URLDecoder
import javax.inject.Inject

data class CategoryProductsState(
    val categoryId: Long = 0,
    val categoryName: String = "",
    val products: List<Product> = emptyList(),
    val filteredProducts: List<Product> = emptyList(),
    val searchQuery: String = "",
    val isLoading: Boolean = false,
    val user: User? = null,
    val cartCount: Int = 0,
    val selectedProductForDetail: Product? = null
)

@HiltViewModel
class CategoryProductsViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val productRepository: ProductRepository,
    private val cartRepository: CartRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val categoryId: Long = savedStateHandle.get<String>("categoryId")?.toLongOrNull() ?: 0L
    private val rawCategoryName: String = savedStateHandle.get<String>("categoryName") ?: "Category"
    private val categoryName: String = try {
        URLDecoder.decode(rawCategoryName, "UTF-8")
    } catch (_: Exception) {
        rawCategoryName
    }

    private val _uiState = MutableStateFlow(
        CategoryProductsState(categoryId = categoryId, categoryName = categoryName)
    )
    val uiState: StateFlow<CategoryProductsState> = _uiState.asStateFlow()

    private val _snackbarEvent = MutableSharedFlow<String>()
    val snackbarEvent: SharedFlow<String> = _snackbarEvent.asSharedFlow()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            launch {
                authRepository.getCurrentUser().collect { user ->
                    _uiState.update { it.copy(user = user) }
                    user?.let { u ->
                        cartRepository.getCartCount(u.id).collect { count ->
                            _uiState.update { it.copy(cartCount = count) }
                        }
                    }
                }
            }

            productRepository.getProductsByCategory(categoryId).collect { products ->
                _uiState.update { current ->
                    val filtered = if (current.searchQuery.isBlank()) {
                        products
                    } else {
                        products.filter {
                            it.name.contains(current.searchQuery, ignoreCase = true) ||
                                    it.description.contains(current.searchQuery, ignoreCase = true)
                        }
                    }
                    current.copy(
                        products = products,
                        filteredProducts = filtered,
                        isLoading = false
                    )
                }
            }
        }
    }

    fun onSearchQueryChanged(query: String) {
        _uiState.update { current ->
            val filtered = if (query.isBlank()) {
                current.products
            } else {
                current.products.filter {
                    it.name.contains(query, ignoreCase = true) ||
                            it.description.contains(query, ignoreCase = true)
                }
            }
            current.copy(searchQuery = query, filteredProducts = filtered)
        }
    }

    fun openProductDetail(product: Product) {
        _uiState.update { it.copy(selectedProductForDetail = product) }
    }

    fun dismissProductDetail() {
        _uiState.update { it.copy(selectedProductForDetail = null) }
    }

    fun addToCart(product: Product, quantity: Int = 1) {
        val currentUser = _uiState.value.user ?: return
        viewModelScope.launch {
            when (val result = cartRepository.addToCart(currentUser.id, product, quantity)) {
                is Resource.Success -> {
                    _snackbarEvent.emit("Added ${product.name} to cart")
                }
                is Resource.Error -> {
                    _snackbarEvent.emit(result.message)
                }
                is Resource.Loading -> Unit
            }
        }
    }
}
