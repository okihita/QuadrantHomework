package com.okihita.quadranthomework.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.okihita.quadranthomework.data.entities.PriceIndexResponse

@Dao
interface PriceIndexDao {

    @Insert
    suspend fun addPriceIndexResponse(priceIndexResponse: PriceIndexResponse)

    @Query("SELECT * FROM price_index")
    suspend fun getAllPriceIndices(): List<PriceIndexResponse>

    @Query("DELETE FROM price_index")
    suspend fun deleteAll()
}