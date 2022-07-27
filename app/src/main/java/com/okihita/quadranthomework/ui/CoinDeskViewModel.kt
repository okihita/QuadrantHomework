package com.okihita.quadranthomework.ui

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.*
import com.okihita.quadranthomework.data.entities.PriceIndex
import com.okihita.quadranthomework.data.entities.getISOZonedDateTime
import com.okihita.quadranthomework.data.repository.CoinDeskRepository
import com.okihita.quadranthomework.utils.fromDeviceToUtc
import com.okihita.quadranthomework.utils.getCurrentDeviceZonedDateTime
import com.okihita.quadranthomework.utils.refresh
import com.okihita.quadranthomework.workers.PriceLocationUpdateWorker
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
    private lateinit var fetchPriceLocationRequest: PeriodicWorkRequest
    lateinit var workInfo: LiveData<WorkInfo>

    private val _cacheItems = MutableLiveData<List<PriceIndex>>()
    val cacheItems: LiveData<List<PriceIndex>> = _cacheItems

    init {
        reloadTodayItemsFromDatabase()
        setupPriceLocationWork()
    }

    private fun setupPriceLocationWork() {
        fetchPriceLocationRequest =
            PeriodicWorkRequestBuilder<PriceLocationUpdateWorker>(15, TimeUnit.MINUTES)
                .build()
        workInfo = workManager.getWorkInfoByIdLiveData(fetchPriceLocationRequest.id)
    }

    fun startPriceLocationUpdateWork() {
        workManager.enqueueUniquePeriodicWork(
            "QuadrantUpdatePriceLocation",
            ExistingPeriodicWorkPolicy.REPLACE,
            fetchPriceLocationRequest
        )
    }

    fun reloadTodayItemsFromDatabase() {
        viewModelScope.launch {
            try {

                // Get all items from database
                val databaseItems = repository.getAllPriceIndices()

                // Get only items for today (using UTC timezone)
                val todayUtcDate = getCurrentDeviceZonedDateTime().fromDeviceToUtc().dayOfYear
                val todayItems = databaseItems.filter {
                    it.getISOZonedDateTime().dayOfYear == todayUtcDate
                }

                _cacheItems.value = todayItems
            } catch (exception: Exception) {
                exception.printStackTrace()
            }
        }
    }

    fun refreshCacheItems() {
        _cacheItems.refresh()
    }
}