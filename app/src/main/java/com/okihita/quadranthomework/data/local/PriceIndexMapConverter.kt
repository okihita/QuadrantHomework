package com.okihita.quadranthomework.data.local

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.okihita.quadranthomework.data.entities.PriceIndex

object PriceIndexMapConverter {

    @TypeConverter
    @JvmStatic
    fun stringToMap(value: String): Map<String, PriceIndex.BitcoinPriceIndex> {
        return Gson().fromJson(value,  object : TypeToken<Map<String, PriceIndex.BitcoinPriceIndex>>() {}.type)
    }

    @TypeConverter
    @JvmStatic
    fun mapToString(value: Map<String, PriceIndex.BitcoinPriceIndex>?): String {
        return if(value == null) "" else Gson().toJson(value)
    }
}
