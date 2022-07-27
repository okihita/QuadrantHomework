package com.okihita.quadranthomework.data.local

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SmallTest
import com.google.common.truth.Truth.assertThat
import com.okihita.quadranthomework.utils.generateTodayUtcPriceIndices
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
@SmallTest
class PriceIndexDaoTest {

    private lateinit var database: PriceIndexDatabase
    private lateinit var dao: PriceIndexDao

    @Before
    fun setup() {
        database = Room
            .inMemoryDatabaseBuilder(
                ApplicationProvider.getApplicationContext(),
                PriceIndexDatabase::class.java
            )
            .allowMainThreadQueries()
            .build()

        dao = database.priceIndexDao
    }

    @After
    fun teardown() {
        database.clearAllTables()
        database.close()
    }

    @Test
    fun insertOnePriceIndex_loadOnePriceIndex() = runBlocking {

        val priceIndex = generateTodayUtcPriceIndices().first()
        dao.addPriceIndex(priceIndex)

        val dbPriceIndices = dao.getAllPriceIndices()
        assertThat(dbPriceIndices).hasSize(1)
    }

    @Test
    fun insert24PriceIndices_allUniqueHours_load24PriceIndices() = runBlocking {

        val priceIndices = generateTodayUtcPriceIndices().shuffled()
        priceIndices.forEach { dao.addPriceIndex(it) }

        val dbPriceIndices = dao.getAllPriceIndices()
        assertThat(dbPriceIndices).hasSize(24)
    }

    @Test
    fun databaseCleared_zeroItemReturned() = runBlocking {

        val priceIndices = generateTodayUtcPriceIndices().shuffled()
        priceIndices.forEach { dao.addPriceIndex(it) }

        dao.deleteAll()

        val allPriceIndices = dao.getAllPriceIndices()
        assertThat(allPriceIndices).isEmpty()
    }

    @Test
    fun fewItemsAdded_newestItemIsTheLast() = runBlocking {

        val priceIndices = generateTodayUtcPriceIndices().shuffled()
        priceIndices.forEach { dao.addPriceIndex(it) }

        val newestPriceIndex = dao.getNewestPriceIndex()
        assertThat(newestPriceIndex.time.updatedISO).isEqualTo(priceIndices.last().time.updatedISO)
    }
}