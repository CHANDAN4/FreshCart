package com.app.freshcart.presentation.admin.orders

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.freshcart.common.OrderStatus
import com.app.freshcart.common.PaymentStatus
import com.app.freshcart.common.Resource
import com.app.freshcart.domain.model.Order
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

enum class AdminOrderFilter(val title: String) {
    ALL("All"),
    PENDING("Pending"),
    ONGOING("Ongoing"),
    COMPLETED("Completed"),
    REJECTED("Rejected")
}

data class AdminOrdersState(
    val allOrders: List<Order> = emptyList(),
    val filteredOrders: List<Order> = emptyList(),
    val selectedFilter: AdminOrderFilter = AdminOrderFilter.ALL,
    val isLoading: Boolean = false,
    val rejectingOrder: Order? = null,
    val unconfirmedPaymentNoticeOrder: Order? = null
)

@HiltViewModel
class AdminOrdersViewModel @Inject constructor(
    private val orderRepository: OrderRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AdminOrdersState())
    val uiState: StateFlow<AdminOrdersState> = _uiState.asStateFlow()

    private val _snackbarEvent = MutableSharedFlow<String>()
    val snackbarEvent: SharedFlow<String> = _snackbarEvent.asSharedFlow()

    init {
        loadOrders()
    }

    private fun loadOrders() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            orderRepository.getAllOrders().collect { orders ->
                _uiState.update { current ->
                    val filtered = applyFilter(orders, current.selectedFilter)
                    current.copy(
                        allOrders = orders,
                        filteredOrders = filtered,
                        isLoading = false
                    )
                }
            }
        }
    }

    private fun applyFilter(orders: List<Order>, filter: AdminOrderFilter): List<Order> {
        return when (filter) {
            AdminOrderFilter.ALL -> orders
            AdminOrderFilter.PENDING -> orders.filter { it.orderStatus == OrderStatus.PENDING }
            AdminOrderFilter.ONGOING -> orders.filter { it.orderStatus == OrderStatus.ACCEPTED }
            AdminOrderFilter.COMPLETED -> orders.filter { it.orderStatus == OrderStatus.COMPLETED }
            AdminOrderFilter.REJECTED -> orders.filter {
                it.orderStatus == OrderStatus.REJECTED || it.orderStatus == OrderStatus.CANCELLED
            }
        }
    }

    fun selectFilter(filter: AdminOrderFilter) {
        _uiState.update { current ->
            val filtered = applyFilter(current.allOrders, filter)
            current.copy(selectedFilter = filter, filteredOrders = filtered)
        }
    }

    fun confirmPaymentReceived(orderId: Long) {
        viewModelScope.launch {
            when (val result = orderRepository.markPaymentConfirmed(orderId)) {
                is Resource.Success -> {
                    _snackbarEvent.emit("Payment confirmed for Order #$orderId")
                }
                is Resource.Error -> {
                    _snackbarEvent.emit(result.message)
                }
                is Resource.Loading -> Unit
            }
        }
    }

    fun onAcceptClicked(order: Order) {
        if (order.paymentStatus != PaymentStatus.CONFIRMED) {
            _uiState.update { it.copy(unconfirmedPaymentNoticeOrder = order) }
            return
        }

        viewModelScope.launch {
            when (val result = orderRepository.acceptOrder(order.id)) {
                is Resource.Success -> {
                    _snackbarEvent.emit("Order #${order.id} accepted and moved to Ongoing Orders.")
                }
                is Resource.Error -> {
                    _snackbarEvent.emit(result.message)
                }
                is Resource.Loading -> Unit
            }
        }
    }

    fun dismissPaymentNotice() {
        _uiState.update { it.copy(unconfirmedPaymentNoticeOrder = null) }
    }

    fun openRejectDialog(order: Order) {
        _uiState.update { it.copy(rejectingOrder = order) }
    }

    fun dismissRejectDialog() {
        _uiState.update { it.copy(rejectingOrder = null) }
    }

    fun rejectOrder(reason: String) {
        val order = _uiState.value.rejectingOrder ?: return
        if (reason.trim().isBlank()) {
            viewModelScope.launch { _snackbarEvent.emit("Please enter a rejection reason.") }
            return
        }

        viewModelScope.launch {
            when (val result = orderRepository.rejectOrder(order.id, reason)) {
                is Resource.Success -> {
                    _snackbarEvent.emit("Order #${order.id} has been rejected.")
                    dismissRejectDialog()
                }
                is Resource.Error -> {
                    _snackbarEvent.emit(result.message)
                }
                is Resource.Loading -> Unit
            }
        }
    }

    fun markOrderDelivered(orderId: Long) {
        viewModelScope.launch {
            when (val result = orderRepository.completeOrder(orderId)) {
                is Resource.Success -> {
                    _snackbarEvent.emit("Order #$orderId marked as Delivered / Completed.")
                }
                is Resource.Error -> {
                    _snackbarEvent.emit(result.message)
                }
                is Resource.Loading -> Unit
            }
        }
    }
}
