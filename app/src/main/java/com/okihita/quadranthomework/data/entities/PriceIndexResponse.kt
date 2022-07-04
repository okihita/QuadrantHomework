package com.okihita.quadranthomework.data.entities

data class PriceIndexResponse(
    val time: CoinDeskTime,
    val disclaimer: String,
    val chartName: String,
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