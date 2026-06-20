package com.mohaseb.soft.ui.items

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.mohaseb.soft.MohasebApp
import com.mohaseb.soft.data.entity.Item
import kotlinx.coroutines.launch

class ItemsViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = (application as MohasebApp).repository

    private val query = MutableLiveData("")

    val items = Transformations.switchMap(query) { q ->
        if (q.isNullOrBlank()) repository.getAllItems().asLiveData()
        else repository.searchItems(q).asLiveData()
    }

    fun setQuery(q: String) {
        query.value = q
    }

    fun saveItem(item: Item) {
        viewModelScope.launch {
            if (item.id == 0L) {
                repository.insertItem(item)
            } else {
                repository.updateItem(item)
            }
        }
    }
}
