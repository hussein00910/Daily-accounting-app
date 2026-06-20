package com.mohaseb.soft.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.mohaseb.soft.data.dao.*
import com.mohaseb.soft.data.entity.*

@Database(
    entities = [
        Account::class,
        Transaction::class,
        Item::class,
        Customer::class,
        Settings::class,
        Category::class,
        Currency::class,
        Tax::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun accountDao(): AccountDao
    abstract fun transactionDao(): TransactionDao
    abstract fun itemDao(): ItemDao
    abstract fun customerDao(): CustomerDao
    abstract fun settingsDao(): SettingsDao
    abstract fun categoryDao(): CategoryDao
    abstract fun currencyDao(): CurrencyDao
    abstract fun taxDao(): TaxDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "mohaseb_soft_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
