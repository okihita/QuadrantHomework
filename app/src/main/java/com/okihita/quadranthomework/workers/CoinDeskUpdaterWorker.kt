package com.okihita.quadranthomework.workers

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.okihita.quadranthomework.data.entities.PriceIndex
import com.okihita.quadranthomework.data.entities.getDateTime
import com.okihita.quadranthomework.data.repository.CoinDeskRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
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
            val responseDateTime = response.getDateTime()

            // If the cache is empty, save the response immediately
            if (repository.getAllPriceIndexResponse().isEmpty()) {
                repository.insertPriceIndexResponse(response)
                showNotification(response)
            }

            // If the data for a certain hour is already saved, don't save the response
            else {
                val lastCacheDateTime = repository.getLatestCacheItem().getDateTime()

                if (response.getDateTime().dayOfYear == lastCacheDateTime.dayOfYear) {
                    if (responseDateTime.hour != lastCacheDateTime.hour) {
                        repository.insertPriceIndexResponse(response)
                        showNotification(response)
                    }
                }
            }

            Result.success()

        } catch (exception: Exception) {
            exception.printStackTrace()
            Result.failure()
        }
    }

    private fun showNotification(response: PriceIndex) {
        val manager =
            applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "channelId"
        val chName = "Hourly BTC price update"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel =
                NotificationChannel(channelId, chName, NotificationManager.IMPORTANCE_DEFAULT)
            channel.description = "Sent when app successfully fetched and saved latest price"
            manager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(applicationContext, channelId)
            .setSmallIcon(com.google.android.material.R.drawable.ic_mtrl_checked_circle)
            .setContentTitle("Price Updated!")
            .setContentText("1 BTC is now USD ${response.bpi["USD"]?.rate_float}")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()

        manager.notify(1, notification)
    }
}