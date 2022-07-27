package com.okihita.quadranthomework.data.repository

import com.okihita.quadranthomework.data.entities.PriceIndex
import com.okihita.quadranthomework.data.local.PriceIndexDatabase
import com.okihita.quadranthomework.data.remote.CoinDeskApi
import javax.inject.Inject

class CoinDeskRepository @Inject constructor(
    private val api: CoinDeskApi,
    private val database: PriceIndexDatabase
) {

    suspend fun callCoinDeskApi() =
        api.getCurrentPrice()

    suspend fun insertPriceIndex(priceIndex: PriceIndex) =
        database.priceIndexDao.addPriceIndex(priceIndex)

    suspend fun getAllPriceIndices() =
        database.priceIndexDao.getAllPriceIndices()

    suspend fun getNewestPriceIndex() =
        database.priceIndexDao.getNewestPriceIndex()
}