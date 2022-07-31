package com.okihita.quadranthomework.workers

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.tasks.Task
import com.okihita.quadranthomework.R
import com.okihita.quadranthomework.data.entities.PriceIndex
import com.okihita.quadranthomework.data.entities.getUTCZonedDateTime
import com.okihita.quadranthomework.data.repository.CoinDeskRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first
import retrofit2.HttpException
import java.util.*
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

// https://developer.android.com/reference/androidx/hilt/work/HiltWorker
@HiltWorker
class PriceLocationUpdateWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val repository: CoinDeskRepository,
    private val locationClient: FusedLocationProviderClient
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {

        return try {

            val lastLocation: Location? = locationClient.lastLocation.getSuspendLocation()
            val deviceLocation = PriceIndex.DeviceLocation(
                lastLocation?.latitude ?: 0.0,
                lastLocation?.longitude ?: 0.0,
                lastLocation?.getCompleteAddressString() ?: "Address not found"
            )

            val apiPriceIndex = repository.callCoinDeskApi()
            apiPriceIndex.location = deviceLocation

            // If the database is empty, save the response immediately
            if (repository.getAllPriceIndices().first().isEmpty()) {
                repository.insertPriceIndex(apiPriceIndex)
                showNotification(apiPriceIndex)
            }

            // If the database contains data from the same hour, check if it's a new-hour data
            else {
                val lastCacheDateTime = repository.getNewestPriceIndex().getUTCZonedDateTime()

                // If the response's date is the same with the date, check if it's the same hour
                if (apiPriceIndex.getUTCZonedDateTime().dayOfYear == lastCacheDateTime.dayOfYear) {
                    if (apiPriceIndex.getUTCZonedDateTime().hour != lastCacheDateTime.hour) {
                        repository.insertPriceIndex(apiPriceIndex)
                        showNotification(apiPriceIndex)
                    } else {
                        // Do nothing with the data from same hour
                    }
                }

                // If the response belongs to a new/different day
                else {
                    repository.insertPriceIndex(apiPriceIndex)
                    showNotification(apiPriceIndex)
                }
            }

            Result.success()

        } catch (exception: SecurityException) {
            Result.failure()
        } catch (exception: HttpException) {
            Result.failure()
        } catch (exception: Exception) {
            Result.failure()
        }
    }

    /**
     * Make the callback function of fusedLocationProvider run sequentially, similar to runBlocking.
     */
    private suspend fun Task<Location>.getSuspendLocation(): Location? =
        suspendCoroutine { continuation ->
            addOnSuccessListener { lastLocation ->
                continuation.resume(lastLocation)
            }
            addOnFailureListener { exception ->
                continuation.resumeWithException(exception)
            }
        }

    private fun Location.getCompleteAddressString(): String {
        var addressString = ""
        val geocoder = Geocoder(applicationContext, Locale.getDefault())

        return try {
            val address: Address? = geocoder.getFromLocation(latitude, longitude, 1).firstOrNull()
            if (address != null) {

                val completeAddress = StringBuilder()
                for (i in 0..address.maxAddressLineIndex) {
                    completeAddress.append(address.getAddressLine(i)).append("\n")
                }
                addressString = completeAddress.toString()
            }

            addressString

        } catch (e: java.lang.Exception) {
            e.printStackTrace()
            return "address not found"
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
            .setSmallIcon(R.drawable.ic_update)
            .setContentTitle("Price Updated!")
            .setContentText("1 BTC is now USD ${response.bpi["USD"]?.rate_float}, from location ${response.location?.address}")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()

        manager.notify(1, notification)
    }
}