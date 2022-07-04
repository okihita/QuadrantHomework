package com.okihita.quadranthomework.data.entities

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.okihita.quadranthomework.data.local.PriceIndexMapConverter

@Entity(tableName = "price_index")
data class PriceIndexResponse(

    @PrimaryKey(autoGenerate = true)
    val id: Int,

    @Embedded
    val time: CoinDeskTime,
    val disclaimer: String,
    val chartName: String,

    @TypeConverters(PriceIndexMapConverter::class)
    val bpi: Map<String, PriceIndex>
) {

    data class CoinDeskTime(
        val updated: String,
        val updatedISO: String,
        val updateduk: String
    )

    data class PriceIndex(
        val code: String,
        val symbol: String,
        val rate: String,
        val description: String,
        val rate_float: Float
    )
}