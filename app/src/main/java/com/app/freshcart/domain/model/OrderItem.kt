package com.app.freshcart.domain.model

data class OrderItem(
    val id: Long = 0,
    val orderId: Long = 0,
    val productId: Long,
    val productName: String,
    val productPrice: Double,
    val quantity: Int,
    val unit: String,
    val imageUrl: String
) {
    val subtotal: Double
        get() = productPrice * quantity
}
