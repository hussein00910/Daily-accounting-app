package com.mohaseb.soft.data.dao

import androidx.room.*
import com.mohaseb.soft.data.entity.Account
import kotlinx.coroutines.flow.Flow

@Dao
interface AccountDao {
    @Query("SELECT * FROM accounts ORDER BY createdAt DESC")
    fun getAllAccounts(): Flow<List<Account>>

    @Query("SELECT * FROM accounts WHERE id = :id")
    suspend fun getAccountById(id: Long): Account?

    @Query("SELECT * FROM accounts WHERE type = :type")
    fun getAccountsByType(type: String): Flow<List<Account>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAccount(account: Account)

    @Update
    suspend fun updateAccount(account: Account)

    @Query("UPDATE accounts SET balance = balance + :amount WHERE id = :accountId")
    suspend fun updateBalance(accountId: Long, amount: Double)

    @Delete
    suspend fun deleteAccount(account: Account)

    @Query("DELETE FROM accounts WHERE id = :id")
    suspend fun deleteAccountById(id: Long)

    @Query("SELECT SUM(balance) FROM accounts WHERE type = 'debit'")
    suspend fun getTotalDebit(): Double?

    @Query("SELECT SUM(balance) FROM accounts WHERE type = 'credit'")
    suspend fun getTotalCredit(): Double?
}
