package com.app.freshcart.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.app.freshcart.common.UnitType

@Entity(
    tableName = "products",
    foreignKeys = [
        ForeignKey(
            entity = CategoryEntity::class,
            parentColumns = ["id"],
            childColumns = ["categoryId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["categoryId"]),
        Index(value = ["name"])
    ]
)
data class ProductEntity(
    @PrimaryKey(autoGenerate = true)
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
)
