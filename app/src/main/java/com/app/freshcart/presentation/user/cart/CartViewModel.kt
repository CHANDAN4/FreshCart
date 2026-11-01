package com.app.freshcart.presentation.user.cart

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.freshcart.common.Resource
import com.app.freshcart.domain.model.CartItem
import com.app.freshcart.domain.model.User
import com.app.freshcart.domain.repository.AuthRepository
import com.app.freshcart.domain.repository.CartRepository
import com.app.freshcart.domain.repository.OrderRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CartState(
    val user: User? = null,
    val items: List<CartItem> = emptyList(),
    val isLoading: Boolean = false,
    val isPlacingOrder: Boolean = false
) {
    val itemsSubtotal: Double
        get() = items.sumOf { it.subtotal }

    // Free delivery if subtotal > 200, otherwise 40
    val deliveryFee: Double
        get() = if (items.isEmpty() || itemsSubtotal >= 200.0) 0.0 else 40.0

    val grandTotal: Double
        get() = itemsSubtotal + deliveryFee
}

@HiltViewModel
class CartViewModel @Inject constructor(
    private val cartRepository: CartRepository,
    private val orderRepository: OrderRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CartState())
    val uiState: StateFlow<CartState> = _uiState.asStateFlow()

    private val _orderPlacedEvent = MutableSharedFlow<Long>()
    val orderPlacedEvent: SharedFlow<Long> = _orderPlacedEvent.asSharedFlow()

    private val _snackbarEvent = MutableSharedFlow<String>()
    val snackbarEvent: SharedFlow<String> = _snackbarEvent.asSharedFlow()

    init {
        loadCart()
    }

    private fun loadCart() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            authRepository.getCurrentUser().collect { user ->
                _uiState.update { it.copy(user = user) }
                user?.let { u ->
                    cartRepository.getCartItems(u.id).collect { items ->
                        _uiState.update { it.copy(items = items, isLoading = false) }
                    }
                }
            }
        }
    }

    fun updateQuantity(cartItem: CartItem, newQuantity: Int) {
        val user = _uiState.value.user ?: return
        viewModelScope.launch {
            if (newQuantity <= 0) {
                cartRepository.removeFromCart(cartItem.id)
            } else {
                cartRepository.updateQuantity(cartItem.id, user.id, newQuantity)
            }
        }
    }

    fun removeItem(cartItemId: Long) {
        viewModelScope.launch {
            cartRepository.removeFromCart(cartItemId)
            _snackbarEvent.emit("Item removed from cart")
        }
    }

    fun placeOrder(onSuccess: (orderId: Long) -> Unit) {
        val state = _uiState.value
        val user = state.user ?: return

        if (state.items.isEmpty()) {
            viewModelScope.launch { _snackbarEvent.emit("Your cart is empty") }
            return
        }

        if (user.address.isBlank()) {
            viewModelScope.launch {
                _snackbarEvent.emit("Please update your delivery address in Profile before ordering.")
            }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isPlacingOrder = true) }

            val result = orderRepository.placeOrder(
                userId = user.id,
                customerName = user.name,
                customerPhone = user.phone,
                deliveryAddress = user.address,
                cartItems = state.items,
                deliveryCharge = state.deliveryFee
            )

            _uiState.update { it.copy(isPlacingOrder = false) }

            when (result) {
                is Resource.Success -> {
                    _orderPlacedEvent.emit(result.data)
                    onSuccess(result.data)
                }
                is Resource.Error -> {
                    _snackbarEvent.emit(result.message)
                }
                is Resource.Loading -> Unit
            }
        }
    }
}
