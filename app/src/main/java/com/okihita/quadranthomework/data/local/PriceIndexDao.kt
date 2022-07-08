package com.okihita.quadranthomework.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.okihita.quadranthomework.data.entities.PriceIndex

@Dao
interface PriceIndexDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addPriceIndexResponse(priceIndex: PriceIndex): Long // Returns row id if success

    @Query("SELECT * FROM price_index")
    suspend fun getAllPriceIndices(): List<PriceIndex>

    @Query(
        "SELECT * FROM price_index " +
                "ORDER BY id DESC " +
                "LIMIT 1"
    )
    suspend fun getNewestPriceIndex(): PriceIndex

    @Query("DELETE FROM price_index")
    suspend fun deleteAll()
}