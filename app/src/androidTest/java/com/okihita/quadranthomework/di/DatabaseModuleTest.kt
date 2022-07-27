package com.okihita.quadranthomework.di

import android.content.Context
import androidx.room.Room
import com.okihita.quadranthomework.data.local.PriceIndexDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn
import javax.inject.Singleton

@Module
@TestInstallIn(
    components = [SingletonComponent::class],
    replaces = [DatabaseModule::class]
)
object DatabaseModuleTest {

    @Singleton
    @Provides
    fun provideTestDatabase(@ApplicationContext context: Context) = Room
        .inMemoryDatabaseBuilder(context, PriceIndexDatabase::class.java)
        .build()
}