package com.okihita.quadranthomework.data.local

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.okihita.quadranthomework.data.entities.PriceIndexResponse

object PriceIndexMapConverter {

    @TypeConverter
    @JvmStatic
    fun stringToMap(value: String): Map<String, PriceIndexResponse.PriceIndex> {
        return Gson().fromJson(value,  object : TypeToken<Map<String, PriceIndexResponse.PriceIndex>>() {}.type)
    }

    @TypeConverter
    @JvmStatic
    fun mapToString(value: Map<String, PriceIndexResponse.PriceIndex>?): String {
        return if(value == null) "" else Gson().toJson(value)
    }
}
