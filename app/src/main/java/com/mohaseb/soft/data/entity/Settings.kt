package com.mohaseb.soft.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "settings")
data class Settings(
    @PrimaryKey
    val id: Int = 1,
    val companyName: String = "",
    val companyAddress: String = "",
    val companyPhone: String = "",
    val taxNumber: String = "",
    val currency: String = "ريال",
    val language: String = "ar",
    val backupPath: String = "",
    val autoBackup: Boolean = true,
    val showLogo: Boolean = true,
    val printDate: Boolean = true,
    val appLocked: Boolean = false,
    val password: String = ""
)
