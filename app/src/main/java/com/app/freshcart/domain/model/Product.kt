package com.app.freshcart.domain.model

import com.app.freshcart.common.UnitType

data class Product(
    val id: Long = 0,
    val categoryId: Long,
    val name: String,
    val description: String,
    val price: Double,
    val discountPrice: Double? = null,
    val unitQuantity: Double = 1.0,
    val unit: UnitType = UnitType.KG,
    val imageUrl: String,
    val isAvailable: Boolean = true
) {
    val effectivePrice: Double
        get() = if (discountPrice != null && discountPrice > 0 && discountPrice < price) discountPrice else price

    val hasDiscount: Boolean
        get() = discountPrice != null && discountPrice > 0 && discountPrice < price

    val discountPercentage: Int
        get() = if (hasDiscount) (((price - effectivePrice) / price) * 100).toInt() else 0
}
