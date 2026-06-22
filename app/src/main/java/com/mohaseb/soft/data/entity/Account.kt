package com.mohaseb.soft.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "accounts")
data class Account(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val type: String, // debit, credit
    val balance: Double = 0.0,
    val categoryId: Long? = null,
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis()
)
