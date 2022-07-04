package com.okihita.quadranthomework.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.okihita.quadranthomework.data.entities.PriceIndexResponse

@Dao
interface PriceIndexDao {

    @Insert
    suspend fun addPriceIndex(priceIndexResponse: PriceIndexResponse)

    @Query("SELECT * FROM price_index")
    suspend fun getAllPriceIndices(): List<PriceIndexResponse>

}