package com.mohaseb.soft.ui.warehouse

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.mohaseb.soft.MohasebApp
import kotlinx.coroutines.launch

class WarehouseViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = (application as MohasebApp).repository

    val items = repository.getAllItems().asLiveData()

    fun adjustQuantity(itemId: Long, delta: Double) {
        viewModelScope.launch {
            repository.updateQuantity(itemId, delta)
        }
    }
}
