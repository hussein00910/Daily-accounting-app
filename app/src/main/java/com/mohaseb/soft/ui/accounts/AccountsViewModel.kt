package com.mohaseb.soft.ui.accounts

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.mohaseb.soft.MohasebApp
import com.mohaseb.soft.data.entity.Account
import kotlinx.coroutines.launch

class AccountsViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = (application as MohasebApp).repository

    val accounts = repository.getAllAccounts().asLiveData()

    fun saveAccount(account: Account) {
        viewModelScope.launch {
            if (account.id == 0L) {
                repository.insertAccount(account)
            } else {
                repository.updateAccount(account)
            }
        }
    }

    fun deleteAccount(account: Account) {
        viewModelScope.launch {
            repository.deleteAccount(account)
        }
    }
}
