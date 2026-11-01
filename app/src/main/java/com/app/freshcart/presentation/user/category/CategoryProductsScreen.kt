package com.app.freshcart.presentation.user.category

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.app.freshcart.presentation.common.EmptyStateView
import com.app.freshcart.presentation.common.FreshTextField
import com.app.freshcart.presentation.common.LoadingView
import com.app.freshcart.presentation.user.dialog.ProductDetailBottomSheet
import com.app.freshcart.presentation.user.home.ProductCard
import com.app.freshcart.ui.theme.FreshGreenDark
import com.app.freshcart.ui.theme.FreshOrangeAccent

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryProductsScreen(
    onNavigateBack: () -> Unit,
    onNavigateToCart: () -> Unit,
    viewModel: CategoryProductsViewModel = hiltViewModel()
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
                        text = state.categoryName,
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
                actions = {
                    IconButton(onClick = onNavigateToCart) {
                        BadgedBox(
                            badge = {
                                if (state.cartCount > 0) {
                                    Badge(
                                        containerColor = FreshOrangeAccent,
                                        contentColor = Color.White
                                    ) {
                                        Text(text = "${state.cartCount}", fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.ShoppingCart,
                                contentDescription = "Cart",
                                tint = FreshGreenDark
                            )
                        }
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
            // Search inside category
            Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                FreshTextField(
                    value = state.searchQuery,
                    onValueChange = { viewModel.onSearchQueryChanged(it) },
                    placeholder = "Search in ${state.categoryName}...",
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            if (state.isLoading && state.products.isEmpty()) {
                LoadingView(message = "Loading items...")
            } else if (state.filteredProducts.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    EmptyStateView(
                        title = "No products found",
                        message = if (state.searchQuery.isNotBlank()) "No items match '${state.searchQuery}'" else "There are no products listed in this category yet."
                    )
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(state.filteredProducts, key = { it.id }) { product ->
                        ProductCard(
                            product = product,
                            onClick = { viewModel.openProductDetail(product) },
                            onAddToCart = { viewModel.addToCart(product, 1) },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }

        // Product Detail Bottom Sheet
        state.selectedProductForDetail?.let { product ->
            ProductDetailBottomSheet(
                product = product,
                onDismiss = { viewModel.dismissProductDetail() },
                onAddToCart = { qty -> viewModel.addToCart(product, qty) }
            )
        }
    }
}
