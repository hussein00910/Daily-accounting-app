package com.mohaseb.soft.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "transactions")
data class Transaction(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val accountId: Long,
    val type: String, // sale, purchase, cash_in, cash_out
    val amount: Double,
    val quantity: Double = 1.0,
    val price: Double = 0.0,
    val itemId: Long? = null,
    val customerId: Long? = null,
    val date: Long = System.currentTimeMillis(),
    val notes: String = "",
    val isCredit: Boolean = false,
    val warehouseId: Long? = null,
    val taxAmount: Double = 0.0,
    val discount: Double = 0.0
)
