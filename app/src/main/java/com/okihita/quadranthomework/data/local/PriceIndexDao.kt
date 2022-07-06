package com.okihita.quadranthomework.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.okihita.quadranthomework.data.entities.PriceIndex

@Dao
interface PriceIndexDao {

    @Insert
    suspend fun addPriceIndexResponse(priceIndex: PriceIndex)

    @Query("SELECT * FROM price_index")
    suspend fun getAllPriceIndices(): List<PriceIndex>

    @Query("DELETE FROM price_index")
    suspend fun deleteAll()
}