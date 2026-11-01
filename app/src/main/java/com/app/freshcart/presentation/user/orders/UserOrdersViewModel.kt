package com.app.freshcart.presentation.user.orders

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.freshcart.common.OrderStatus
import com.app.freshcart.common.Resource
import com.app.freshcart.domain.model.Order
import com.app.freshcart.domain.model.User
import com.app.freshcart.domain.repository.AuthRepository
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

enum class UserOrderTab(val title: String) {
    ONGOING("Ongoing"),
    COMPLETED("Completed"),
    PAST("Cancelled / Rejected")
}

data class UserOrdersState(
    val user: User? = null,
    val orders: List<Order> = emptyList(),
    val filteredOrders: List<Order> = emptyList(),
    val selectedTab: UserOrderTab = UserOrderTab.ONGOING,
    val isLoading: Boolean = false,
    val selectedOrderForDetail: Order? = null,
    val isReordering: Boolean = false
)

@HiltViewModel
class UserOrdersViewModel @Inject constructor(
    private val orderRepository: OrderRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(UserOrdersState())
    val uiState: StateFlow<UserOrdersState> = _uiState.asStateFlow()

    private val _snackbarEvent = MutableSharedFlow<String>()
    val snackbarEvent: SharedFlow<String> = _snackbarEvent.asSharedFlow()

    private val _reorderSuccessEvent = MutableSharedFlow<Unit>()
    val reorderSuccessEvent: SharedFlow<Unit> = _reorderSuccessEvent.asSharedFlow()

    init {
        loadOrders()
    }

    private fun loadOrders() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            authRepository.getCurrentUser().collect { user ->
                _uiState.update { it.copy(user = user) }
                user?.let { u ->
                    orderRepository.getOrdersByUser(u.id).collect { orders ->
                        _uiState.update { current ->
                            val filtered = applyFilter(orders, current.selectedTab)
                            current.copy(
                                orders = orders,
                                filteredOrders = filtered,
                                isLoading = false
                            )
                        }
                    }
                }
            }
        }
    }

    private fun applyFilter(orders: List<Order>, tab: UserOrderTab): List<Order> {
        return when (tab) {
            UserOrderTab.ONGOING -> orders.filter {
                it.orderStatus == OrderStatus.PENDING || it.orderStatus == OrderStatus.ACCEPTED
            }
            UserOrderTab.COMPLETED -> orders.filter {
                it.orderStatus == OrderStatus.COMPLETED
            }
            UserOrderTab.PAST -> orders.filter {
                it.orderStatus == OrderStatus.REJECTED || it.orderStatus == OrderStatus.CANCELLED
            }
        }
    }

    fun selectTab(tab: UserOrderTab) {
        _uiState.update { current ->
            val filtered = applyFilter(current.orders, tab)
            current.copy(selectedTab = tab, filteredOrders = filtered)
        }
    }

    fun openOrderDetail(order: Order) {
        _uiState.update { it.copy(selectedOrderForDetail = order) }
    }

    fun dismissOrderDetail() {
        _uiState.update { it.copy(selectedOrderForDetail = null) }
    }

    fun reorder(order: Order) {
        val user = _uiState.value.user ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isReordering = true) }
            when (val result = orderRepository.reorder(user.id, order.id)) {
                is Resource.Success -> {
                    _uiState.update { it.copy(isReordering = false) }
                    _snackbarEvent.emit("Items from Order #${order.id} added to your cart with latest prices.")
                    _reorderSuccessEvent.emit(Unit)
                }
                is Resource.Error -> {
                    _uiState.update { it.copy(isReordering = false) }
                    _snackbarEvent.emit(result.message)
                }
                is Resource.Loading -> Unit
            }
        }
    }
}
