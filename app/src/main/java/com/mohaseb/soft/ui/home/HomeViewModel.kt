package com.mohaseb.soft.ui.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.mohaseb.soft.MohasebApp
import kotlinx.coroutines.launch

class HomeViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = (application as MohasebApp).repository

    val totalDebit = MutableLiveData<Double>()
    val totalCredit = MutableLiveData<Double>()

    init {
        loadTotals()
    }

    fun loadTotals() {
        viewModelScope.launch {
            totalDebit.value = repository.getTotalDebit()
            totalCredit.value = repository.getTotalCredit()
        }
    }
}
