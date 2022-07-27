package com.okihita.quadranthomework.ui

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequest
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.okihita.quadranthomework.data.entities.PriceIndex
import com.okihita.quadranthomework.data.entities.getUTCZonedDateTime
import com.okihita.quadranthomework.data.repository.CoinDeskRepository
import com.okihita.quadranthomework.utils.fromDeviceToUtc
import com.okihita.quadranthomework.utils.getSystemZonedDateTime
import com.okihita.quadranthomework.workers.PriceLocationUpdateWorker
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    @ApplicationContext context: Context,
    repository: CoinDeskRepository
) : ViewModel() {

    private val workManager = WorkManager.getInstance(context)
    private var fetchPriceLocationRequest: PeriodicWorkRequest =
        PeriodicWorkRequestBuilder<PriceLocationUpdateWorker>(15, TimeUnit.MINUTES)
            .build()

    fun startPriceLocationUpdateWork() {
        workManager.enqueueUniquePeriodicWork(
            "QuadrantUpdatePriceLocation",
            ExistingPeriodicWorkPolicy.REPLACE,
            fetchPriceLocationRequest
        )
    }

    private val todayUtcDayOfYear = getSystemZonedDateTime().fromDeviceToUtc().dayOfYear
    val priceIndicesFlow: Flow<List<PriceIndex>> = repository.getAllPriceIndices().map {
        it.filter { priceIndex ->
            priceIndex.getUTCZonedDateTime().dayOfYear == todayUtcDayOfYear
        }
    }
}