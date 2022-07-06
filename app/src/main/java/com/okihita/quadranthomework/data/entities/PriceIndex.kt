package com.okihita.quadranthomework.data.entities

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.okihita.quadranthomework.data.local.PriceIndexMapConverter
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Entity(tableName = "price_index")
data class PriceIndex(

    @PrimaryKey(autoGenerate = true)
    val id: Int,

    @Embedded
    val time: CoinDeskTime,

    @TypeConverters(PriceIndexMapConverter::class)
    val bpi: Map<String, BitcoinPriceIndex>
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
}

fun PriceIndex.getDateTime(): LocalDateTime {
    return LocalDateTime.parse(this.time.updatedISO, DateTimeFormatter.ISO_DATE_TIME)
}