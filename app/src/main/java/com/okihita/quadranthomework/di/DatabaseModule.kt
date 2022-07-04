package com.okihita.quadranthomework.di

import android.content.Context
import androidx.room.Room
import com.okihita.quadranthomework.data.local.PriceIndexDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun providePriceIndexDatabase(
        @ApplicationContext context: Context
    ) = Room.databaseBuilder(
        context,
        PriceIndexDatabase::class.java,
        "price_index_db"
    ).build()
}