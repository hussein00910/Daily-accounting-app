package com.mohaseb.soft.ui.currencies

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.mohaseb.soft.MohasebApp
import com.mohaseb.soft.data.entity.Currency
import kotlinx.coroutines.launch

class CurrenciesViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = (application as MohasebApp).repository

    val currencies = repository.getAllCurrencies().asLiveData()

    fun saveCurrency(currency: Currency) {
        viewModelScope.launch {
            if (currency.id == 0L) {
                repository.insertCurrency(currency)
            } else {
                repository.updateCurrency(currency)
            }
        }
    }

    fun deleteCurrency(currency: Currency) {
        viewModelScope.launch {
            repository.deleteCurrency(currency)
        }
    }
}
