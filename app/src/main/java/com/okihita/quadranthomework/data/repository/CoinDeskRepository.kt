package com.okihita.quadranthomework.data.repository

import com.okihita.quadranthomework.data.entities.PriceIndex
import com.okihita.quadranthomework.data.local.PriceIndexDatabase
import com.okihita.quadranthomework.data.remote.CoinDeskApi
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

open class CoinDeskRepository @Inject constructor(
    private val api: CoinDeskApi,
    private val database: PriceIndexDatabase
) {

    open suspend fun callCoinDeskApi(): PriceIndex = api.getCurrentPrice()

    suspend fun insertPriceIndex(priceIndex: PriceIndex): Long =
        database.priceIndexDao.addPriceIndex(priceIndex)

    fun getAllPriceIndices(): Flow<List<PriceIndex>> =
        database.priceIndexDao.getAllPriceIndices()

    suspend fun getNewestPriceIndex(): PriceIndex =
        database.priceIndexDao.getNewestPriceIndex()
}