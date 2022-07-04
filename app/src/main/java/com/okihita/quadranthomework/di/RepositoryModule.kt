package com.okihita.quadranthomework.di

import com.okihita.quadranthomework.data.local.PriceIndexDatabase
import com.okihita.quadranthomework.data.remote.CoinDeskApi
import com.okihita.quadranthomework.data.repository.CoinDeskRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    @Singleton
    @Provides
    fun providePriceIndexRepository(
        api: CoinDeskApi,
        database: PriceIndexDatabase
    ) = CoinDeskRepository(api, database)
}