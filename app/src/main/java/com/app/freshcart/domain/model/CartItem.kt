package com.app.freshcart.domain.model

data class CartItem(
    val id: Long = 0,
    val userId: Long,
    val product: Product,
    val quantity: Int
) {
    val subtotal: Double
        get() = product.effectivePrice * quantity
}
