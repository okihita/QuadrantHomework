package com.okihita.quadranthomework.ui

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.*
import com.okihita.quadranthomework.data.entities.PriceIndexResponse
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

    private val _priceIndexResponse = MutableLiveData<PriceIndexResponse>()
    val latestPriceIndex: LiveData<PriceIndexResponse> = _priceIndexResponse

    private val _dbItems = MutableLiveData<List<PriceIndexResponse>>()
    val dbItems = _dbItems

    init {
        resetWork()
        loadDatabaseContent()
    }

    private fun loadDatabaseContent() {
        viewModelScope.launch {
            try {
                val databaseItems = repository.getAllPriceIndexResponse()
                _dbItems.value = databaseItems

                _priceIndexResponse.value = databaseItems.first()
            } catch (exception: Exception) {
                exception.printStackTrace()
            }
        }
    }

    private fun resetWork() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.UNMETERED)
            .setRequiresBatteryNotLow(true)
            .build()

        val work = PeriodicWorkRequestBuilder<CoinDeskUpdaterWorker>(15, TimeUnit.MINUTES)
            .build()

        workManager.enqueueUniquePeriodicWork(
            CoinDeskUpdaterWorker.WORK_NAME,
            ExistingPeriodicWorkPolicy.REPLACE,
            work
        )
    }
}