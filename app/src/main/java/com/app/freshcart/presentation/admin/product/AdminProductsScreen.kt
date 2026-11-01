package com.app.freshcart.presentation.admin.product

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.app.freshcart.common.Formatters
import com.app.freshcart.common.UnitType
import com.app.freshcart.domain.model.Category
import com.app.freshcart.domain.model.Product
import com.app.freshcart.presentation.common.EmptyStateView
import com.app.freshcart.presentation.common.FreshButton
import com.app.freshcart.presentation.common.FreshTextField
import com.app.freshcart.presentation.common.LoadingView
import com.app.freshcart.ui.theme.FreshGreenDark
import com.app.freshcart.ui.theme.FreshGreenPrimary
import com.app.freshcart.ui.theme.FreshSurfaceVariant

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminProductsScreen(
    onNavigateBack: () -> Unit,
    viewModel: AdminProductsViewModel = hiltViewModel()
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
                        text = "Product Catalog",
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
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { viewModel.openAddProduct() },
                containerColor = FreshGreenPrimary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Add Product")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Search Bar
            Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                FreshTextField(
                    value = state.searchQuery,
                    onValueChange = { viewModel.onSearchQueryChanged(it) },
                    placeholder = "Search products...",
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

            // Categories Filter Bar
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    FilterChip(
                        selected = state.selectedCategoryId == null,
                        onClick = { viewModel.selectCategory(null) },
                        label = { Text("All (${state.products.size})") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = FreshGreenPrimary,
                            selectedLabelColor = Color.White
                        )
                    )
                }
                items(state.categories, key = { it.id }) { cat ->
                    val count = state.products.count { it.categoryId == cat.id }
                    FilterChip(
                        selected = state.selectedCategoryId == cat.id,
                        onClick = { viewModel.selectCategory(cat.id) },
                        label = { Text("${cat.name} ($count)") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = FreshGreenPrimary,
                            selectedLabelColor = Color.White
                        )
                    )
                }
            }

            // Products List
            if (state.isLoading && state.products.isEmpty()) {
                LoadingView(message = "Loading products...")
            } else if (state.filteredProducts.isEmpty()) {
                EmptyStateView(
                    title = "No Products Found",
                    message = if (state.searchQuery.isNotBlank()) "No products matched your search." else "Tap + to add a product under this category."
                )
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(state.filteredProducts, key = { it.id }) { product ->
                        val categoryName = state.categories.find { it.id == product.categoryId }?.name ?: "Grocery"
                        AdminProductItemCard(
                            product = product,
                            categoryName = categoryName,
                            onEdit = { viewModel.openEditProduct(product) },
                            onDelete = { viewModel.confirmDeleteProduct(product) }
                        )
                    }
                    item {
                        Spacer(modifier = Modifier.height(72.dp))
                    }
                }
            }
        }

        // Add / Edit Product Dialog
        if (state.isAddEditOpen) {
            AddEditProductDialog(
                product = state.selectedProduct,
                categories = state.categories,
                initialCategoryId = state.selectedCategoryId ?: state.categories.firstOrNull()?.id ?: 1L,
                onDismiss = { viewModel.dismissAddEdit() },
                onSave = { name, desc, catId, price, discPrice, qty, unit, img, avail ->
                    viewModel.saveProduct(name, desc, catId, price, discPrice, qty, unit, img, avail)
                }
            )
        }

        // Delete Confirmation Dialog
        state.deleteConfirmationProduct?.let { productToDelete ->
            AlertDialog(
                onDismissRequest = { viewModel.dismissDeleteConfirmation() },
                icon = {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error
                    )
                },
                title = { Text("Delete Product?") },
                text = {
                    Text("Are you sure you want to remove '${productToDelete.name}' from inventory?")
                },
                confirmButton = {
                    TextButton(onClick = { viewModel.deleteProduct() }) {
                        Text("Delete", color = MaterialTheme.colorScheme.error)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { viewModel.dismissDeleteConfirmation() }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}

@Composable
private fun AdminProductItemCard(
    product: Product,
    categoryName: String,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = product.imageUrl,
                contentDescription = product.name,
                modifier = Modifier
                    .size(76.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(FreshSurfaceVariant),
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = categoryName.uppercase(),
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = FreshGreenPrimary
                )
                Text(
                    text = product.name,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1
                )
                Text(
                    text = "${product.unitQuantity} ${product.unit.displayName}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(4.dp))

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

                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = if (product.isAvailable) "● In Stock" else "● Out of Stock",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                    color = if (product.isAvailable) FreshGreenPrimary else MaterialTheme.colorScheme.error
                )
            }

            Column {
                IconButton(onClick = onEdit) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit",
                        tint = FreshGreenPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                }
                IconButton(onClick = onDelete) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddEditProductDialog(
    product: Product?,
    categories: List<Category>,
    initialCategoryId: Long,
    onDismiss: () -> Unit,
    onSave: (
        name: String,
        desc: String,
        catId: Long,
        price: Double,
        discPrice: Double?,
        qty: Double,
        unit: UnitType,
        imageUrl: String,
        isAvailable: Boolean
    ) -> Unit
) {
    var name by remember { mutableStateOf(product?.name ?: "") }
    var description by remember { mutableStateOf(product?.description ?: "") }
    var selectedCategoryId by remember { mutableStateOf(product?.categoryId ?: initialCategoryId) }
    var priceText by remember { mutableStateOf(product?.price?.toString() ?: "") }
    var discountPriceText by remember { mutableStateOf(product?.discountPrice?.toString() ?: "") }
    var unitQuantityText by remember { mutableStateOf(product?.unitQuantity?.toString() ?: "1.0") }
    var selectedUnit by remember { mutableStateOf(product?.unit ?: UnitType.KG) }
    var imageUrl by remember { mutableStateOf(product?.imageUrl ?: "") }
    var isAvailable by remember { mutableStateOf(product?.isAvailable ?: true) }

    var categoryDropdownExpanded by remember { mutableStateOf(false) }
    var unitDropdownExpanded by remember { mutableStateOf(false) }

    val presetImages = listOf(
        "https://images.unsplash.com/photo-1560806887-1e4cd0b6cbd6?w=500&q=80", // Apples
        "https://images.unsplash.com/photo-1571771894821-ce9b6c11b08e?w=500&q=80", // Bananas
        "https://images.unsplash.com/photo-1592924357228-91a4daadcfea?w=500&q=80", // Tomatoes
        "https://images.unsplash.com/photo-1518977676601-b53f82aba655?w=500&q=80", // Potatoes
        "https://images.unsplash.com/photo-1550583724-b2692b85b150?w=500&q=80", // Milk
        "https://images.unsplash.com/photo-1509440159596-0249088772ff?w=500&q=80"  // Bread
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (product == null) "Add Product" else "Edit Product",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Category Picker
                ExposedDropdownMenuBox(
                    expanded = categoryDropdownExpanded,
                    onExpandedChange = { categoryDropdownExpanded = !categoryDropdownExpanded }
                ) {
                    val currentCatName = categories.find { it.id == selectedCategoryId }?.name ?: "Select Category"
                    OutlinedTextField(
                        value = currentCatName,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Category *") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryDropdownExpanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
                        shape = RoundedCornerShape(12.dp)
                    )
                    ExposedDropdownMenu(
                        expanded = categoryDropdownExpanded,
                        onDismissRequest = { categoryDropdownExpanded = false }
                    ) {
                        categories.forEach { cat ->
                            DropdownMenuItem(
                                text = { Text(cat.name) },
                                onClick = {
                                    selectedCategoryId = cat.id
                                    categoryDropdownExpanded = false
                                }
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Product Name *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description") },
                    maxLines = 2,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = priceText,
                        onValueChange = { priceText = it },
                        label = { Text("Price (₹) *") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    )
                    OutlinedTextField(
                        value = discountPriceText,
                        onValueChange = { discountPriceText = it },
                        label = { Text("Discount (₹)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = unitQuantityText,
                        onValueChange = { unitQuantityText = it },
                        label = { Text("Quantity") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    )

                    // Unit selector dropdown
                    ExposedDropdownMenuBox(
                        expanded = unitDropdownExpanded,
                        onExpandedChange = { unitDropdownExpanded = !unitDropdownExpanded },
                        modifier = Modifier.weight(1f)
                    ) {
                        OutlinedTextField(
                            value = selectedUnit.displayName,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Unit") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = unitDropdownExpanded) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor(),
                            shape = RoundedCornerShape(12.dp)
                        )
                        ExposedDropdownMenu(
                            expanded = unitDropdownExpanded,
                            onDismissRequest = { unitDropdownExpanded = false }
                        ) {
                            UnitType.entries.forEach { unit ->
                                DropdownMenuItem(
                                    text = { Text("${unit.name} (${unit.displayName})") },
                                    onClick = {
                                        selectedUnit = unit
                                        unitDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }

                OutlinedTextField(
                    value = imageUrl,
                    onValueChange = { imageUrl = it },
                    label = { Text("Image URL") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Text(
                    text = "Preset images:",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(presetImages) { presetUrl ->
                        AsyncImage(
                            model = presetUrl,
                            contentDescription = null,
                            modifier = Modifier
                                .size(42.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { imageUrl = presetUrl },
                            contentScale = ContentScale.Crop
                        )
                    }
                }

                // In Stock Toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Available In Stock",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium)
                    )
                    Switch(
                        checked = isAvailable,
                        onCheckedChange = { isAvailable = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = FreshGreenPrimary
                        )
                    )
                }
            }
        },
        confirmButton = {
            FreshButton(
                text = if (product == null) "Add" else "Save",
                onClick = {
                    val price = priceText.toDoubleOrNull() ?: 0.0
                    val discPrice = discountPriceText.toDoubleOrNull()
                    val qty = unitQuantityText.toDoubleOrNull() ?: 1.0
                    onSave(name, description, selectedCategoryId, price, discPrice, qty, selectedUnit, imageUrl, isAvailable)
                },
                enabled = name.isNotBlank() && (priceText.toDoubleOrNull() ?: 0.0) > 0.0,
                modifier = Modifier.width(100.dp)
            )
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
