package com.okihita.quadranthomework.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.okihita.quadranthomework.data.entities.PriceIndex

@Database(entities = [PriceIndex::class], version = 1, exportSchema = false)
@TypeConverters(PriceIndexMapConverter::class)
abstract class PriceIndexDatabase : RoomDatabase() {

    abstract fun priceIndexDao(): PriceIndexDao
}