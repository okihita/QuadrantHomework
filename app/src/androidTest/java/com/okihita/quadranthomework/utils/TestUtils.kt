package com.okihita.quadranthomework.utils

import com.google.gson.Gson
import com.okihita.quadranthomework.data.entities.PriceIndex
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import kotlin.random.Random

/**
 * Generate 24 instances of Price Index object, each corresponding to a certain hour
 * with some fluctuations in the rates. IMPORTANT ASSUMPTION: The device/emulator is in Jakarta.
 */
fun generateTodayUtcPriceIndices(): List<PriceIndex> {

    val todayPriceIndices = mutableListOf<PriceIndex>()
    val priceIndex = Gson().fromJson(latestPriceIndexJson, PriceIndex::class.java)

    val localDateTime = ZonedDateTime.now(ZoneId.systemDefault())
    println(localDateTime)
    val utcDateTime: ZonedDateTime = localDateTime.fromDeviceToUtc()

    // Create 24 objects with ascending hour and fluctuating rates
    repeat(24) {
        val thisHourDateTime = utcDateTime.with(LocalTime.of(it, Random.nextInt(59)))
        priceIndex.bpi.forEach { currencyKeyedBpi ->

            // Add a fluctuation between -100 to +100
            val newRate = currencyKeyedBpi.value.rate_float + (Random.nextFloat() * 200 - 100)
            currencyKeyedBpi.value.rate_float = newRate
            currencyKeyedBpi.value.rate = newRate.toString()
        }

        priceIndex.time.updatedISO = thisHourDateTime.format(DateTimeFormatter.ISO_DATE_TIME)
        todayPriceIndices.add(priceIndex)
    }

    return todayPriceIndices
}

val latestPriceIndexJson = """
    {
      "time": {
        "updated": "Jul 27, 2022 07:45:00 UTC",
        "updatedISO": "2022-07-27T07:45:00+00:00",
        "updateduk": "Jul 27, 2022 at 08:45 BST"
      },
      "disclaimer": "This data was produced from the CoinDesk Bitcoin Price Index (USD). Non-USD currency data converted using hourly conversion rate from openexchangerates.org",
      "chartName": "Bitcoin",
      "bpi": {
        "USD": {
          "code": "USD",
          "symbol": "&#36;",
          "rate": "21,348.0434",
          "description": "United States Dollar",
          "rate_float": 21348.0434
        },
        "GBP": {
          "code": "GBP",
          "symbol": "&pound;",
          "rate": "17,838.2543",
          "description": "British Pound Sterling",
          "rate_float": 17838.2543
        },
        "EUR": {
          "code": "EUR",
          "symbol": "&euro;",
          "rate": "20,796.1111",
          "description": "Euro",
          "rate_float": 20796.1111
        }
      }
    }
""".trimIndent()
