package com.app.freshcart.presentation.admin.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.freshcart.domain.model.Order
import com.app.freshcart.domain.model.User
import com.app.freshcart.domain.repository.AuthRepository
import com.app.freshcart.domain.repository.OrderRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AdminDashboardState(
    val ongoingOrdersCount: Int = 0,
    val completedOrdersCount: Int = 0,
    val pastOrdersCount: Int = 0,
    val totalOrdersCount: Int = 0,
    val recentOrders: List<Order> = emptyList(),
    val adminUser: User? = null,
    val isLoading: Boolean = false
)

@HiltViewModel
class AdminDashboardViewModel @Inject constructor(
    private val orderRepository: OrderRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AdminDashboardState())
    val uiState: StateFlow<AdminDashboardState> = _uiState.asStateFlow()

    init {
        loadDashboardData()
    }

    private fun loadDashboardData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            launch {
                authRepository.getCurrentUser().collect { user ->
                    _uiState.update { it.copy(adminUser = user) }
                }
            }

            combine(
                orderRepository.getOngoingOrdersCount(),
                orderRepository.getCompletedOrdersCount(),
                orderRepository.getPastOrdersCount(),
                orderRepository.getAllOrders()
            ) { ongoing, completed, past, allOrders ->
                AdminDashboardState(
                    ongoingOrdersCount = ongoing,
                    completedOrdersCount = completed,
                    pastOrdersCount = past,
                    totalOrdersCount = allOrders.size,
                    recentOrders = allOrders.take(5),
                    adminUser = _uiState.value.adminUser,
                    isLoading = false
                )
            }.collect { newState ->
                _uiState.update {
                    newState.copy(adminUser = it.adminUser)
                }
            }
        }
    }

    fun logout(onLoggedOut: () -> Unit) {
        viewModelScope.launch {
            authRepository.logout()
            onLoggedOut()
        }
    }
}
