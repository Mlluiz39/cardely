package com.example

import android.content.Context
import com.example.data.local.AppDatabase
import com.example.data.local.CardEntity
import com.example.data.local.TransactionEntity
import com.example.data.repository.CardelyRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

object ServiceLocator {
    private var database: AppDatabase? = null
    var repository: CardelyRepository? = null
        private set

    fun init(context: Context) {
        if (database == null) {
            val db = AppDatabase.getInstance(context)
            database = db
            val repo = CardelyRepository(db.cardDao(), db.transactionDao())
            repository = repo
        }
    }
}
