package com.mohaseb.soft.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "customers")
data class Customer(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val phone: String = "",
    val address: String = "",
    val balance: Double = 0.0,
    val type: String = "customer", // customer, supplier
    val notes: String = ""
)
