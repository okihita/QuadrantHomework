package com.okihita.quadranthomework.utils

import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

fun ZonedDateTime.toDateString(outputFormat: String = "E, d MMM yyyy - HH:mm"): String {
    val formatter = DateTimeFormatter.ofPattern(outputFormat)
    return format(formatter)
}

fun getSystemZonedDateTime(): ZonedDateTime {
    return ZonedDateTime.now(ZoneId.systemDefault())
}

fun ZonedDateTime.fromUtcToDevice(): ZonedDateTime {
    // Assume that the `this` has atZone("UTC")
    val deviceZone = ZoneId.systemDefault()
    return this.withZoneSameInstant(deviceZone)
}

fun ZonedDateTime.fromDeviceToUtc(): ZonedDateTime {
    // Assume that the `this` has atZone(ZoneId.systemDefault()), which is device's timezone
    return this.withZoneSameInstant(ZoneId.of("UTC"))
}