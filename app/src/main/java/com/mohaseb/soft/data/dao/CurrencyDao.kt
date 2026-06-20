package com.mohaseb.soft.data.dao

import androidx.room.*
import com.mohaseb.soft.data.entity.Currency
import kotlinx.coroutines.flow.Flow

@Dao
interface CurrencyDao {
    @Query("SELECT * FROM currencies ORDER BY name")
    fun getAllCurrencies(): Flow<List<Currency>>

    @Query("SELECT * FROM currencies WHERE isDefault = 1 LIMIT 1")
    suspend fun getDefaultCurrency(): Currency?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCurrency(currency: Currency)

    @Update
    suspend fun updateCurrency(currency: Currency)

    @Delete
    suspend fun deleteCurrency(currency: Currency)
}
