package com.okihita.quadranthomework.utils

import androidx.lifecycle.MutableLiveData
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

fun <T> MutableLiveData<T>.refresh() {
    this.value = this.value // Calls MLD with the same value to trigger observer e.g. for UI updates
}

fun ZonedDateTime.toDateString(outputFormat: String = "E, d MMM yyyy - HH:mm"): String {
    val formatter = DateTimeFormatter.ofPattern(outputFormat)
    return format(formatter)
}

fun getCurrentDeviceZonedDateTime(): ZonedDateTime {
    return LocalDateTime.now().atZone(ZoneId.systemDefault())
}

fun ZonedDateTime.fromUtcToDevice(): ZonedDateTime {
    // Assume that the `this` has atZone("UTC")
    val deviceZone = ZoneId.systemDefault()
    return this.withZoneSameInstant(deviceZone)
}

fun ZonedDateTime.fromDeviceToUtc(): ZonedDateTime {
    // Assume that the `this` has atZone(ZoneId.systemDefault())
    return this.withZoneSameInstant(ZoneId.of("UTC"))
}