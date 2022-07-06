package com.okihita.quadranthomework.ui

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.okihita.quadranthomework.data.entities.PriceIndex
import com.okihita.quadranthomework.data.repository.CoinDeskRepository
import com.okihita.quadranthomework.workers.CoinDeskUpdaterWorker
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltViewModel
class CoinDeskViewModel @Inject constructor(
    @ApplicationContext context: Context,
    private val repository: CoinDeskRepository
) : ViewModel() {

    private val workManager = WorkManager.getInstance(context)
    lateinit var workInfo: LiveData<WorkInfo>

    private val _cacheItems = MutableLiveData<List<PriceIndex>>()
    val cacheItems = _cacheItems

    init {
        Log.d("Xena", "vm init: ")
        reloadCache()
        resetBackgroundWork()
    }

    fun reloadCache() {
        Log.d("Xena", "reloadCache: ")
        viewModelScope.launch {
            try {
                val databaseItems = repository.getAllPriceIndexResponse()
                _cacheItems.value = databaseItems
            } catch (exception: Exception) {
                exception.printStackTrace()
            }
        }
    }

    private fun resetBackgroundWork() {
        Log.d("Xena", "resetBackgroundWork: ")
        val work = PeriodicWorkRequestBuilder<CoinDeskUpdaterWorker>(15, TimeUnit.MINUTES)
            .build()

        workManager.enqueueUniquePeriodicWork(
            CoinDeskUpdaterWorker.WORK_NAME,
            ExistingPeriodicWorkPolicy.REPLACE,
            work
        )

        workInfo = workManager.getWorkInfoByIdLiveData(work.id)
    }
}