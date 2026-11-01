package com.app.freshcart.presentation.admin.orders

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.app.freshcart.common.Formatters
import com.app.freshcart.common.OrderStatus
import com.app.freshcart.common.PaymentStatus
import com.app.freshcart.domain.model.Order
import com.app.freshcart.presentation.common.EmptyStateView
import com.app.freshcart.presentation.common.FreshButton
import com.app.freshcart.presentation.common.FreshOutlinedButton
import com.app.freshcart.presentation.common.LoadingView
import com.app.freshcart.presentation.common.OrderStatusBadge
import com.app.freshcart.presentation.common.PaymentStatusBadge
import com.app.freshcart.ui.theme.FreshGreenDark
import com.app.freshcart.ui.theme.FreshGreenPrimary
import com.app.freshcart.ui.theme.FreshOrangeAccent
import com.app.freshcart.ui.theme.FreshSurfaceVariant
import com.app.freshcart.ui.theme.StatusError

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminOrdersScreen(
    onNavigateBack: () -> Unit,
    viewModel: AdminOrdersViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(key1 = true) {
        viewModel.snackbarEvent.collect { message ->
            snackbarHostState.showSnackbar(message)
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Order Management",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = FreshGreenDark
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = FreshGreenDark
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Filter Tabs
            ScrollableTabRow(
                selectedTabIndex = state.selectedFilter.ordinal,
                edgePadding = 16.dp,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = FreshGreenPrimary
            ) {
                AdminOrderFilter.entries.forEach { filter ->
                    Tab(
                        selected = state.selectedFilter == filter,
                        onClick = { viewModel.selectFilter(filter) },
                        text = {
                            Text(
                                text = filter.name.lowercase().replaceFirstChar { it.uppercase() },
                                fontWeight = if (state.selectedFilter == filter) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    )
                }
            }

            if (state.isLoading && state.allOrders.isEmpty()) {
                LoadingView(message = "Loading orders...")
            } else if (state.filteredOrders.isEmpty()) {
                EmptyStateView(
                    title = "No ${state.selectedFilter.name.lowercase()} orders",
                    message = "Orders in this category will appear here."
                )
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(state.filteredOrders, key = { it.id }) { order ->
                        AdminOrderDetailCard(
                            order = order,
                            onConfirmPayment = { viewModel.confirmPaymentReceived(order.id) },
                            onAccept = { viewModel.onAcceptClicked(order) },
                            onReject = { viewModel.openRejectDialog(order) },
                            onDeliver = { viewModel.markOrderDelivered(order.id) }
                        )
                    }
                    item {
                        Spacer(modifier = Modifier.height(24.dp))
                    }
                }
            }
        }

        // Unconfirmed Payment Notice Dialog
        state.unconfirmedPaymentNoticeOrder?.let { order ->
            AlertDialog(
                onDismissRequest = { viewModel.dismissPaymentNotice() },
                icon = {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = FreshOrangeAccent,
                        modifier = Modifier.size(32.dp)
                    )
                },
                title = {
                    Text(
                        text = "Payment Not Confirmed",
                        fontWeight = FontWeight.Bold
                    )
                },
                text = {
                    Text(
                        text = "Business Rule Notice: Order #${order.id} cannot be accepted yet because the payment has not been marked as received.\n\nPlease confirm cash on delivery / QR payment receipt before accepting this order."
                    )
                },
                confirmButton = {
                    FreshButton(
                        text = "Confirm Payment Now",
                        onClick = {
                            viewModel.confirmPaymentReceived(order.id)
                            viewModel.dismissPaymentNotice()
                        },
                        modifier = Modifier.width(180.dp)
                    )
                },
                dismissButton = {
                    TextButton(onClick = { viewModel.dismissPaymentNotice() }) {
                        Text("Cancel")
                    }
                }
            )
        }

        // Rejection Reason Dialog
        state.rejectingOrder?.let { order ->
            var reason by remember { mutableStateOf("") }
            AlertDialog(
                onDismissRequest = { viewModel.dismissRejectDialog() },
                icon = {
                    Icon(
                        imageVector = Icons.Default.Cancel,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error
                    )
                },
                title = { Text("Reject Order #${order.id}") },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Please provide a reason for rejecting this order:")
                        OutlinedTextField(
                            value = reason,
                            onValueChange = { reason = it },
                            label = { Text("Rejection reason *") },
                            placeholder = { Text("e.g. Out of delivery area, customer cancelled") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            maxLines = 3
                        )
                    }
                },
                confirmButton = {
                    TextButton(
                        onClick = { viewModel.rejectOrder(reason) },
                        enabled = reason.isNotBlank()
                    ) {
                        Text("Reject Order", color = MaterialTheme.colorScheme.error)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { viewModel.dismissRejectDialog() }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}

@Composable
private fun AdminOrderDetailCard(
    order: Order,
    onConfirmPayment: () -> Unit,
    onAccept: () -> Unit,
    onReject: () -> Unit,
    onDeliver: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header: ID & Date
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Order #${order.id}",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = FreshGreenDark
                )
                Text(
                    text = Formatters.formatDateTime(order.createdAt),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Badges row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                PaymentStatusBadge(status = order.paymentStatus)
                OrderStatusBadge(status = order.orderStatus)
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
            Spacer(modifier = Modifier.height(10.dp))

            // Customer Info
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    tint = FreshGreenPrimary,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "${order.customerName} (${order.customerPhone})",
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Row(verticalAlignment = Alignment.Top) {
                Icon(
                    imageVector = Icons.Default.Place,
                    contentDescription = null,
                    tint = FreshGreenPrimary,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = order.deliveryAddress,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Product items summary
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(FreshSurfaceVariant, RoundedCornerShape(10.dp))
                    .padding(10.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "Order Items (${order.items.size}):",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    order.items.forEach { item ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "• ${item.productName} × ${item.quantity}",
                                style = MaterialTheme.typography.bodySmall
                            )
                            Text(
                                text = Formatters.formatPrice(item.subtotal),
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Payment method & Total
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Payments,
                        contentDescription = null,
                        tint = FreshOrangeAccent,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Cash on Delivery (COD)",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium)
                    )
                }

                Text(
                    text = "Total: ${Formatters.formatPrice(order.totalAmount)}",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = FreshGreenDark
                    )
                )
            }

            // Rejection reason notice if present
            order.rejectionReason?.let { reason ->
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Rejection Reason: $reason",
                    style = MaterialTheme.typography.bodySmall,
                    color = StatusError,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Admin Action Buttons
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                // Confirm Payment Button (if still pending)
                if (order.paymentStatus != PaymentStatus.CONFIRMED && order.orderStatus != OrderStatus.REJECTED && order.orderStatus != OrderStatus.CANCELLED) {
                    FreshButton(
                        text = "Confirm COD Payment Received",
                        onClick = onConfirmPayment,
                        containerColor = FreshOrangeAccent,
                        modifier = Modifier.height(44.dp)
                    )
                }

                // Accept & Reject for Pending orders
                if (order.orderStatus == OrderStatus.PENDING) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        FreshOutlinedButton(
                            text = "Reject",
                            onClick = onReject,
                            contentColor = MaterialTheme.colorScheme.error,
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp)
                        )
                        FreshButton(
                            text = "Accept Order",
                            onClick = onAccept,
                            containerColor = FreshGreenPrimary,
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp)
                        )
                    }
                }

                // Mark Delivered for Accepted (Ongoing) orders
                if (order.orderStatus == OrderStatus.ACCEPTED) {
                    FreshButton(
                        text = "Mark as Delivered / Completed",
                        onClick = onDeliver,
                        containerColor = FreshGreenPrimary,
                        modifier = Modifier.height(44.dp)
                    )
                }
            }
        }
    }
}
