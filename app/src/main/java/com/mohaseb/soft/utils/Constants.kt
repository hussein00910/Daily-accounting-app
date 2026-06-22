package com.mohaseb.soft.utils

object Constants {
    const val DATABASE_NAME = "mohaseb_soft_database"
    const val DATABASE_VERSION = 1

    // Transaction types
    const val TYPE_SALE = "sale"
    const val TYPE_PURCHASE = "purchase"
    const val TYPE_CASH_IN = "cash_in"
    const val TYPE_CASH_OUT = "cash_out"
    const val TYPE_DAILY_ENTRY = "daily_entry"
    const val TYPE_OPENING_ENTRY = "opening_entry"

    // Account types
    const val ACCOUNT_DEBIT = "debit"
    const val ACCOUNT_CREDIT = "credit"

    // Customer types
    const val CUSTOMER_TYPE = "customer"
    const val SUPPLIER_TYPE = "supplier"

    // Warehouse operations
    const val WAREHOUSE_IN = "warehouse_in"
    const val WAREHOUSE_OUT = "warehouse_out"
    const val WAREHOUSE_TRANSFER = "warehouse_transfer"
    const val WAREHOUSE_ADJUST = "warehouse_adjust"

    // SharedPreferences keys
    const val PREFS_NAME = "mohaseb_soft_prefs"
    const val PREF_LAST_BACKUP_TIME = "last_backup_time"
    const val PREF_APP_LOCKED = "app_locked"

    // Backup/Export folders
    const val BACKUP_FOLDER = "backups"
    const val EXPORT_FOLDER = "exports"
}
