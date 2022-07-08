package com.okihita.quadranthomework.data.local

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SmallTest
import com.google.common.truth.Truth.assertThat
import com.okihita.quadranthomework.data.entities.PriceIndex
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
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
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            PriceIndexDatabase::class.java
        )
            .allowMainThreadQueries()
            .build()

        dao = database.priceIndexDao()
    }

    @After
    fun teardown() {
        database.close()
    }

    @Test
    fun insertPriceIndex() = runTest {
        val priceIndex = PriceIndex(
            4,
            PriceIndex.CoinDeskTime(
                "2022-07-08T09:09:00+00:00"
            ),
            mapOf(
                Pair(
                    "USD", PriceIndex.BitcoinPriceIndex(
                        "USD",
                        "&#36;",
                        "21,379.7366",
                        "United States Dollar",
                        21379.7366f
                    )
                )
            ),
            null
        )
        dao.addPriceIndexResponse(priceIndex)

        val allPriceIndices = dao.getAllPriceIndices()
        assertThat(allPriceIndices).contains(priceIndex)
    }

    @Test
    fun clearDatabase() = runTest {
        val priceIndex = PriceIndex(
            1,
            PriceIndex.CoinDeskTime(
                "2022-07-08T09:09:00+00:00"
            ),
            mapOf(
                Pair(
                    "USD", PriceIndex.BitcoinPriceIndex(
                        "USD",
                        "&#36;",
                        "21,379.7366",
                        "United States Dollar",
                        21379.7366f
                    )
                )
            ),
            null
        )
        dao.addPriceIndexResponse(priceIndex)
        dao.addPriceIndexResponse(priceIndex)
        dao.deleteAll()

        val allPriceIndices = dao.getAllPriceIndices()
        assertThat(allPriceIndices).doesNotContain(priceIndex)
    }

    @Test
    fun getNewestItem() = runTest {

        val bip = mapOf(
            Pair(
                "USD", PriceIndex.BitcoinPriceIndex(
                    "USD", "&#36;", "21,379.7366", "United States Dollar", 21379.7366f
                )
            )
        )

        val priceIndex1 = PriceIndex(
            1,
            PriceIndex.CoinDeskTime(
                "2022-07-08T09:09:00+00:00"
            ),
            bip,
            null
        )

        val priceIndex2 = PriceIndex(
            2,
            PriceIndex.CoinDeskTime(
                "2022-07-08T09:09:00+00:00"
            ),
            bip,
            null
        )

        val priceIndex3 = PriceIndex(
            3,
            PriceIndex.CoinDeskTime(
                "2022-07-08T09:09:00+00:00"
            ),
            bip,
            null
        )

        dao.addPriceIndexResponse(priceIndex2)
        dao.addPriceIndexResponse(priceIndex3)
        dao.addPriceIndexResponse(priceIndex1)

        val newestPriceIndex = dao.getNewestPriceIndex()
        assertThat(newestPriceIndex).isEqualTo(priceIndex3)
    }
}