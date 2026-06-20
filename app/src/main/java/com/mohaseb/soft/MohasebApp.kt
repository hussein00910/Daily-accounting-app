package com.mohaseb.soft

import android.app.Application
import com.mohaseb.soft.data.AppDatabase
import com.mohaseb.soft.data.repository.AppRepository

class MohasebApp : Application() {
    val database by lazy { AppDatabase.getDatabase(this) }
    val repository by lazy { AppRepository(database) }
}
