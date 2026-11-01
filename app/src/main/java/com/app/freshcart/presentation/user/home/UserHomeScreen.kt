package com.app.freshcart.presentation.user.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.app.freshcart.common.Formatters
import com.app.freshcart.domain.model.Category
import com.app.freshcart.domain.model.Product
import com.app.freshcart.presentation.common.EmptyStateView
import com.app.freshcart.presentation.common.FreshTextField
import com.app.freshcart.presentation.common.LoadingView
import com.app.freshcart.presentation.user.dialog.ProductDetailBottomSheet
import com.app.freshcart.ui.theme.FreshCardBorder
import com.app.freshcart.ui.theme.FreshGreenContainer
import com.app.freshcart.ui.theme.FreshGreenDark
import com.app.freshcart.ui.theme.FreshGreenLight
import com.app.freshcart.ui.theme.FreshGreenPrimary
import com.app.freshcart.ui.theme.FreshOrangeAccent
import com.app.freshcart.ui.theme.FreshOrangeContainer
import com.app.freshcart.ui.theme.FreshSurfaceVariant

@Composable
fun UserHomeScreen(
    onNavigateToCategory: (categoryId: Long, categoryName: String) -> Unit,
    onNavigateToCart: () -> Unit,
    onNavigateToProfile: () -> Unit,
    viewModel: UserHomeViewModel = hiltViewModel()
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
            UserHomeHeader(
                address = state.user?.address ?: "Select delivery location",
                userName = state.user?.name ?: "Customer",
                cartCount = state.cartCount,
                onCartClick = onNavigateToCart,
                onProfileClick = onNavigateToProfile
            )
        }
    ) { paddingValues ->
        if (state.isLoading && state.categories.isEmpty() && state.allProducts.isEmpty()) {
            LoadingView(message = "Loading fresh groceries...")
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Search Field
                item {
                    Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)) {
                        FreshTextField(
                            value = state.searchQuery,
                            onValueChange = { viewModel.onSearchQueryChanged(it) },
                            placeholder = "Search fresh fruits, vegetables, milk...",
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
                }

                if (state.isSearching) {
                    // Search Results List
                    item {
                        Text(
                            text = "Search Results (${state.searchResults.size})",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                    }
                    if (state.searchResults.isEmpty()) {
                        item {
                            EmptyStateView(
                                title = "No items match '${state.searchQuery}'",
                                message = "Try searching for something else like apples, milk, or bread."
                            )
                        }
                    } else {
                        items(state.searchResults, key = { it.id }) { product ->
                            Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                                ProductListRowItem(
                                    product = product,
                                    onClick = { viewModel.openProductDetail(product) },
                                    onAddToCart = { viewModel.addToCart(product, 1) }
                                )
                            }
                        }
                    }
                } else {
                    // Promotional Banners Carousel
                    item {
                        PromotionalBannersSection()
                    }

                    // Categories Carousel
                    item {
                        SectionHeader(
                            title = "Categories",
                            subtitle = "Explore fresh produce",
                            onSeeAll = null
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        LazyRow(
                            contentPadding = PaddingValues(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            items(state.categories, key = { it.id }) { category ->
                                CategoryPillCard(
                                    category = category,
                                    onClick = { onNavigateToCategory(category.id, category.name) }
                                )
                            }
                        }
                    }

                    // Popular Products Carousel
                    if (state.popularProducts.isNotEmpty()) {
                        item {
                            SectionHeader(
                                title = "Popular Products",
                                subtitle = "Top picks this week",
                                onSeeAll = null
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            LazyRow(
                                contentPadding = PaddingValues(horizontal = 16.dp),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                items(state.popularProducts, key = { it.id }) { product ->
                                    ProductCard(
                                        product = product,
                                        onClick = { viewModel.openProductDetail(product) },
                                        onAddToCart = { viewModel.addToCart(product, 1) }
                                    )
                                }
                            }
                        }
                    }

                    // Recommended Products
                    if (state.recommendedProducts.isNotEmpty()) {
                        item {
                            SectionHeader(
                                title = "Recommended for You",
                                subtitle = "Handpicked farm fresh items",
                                onSeeAll = null
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            LazyRow(
                                contentPadding = PaddingValues(horizontal = 16.dp),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                items(state.recommendedProducts, key = { it.id }) { product ->
                                    ProductCard(
                                        product = product,
                                        onClick = { viewModel.openProductDetail(product) },
                                        onAddToCart = { viewModel.addToCart(product, 1) }
                                    )
                                }
                            }
                        }
                    }

                    // Products Grouped by Category
                    state.categories.forEach { category ->
                        val categoryProducts = state.allProducts.filter { it.categoryId == category.id }
                        if (categoryProducts.isNotEmpty()) {
                            item {
                                SectionHeader(
                                    title = category.name,
                                    subtitle = category.description.ifBlank { "Fresh items" },
                                    onSeeAll = { onNavigateToCategory(category.id, category.name) }
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                LazyRow(
                                    contentPadding = PaddingValues(horizontal = 16.dp),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    items(categoryProducts, key = { it.id }) { product ->
                                        ProductCard(
                                            product = product,
                                            onClick = { viewModel.openProductDetail(product) },
                                            onAddToCart = { viewModel.addToCart(product, 1) }
                                        )
                                    }
                                }
                            }
                        }
                    }

                    item {
                        Spacer(modifier = Modifier.height(24.dp))
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

@Composable
private fun UserHomeHeader(
    address: String,
    userName: String,
    cartCount: Int,
    onCartClick: () -> Unit,
    onProfileClick: () -> Unit
) {
    SurfaceHeader {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Location & Greeting
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = "Delivery Location",
                        tint = FreshGreenPrimary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "DELIVER TO",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = FreshGreenPrimary,
                            letterSpacing = 0.8.sp
                        )
                    )
                }
                Text(
                    text = address,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Cart & Profile Actions
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                IconButton(onClick = onCartClick) {
                    BadgedBox(
                        badge = {
                            if (cartCount > 0) {
                                Badge(
                                    containerColor = FreshOrangeAccent,
                                    contentColor = Color.White
                                ) {
                                    Text(text = "$cartCount", fontWeight = FontWeight.Bold)
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

                IconButton(onClick = onProfileClick) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(FreshGreenContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "Profile",
                            tint = FreshGreenPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SurfaceHeader(content: @Composable () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
    ) {
        content()
    }
}

@Composable
private fun SectionHeader(
    title: String,
    subtitle: String,
    onSeeAll: (() -> Unit)?
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        if (onSeeAll != null) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.clickable { onSeeAll() }
            ) {
                Text(
                    text = "See all",
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontWeight = FontWeight.SemiBold,
                        color = FreshGreenPrimary
                    )
                )
                Spacer(modifier = Modifier.width(2.dp))
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = null,
                    tint = FreshGreenPrimary,
                    modifier = Modifier.size(14.dp)
                )
            }
        }
    }
}

@Composable
private fun PromotionalBannersSection() {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            BannerCard(
                headline = "Fresh Farm Veggies",
                subhead = "Up to 30% OFF on all green greens",
                brush = Brush.horizontalGradient(listOf(FreshGreenDark, FreshGreenPrimary)),
                imageUrl = "https://images.unsplash.com/photo-1540420773420-3366772f4999?w=500&q=80"
            )
        }
        item {
            BannerCard(
                headline = "Daily Fresh Dairy",
                subhead = "Pure milk, butter & artisanal curd",
                brush = Brush.horizontalGradient(listOf(Color(0xFFE67E22), FreshOrangeAccent)),
                imageUrl = "https://images.unsplash.com/photo-1528750997573-59b89d56f4f7?w=500&q=80"
            )
        }
        item {
            BannerCard(
                headline = "Crisp Juicy Fruits",
                subhead = "Hand-picked organic apples & bananas",
                brush = Brush.horizontalGradient(listOf(Color(0xFF8E44AD), Color(0xFF9B59B6))),
                imageUrl = "https://images.unsplash.com/photo-1610832958506-aa56368176cf?w=500&q=80"
            )
        }
    }
}

@Composable
private fun BannerCard(
    headline: String,
    subhead: String,
    brush: Brush,
    imageUrl: String
) {
    Card(
        modifier = Modifier
            .width(290.dp)
            .height(130.dp),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(brush)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1.3f)) {
                    Text(
                        text = headline,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = subhead,
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = Color.White.copy(alpha = 0.9f)
                        ),
                        maxLines = 2
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                AsyncImage(
                    model = imageUrl,
                    contentDescription = null,
                    modifier = Modifier
                        .size(80.dp)
                        .clip(RoundedCornerShape(12.dp)),
                    contentScale = ContentScale.Crop
                )
            }
        }
    }
}

@Composable
private fun CategoryPillCard(
    category: Category,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable { onClick() }
    ) {
        Box(
            modifier = Modifier
                .size(68.dp)
                .clip(CircleShape)
                .background(FreshGreenContainer),
            contentAlignment = Alignment.Center
        ) {
            AsyncImage(
                model = category.imageUrl,
                contentDescription = category.name,
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = category.name,
            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
fun ProductCard(
    product: Product,
    onClick: () -> Unit,
    onAddToCart: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .width(160.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            // Image with Discount Tag
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(110.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(FreshSurfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                AsyncImage(
                    model = product.imageUrl,
                    contentDescription = product.name,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )

                if (product.hasDiscount) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(6.dp)
                            .background(FreshOrangeAccent, RoundedCornerShape(6.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "${product.discountPercentage}% OFF",
                            color = Color.White,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = product.name,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Text(
                text = "${product.unitQuantity} ${product.unit.displayName}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = Formatters.formatPrice(product.effectivePrice),
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = FreshGreenDark
                        )
                    )
                    if (product.hasDiscount) {
                        Text(
                            text = Formatters.formatPrice(product.price),
                            style = MaterialTheme.typography.labelSmall.copy(
                                textDecoration = TextDecoration.LineThrough,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                    }
                }

                Button(
                    onClick = onAddToCart,
                    enabled = product.isAvailable,
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = FreshGreenPrimary,
                        contentColor = Color.White
                    ),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                    modifier = Modifier.height(30.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add",
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "Add",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                    )
                }
            }
        }
    }
}

@Composable
private fun ProductListRowItem(
    product: Product,
    onClick: () -> Unit,
    onAddToCart: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = product.imageUrl,
                contentDescription = product.name,
                modifier = Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(FreshSurfaceVariant),
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = product.name,
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    maxLines = 1
                )
                Text(
                    text = "${product.unitQuantity} ${product.unit.displayName}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = Formatters.formatPrice(product.effectivePrice),
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = FreshGreenDark
                        )
                    )
                    if (product.hasDiscount) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = Formatters.formatPrice(product.price),
                            style = MaterialTheme.typography.bodySmall.copy(
                                textDecoration = TextDecoration.LineThrough,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                    }
                }
            }

            Button(
                onClick = onAddToCart,
                enabled = product.isAvailable,
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = FreshGreenPrimary),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                modifier = Modifier.height(34.dp)
            ) {
                Text("Add", fontWeight = FontWeight.Bold)
            }
        }
    }
}
