package com.okihita.quadranthomework.data.repository

import com.okihita.quadranthomework.data.entities.PriceIndexResponse
import com.okihita.quadranthomework.data.local.PriceIndexDatabase
import com.okihita.quadranthomework.data.remote.CoinDeskApi
import javax.inject.Inject

class CoinDeskRepository @Inject constructor(
    private val api: CoinDeskApi,
    private val database: PriceIndexDatabase
) {

    suspend fun callCoinDeskApi() = api.getCurrentPrice()

    suspend fun insertPriceIndexResponse(priceIndexResponse: PriceIndexResponse) =
        database.priceIndexDao().addPriceIndexResponse(priceIndexResponse)

    suspend fun getAllPriceIndexResponse() = database.priceIndexDao().getAllPriceIndices()

    suspend fun cleanDatabase() = database.priceIndexDao().deleteAll()
}