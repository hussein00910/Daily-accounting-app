package com.mohaseb.soft.ui.settings

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.mohaseb.soft.MohasebApp
import com.mohaseb.soft.R
import com.mohaseb.soft.data.entity.Settings
import com.mohaseb.soft.utils.BackupManager
import com.mohaseb.soft.utils.ExportManager
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class SettingsViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = (application as MohasebApp).repository
    private val backupManager = BackupManager(application, repository)
    private val exportManager = ExportManager(application)

    private val _settings = MutableLiveData<Settings>()
    val settings: LiveData<Settings> = _settings

    private val _message = MutableLiveData<Int>()
    val message: LiveData<Int> = _message

    init {
        viewModelScope.launch {
            val current = repository.getSettings().first()
            val resolved = current ?: Settings().also { repository.insertSettings(it) }
            _settings.value = resolved
        }
    }

    fun saveSettings(settings: Settings) {
        viewModelScope.launch {
            repository.updateSettings(settings)
            _settings.value = settings
        }
    }

    fun backupNow() {
        viewModelScope.launch {
            try {
                backupManager.backupData()
                _message.value = R.string.backup_success
            } catch (e: Exception) {
                _message.value = R.string.backup_failed
            }
        }
    }

    fun restoreBackup(filePath: String) {
        viewModelScope.launch {
            val result = backupManager.restoreData(filePath)
            _message.value = if (result != null) R.string.restore_success else R.string.restore_failed
        }
    }

    fun exportFullDataset() {
        viewModelScope.launch {
            val accounts = repository.getAllAccounts().first()
            val items = repository.getAllItems().first()
            val customers = repository.getAllCustomers().first()
            val transactions = repository.getAllTransactions().first()
            exportManager.exportFullDataset(
                accounts, items, customers, transactions, "full_export_${System.currentTimeMillis()}"
            )
        }
    }
}
