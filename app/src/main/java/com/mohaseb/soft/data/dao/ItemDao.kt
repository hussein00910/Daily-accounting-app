package com.mohaseb.soft.data.dao

import androidx.room.*
import com.mohaseb.soft.data.entity.Item
import kotlinx.coroutines.flow.Flow

@Dao
interface ItemDao {
    @Query("SELECT * FROM items ORDER BY name")
    fun getAllItems(): Flow<List<Item>>

    @Query("SELECT * FROM items WHERE categoryId = :categoryId")
    fun getItemsByCategory(categoryId: Long): Flow<List<Item>>

    @Query("SELECT * FROM items WHERE name LIKE '%' || :query || '%' OR code LIKE '%' || :query || '%'")
    fun searchItems(query: String): Flow<List<Item>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItem(item: Item)

    @Update
    suspend fun updateItem(item: Item)

    @Delete
    suspend fun deleteItem(item: Item)

    @Query("UPDATE items SET quantity = quantity + :amount WHERE id = :itemId")
    suspend fun updateQuantity(itemId: Long, amount: Double)
}
