package com.okihita.quadranthomework.workers

import android.content.Context
import android.location.Location
import android.os.Looper
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.google.android.gms.location.*
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.util.concurrent.TimeUnit

@HiltWorker
class LocationUpdateWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters
) : CoroutineWorker(context, params) {

    private var location: Location? = null // The current location
    private lateinit var locationRequest: LocationRequest
    private lateinit var locationCallback: LocationCallback
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient

    override suspend fun doWork(): Result {
        Log.d("Xena", "doWork: fetch location")

        return try {
            locationRequest = LocationRequest.create().apply {
                interval = TimeUnit.SECONDS.toMillis(60)
                fastestInterval = TimeUnit.SECONDS.toMillis(30)
                maxWaitTime = TimeUnit.MINUTES.toMillis(2)
                priority = Priority.PRIORITY_HIGH_ACCURACY
            }

            locationCallback = object : LocationCallback() {
                override fun onLocationResult(locationResult: LocationResult) {
                    super.onLocationResult(locationResult)
                    location = locationResult.lastLocation
                }
            }

            fusedLocationProviderClient = LocationServices
                .getFusedLocationProviderClient(applicationContext)

            fusedLocationProviderClient.lastLocation.addOnCompleteListener {

                try {
                    location = it.result
                } catch (unlikely: Exception) {
                    // Somehow the permission is denied in the middle of the request
                    Log.e("Xena", "doWork: $unlikely")
                }
            }

            fusedLocationProviderClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                Looper.getMainLooper()
            )

            Result.success()

        } catch (exception: Exception) {
            Log.e("Xena", "doWork: $exception")
            exception.printStackTrace()
            Result.failure()
        }
    }
}