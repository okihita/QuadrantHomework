package com.okihita.quadranthomework.data.remote

import com.okihita.quadranthomework.data.entities.PriceIndex
import retrofit2.http.GET

interface CoinDeskApi {

    @GET("/v1/bpi/currentprice.json")
    suspend fun getCurrentPrice(): PriceIndex
}