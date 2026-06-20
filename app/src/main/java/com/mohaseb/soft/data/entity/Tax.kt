package com.mohaseb.soft.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "taxes")
data class Tax(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val rate: Double = 0.0,
    val isDefault: Boolean = false,
    val showInInvoice: Boolean = true
)
