package com.okihita.quadranthomework.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.okihita.quadranthomework.data.entities.PriceIndexResponse

@Database(entities = [PriceIndexResponse::class], version = 1, exportSchema = false)
@TypeConverters(PriceIndexMapConverter::class)
abstract class PriceIndexDatabase : RoomDatabase() {

    abstract fun priceIndexDao(): PriceIndexDao
}