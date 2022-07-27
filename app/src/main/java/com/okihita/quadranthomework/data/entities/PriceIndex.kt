package com.okihita.quadranthomework.data.entities

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.okihita.quadranthomework.data.local.PriceIndexMapConverter
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

@Entity(tableName = "price_index")
data class PriceIndex(

    @Embedded
    val time: CoinDeskTime,

    @TypeConverters(PriceIndexMapConverter::class)
    val bpi: Map<String, BitcoinPriceIndex>,

    @Embedded
    var location: DeviceLocation?

) {
    @PrimaryKey(autoGenerate = true)
    var id: Long = 0

    data class CoinDeskTime(
        var updatedISO: String,
    )

    data class BitcoinPriceIndex(
        val code: String,
        val symbol: String,
        var rate: String,
        val description: String,
        var rate_float: Float
    )

    data class DeviceLocation(
        var latitude: Double? = 0.0,
        var longitude: Double? = 0.0,
        var address: String? = ""
    )
}

fun PriceIndex.getUTCZonedDateTime(): ZonedDateTime {
    val utcLocalDateTime: LocalDateTime = LocalDateTime.parse(time.updatedISO, DateTimeFormatter.ISO_DATE_TIME)
    return utcLocalDateTime.atZone(ZoneId.of("UTC"))
}