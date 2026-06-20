package com.mohaseb.soft.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "currencies")
data class Currency(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val symbol: String,
    val rate: Double = 1.0,
    val isDefault: Boolean = false
)
