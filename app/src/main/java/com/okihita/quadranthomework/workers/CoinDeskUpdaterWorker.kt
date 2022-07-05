package com.okihita.quadranthomework.workers

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.okihita.quadranthomework.data.remote.CoinDeskApi
import com.okihita.quadranthomework.data.repository.CoinDeskRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.first
import javax.inject.Inject

@HiltWorker
class CoinDeskUpdaterWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters
) : CoroutineWorker(context, params) {

    companion object {
        const val WORK_NAME = "CoinDeskUpdaterWorker"
    }

    @Inject
    lateinit var repository: CoinDeskRepository

    override suspend fun doWork(): Result {
        return try {
            val response = repository.callCoinDeskApi()
            repository.insertPriceIndexResponse(response)
            Result.success()
        } catch (exception: Exception) {
            Result.failure()
        }
    }
}