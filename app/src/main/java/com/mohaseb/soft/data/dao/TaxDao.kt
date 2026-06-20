package com.mohaseb.soft.data.dao

import androidx.room.*
import com.mohaseb.soft.data.entity.Tax
import kotlinx.coroutines.flow.Flow

@Dao
interface TaxDao {
    @Query("SELECT * FROM taxes ORDER BY name")
    fun getAllTaxes(): Flow<List<Tax>>

    @Query("SELECT * FROM taxes WHERE isDefault = 1 LIMIT 1")
    suspend fun getDefaultTax(): Tax?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTax(tax: Tax)

    @Update
    suspend fun updateTax(tax: Tax)

    @Delete
    suspend fun deleteTax(tax: Tax)
}
