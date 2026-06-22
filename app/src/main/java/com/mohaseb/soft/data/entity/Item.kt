package com.mohaseb.soft.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "items")
data class Item(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val code: String = "",
    val categoryId: Long? = null,
    val unit: String = "حبة",
    val purchasePrice: Double = 0.0,
    val salePrice: Double = 0.0,
    val quantity: Double = 0.0,
    val minQuantity: Double = 0.0,
    val barcode: String = "",
    val notes: String = "",
    val imageUrl: String = "",
    val taxId: Long? = null
)
