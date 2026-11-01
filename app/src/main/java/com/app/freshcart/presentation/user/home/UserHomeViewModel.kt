package com.app.freshcart.presentation.user.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.freshcart.common.Resource
import com.app.freshcart.domain.model.Category
import com.app.freshcart.domain.model.Product
import com.app.freshcart.domain.model.User
import com.app.freshcart.domain.repository.AuthRepository
import com.app.freshcart.domain.repository.CartRepository
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

data class UserHomeState(
    val user: User? = null,
    val categories: List<Category> = emptyList(),
    val popularProducts: List<Product> = emptyList(),
    val recommendedProducts: List<Product> = emptyList(),
    val allProducts: List<Product> = emptyList(),
    val cartCount: Int = 0,
    val searchQuery: String = "",
    val searchResults: List<Product> = emptyList(),
    val isSearching: Boolean = false,
    val isLoading: Boolean = false,
    val selectedProductForDetail: Product? = null
)

@HiltViewModel
class UserHomeViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val categoryRepository: CategoryRepository,
    private val productRepository: ProductRepository,
    private val cartRepository: CartRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(UserHomeState())
    val uiState: StateFlow<UserHomeState> = _uiState.asStateFlow()

    private val _snackbarEvent = MutableSharedFlow<String>()
    val snackbarEvent: SharedFlow<String> = _snackbarEvent.asSharedFlow()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            // Observe current user
            launch {
                authRepository.getCurrentUser().collect { user ->
                    _uiState.update { it.copy(user = user) }
                    user?.let { u ->
                        // Collect cart count for current user
                        cartRepository.getCartCount(u.id).collect { count ->
                            _uiState.update { it.copy(cartCount = count) }
                        }
                    }
                }
            }

            // Combine categories, products, popular, recommended
            combine(
                categoryRepository.getAllCategories(),
                productRepository.getPopularProducts(),
                productRepository.getRecommendedProducts(),
                productRepository.getAllProducts()
            ) { categories, popular, recommended, allProducts ->
                UserHomeState(
                    user = _uiState.value.user,
                    categories = categories,
                    popularProducts = popular,
                    recommendedProducts = recommended,
                    allProducts = allProducts,
                    cartCount = _uiState.value.cartCount,
                    isLoading = false
                )
            }.collect { combinedState ->
                _uiState.update { current ->
                    combinedState.copy(
                        user = current.user,
                        cartCount = current.cartCount,
                        searchQuery = current.searchQuery,
                        searchResults = current.searchResults,
                        isSearching = current.isSearching,
                        selectedProductForDetail = current.selectedProductForDetail
                    )
                }
            }
        }
    }

    fun onSearchQueryChanged(query: String) {
        val isSearching = query.isNotBlank()
        val results = if (isSearching) {
            _uiState.value.allProducts.filter {
                it.name.contains(query, ignoreCase = true) || it.description.contains(query, ignoreCase = true)
            }
        } else {
            emptyList()
        }
        _uiState.update { it.copy(searchQuery = query, isSearching = isSearching, searchResults = results) }
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
