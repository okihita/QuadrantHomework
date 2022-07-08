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

    @PrimaryKey(autoGenerate = true)
    val id: Int,

    @Embedded
    val time: CoinDeskTime,

    @TypeConverters(PriceIndexMapConverter::class)
    val bpi: Map<String, BitcoinPriceIndex>,

    @Embedded
    var location: DeviceLocation?

) {

    data class CoinDeskTime(
        val updatedISO: String,
    )

    data class BitcoinPriceIndex(
        val code: String,
        val symbol: String,
        val rate: String,
        val description: String,
        val rate_float: Float
    )

    data class DeviceLocation(
        var latitude: Double? = 0.0,
        var longitude: Double? = 0.0,
        var address: String? = ""
    )
}

fun PriceIndex.getISOZonedDateTime(): ZonedDateTime {
    val utcLocalDateTime =
        LocalDateTime.parse(time.updatedISO, DateTimeFormatter.ISO_OFFSET_DATE_TIME)
    return utcLocalDateTime.atZone(ZoneId.of("UTC"))
}