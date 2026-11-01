package com.app.freshcart.data.mapper

import com.app.freshcart.data.local.dao.CartItemWithProduct
import com.app.freshcart.data.local.entity.CategoryEntity
import com.app.freshcart.data.local.entity.OrderEntity
import com.app.freshcart.data.local.entity.OrderItemEntity
import com.app.freshcart.data.local.entity.ProductEntity
import com.app.freshcart.data.local.entity.UserEntity
import com.app.freshcart.domain.model.CartItem
import com.app.freshcart.domain.model.Category
import com.app.freshcart.domain.model.Order
import com.app.freshcart.domain.model.OrderItem
import com.app.freshcart.domain.model.Product
import com.app.freshcart.domain.model.User

fun UserEntity.toDomain(): User = User(
    id = id,
    email = email,
    name = name,
    phone = phone,
    address = address,
    profileImageUrl = profileImageUrl,
    role = role
)

fun User.toEntity(passwordHash: String = ""): UserEntity = UserEntity(
    id = id,
    email = email,
    passwordHash = passwordHash,
    name = name,
    phone = phone,
    address = address,
    profileImageUrl = profileImageUrl,
    role = role
)

fun CategoryEntity.toDomain(): Category = Category(
    id = id,
    name = name,
    description = description,
    imageUrl = imageUrl
)

fun Category.toEntity(): CategoryEntity = CategoryEntity(
    id = id,
    name = name,
    description = description,
    imageUrl = imageUrl
)

fun ProductEntity.toDomain(): Product = Product(
    id = id,
    categoryId = categoryId,
    name = name,
    description = description,
    price = price,
    discountPrice = discountPrice,
    unitQuantity = unitQuantity,
    unit = unit,
    imageUrl = imageUrl,
    isAvailable = isAvailable
)

fun Product.toEntity(): ProductEntity = ProductEntity(
    id = id,
    categoryId = categoryId,
    name = name,
    description = description,
    price = price,
    discountPrice = discountPrice,
    unitQuantity = unitQuantity,
    unit = unit,
    imageUrl = imageUrl,
    isAvailable = isAvailable
)

fun CartItemWithProduct.toDomain(): CartItem = CartItem(
    id = cartItemId,
    userId = userId,
    product = Product(
        id = productId,
        categoryId = categoryId,
        name = name,
        description = description,
        price = price,
        discountPrice = discountPrice,
        unitQuantity = unitQuantity,
        unit = unit,
        imageUrl = imageUrl,
        isAvailable = isAvailable
    ),
    quantity = quantity
)

fun OrderItemEntity.toDomain(): OrderItem = OrderItem(
    id = id,
    orderId = orderId,
    productId = productId,
    productName = productName,
    productPrice = productPrice,
    quantity = quantity,
    unit = unit,
    imageUrl = imageUrl
)

fun OrderItem.toEntity(orderId: Long = this.orderId): OrderItemEntity = OrderItemEntity(
    id = id,
    orderId = orderId,
    productId = productId,
    productName = productName,
    productPrice = productPrice,
    quantity = quantity,
    unit = unit,
    imageUrl = imageUrl
)

fun OrderEntity.toDomain(items: List<OrderItem> = emptyList()): Order = Order(
    id = id,
    userId = userId,
    customerName = customerName,
    customerPhone = customerPhone,
    deliveryAddress = deliveryAddress,
    totalAmount = totalAmount,
    paymentMethod = paymentMethod,
    paymentStatus = paymentStatus,
    orderStatus = orderStatus,
    rejectionReason = rejectionReason,
    createdAt = createdAt,
    items = items
)

fun Order.toEntity(): OrderEntity = OrderEntity(
    id = id,
    userId = userId,
    customerName = customerName,
    customerPhone = customerPhone,
    deliveryAddress = deliveryAddress,
    totalAmount = totalAmount,
    paymentMethod = paymentMethod,
    paymentStatus = paymentStatus,
    orderStatus = orderStatus,
    rejectionReason = rejectionReason,
    createdAt = createdAt
)
