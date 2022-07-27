package com.okihita.quadranthomework.workers

import android.content.Context
import androidx.hilt.work.HiltWorkerFactory
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.GrantPermissionRule
import androidx.work.ListenableWorker
import androidx.work.WorkerFactory
import androidx.work.WorkerParameters
import androidx.work.await
import androidx.work.testing.TestListenableWorkerBuilder
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.common.truth.Truth
import com.okihita.quadranthomework.data.entities.PriceIndex
import com.okihita.quadranthomework.data.local.PriceIndexDatabase
import com.okihita.quadranthomework.data.remote.CoinDeskApi
import com.okihita.quadranthomework.data.repository.CoinDeskRepository
import com.okihita.quadranthomework.utils.generate24HourPriceIndices
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.`when`
import org.mockito.kotlin.mock
import javax.inject.Inject

@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class PriceLocationUpdateWorkerTest {

    @get:Rule
    val hiltAndroidRule = HiltAndroidRule(this)

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    @Inject
    lateinit var database: PriceIndexDatabase

    @Inject
    lateinit var locationClient: FusedLocationProviderClient

    private lateinit var context: Context

    @get:Rule
    val grantPermissionRule: GrantPermissionRule = GrantPermissionRule.grant(
        android.Manifest.permission.ACCESS_COARSE_LOCATION,
        android.Manifest.permission.ACCESS_FINE_LOCATION,
        android.Manifest.permission.ACCESS_BACKGROUND_LOCATION,
    )

    @Before
    fun setup() {
        hiltAndroidRule.inject()
        context = ApplicationProvider.getApplicationContext()
    }

    @After
    fun teardown() {
        database.clearAllTables()
        database.close()
    }

    @Test
    fun workStarted_defaultDependencies_resultSuccessDatabasePopulated() {

        runBlocking {

            val worker = TestListenableWorkerBuilder<PriceLocationUpdateWorker>(context)
                .setWorkerFactory(workerFactory)
                .build()

            val result = worker.startWork().await()

            Truth.assertThat(result).isEqualTo(ListenableWorker.Result.success())
            val dbResult: List<PriceIndex> = database.priceIndexDao.getAllPriceIndices().first()
            Truth.assertThat(dbResult).hasSize(1)
        }
    }

    @Test
    fun workStarted_mockedApiResponse_resultSuccessDatabasePopulatedEqualItems() {

        val mockedApi: CoinDeskApi = mock()
        val mockedApiRepository = CoinDeskRepository(mockedApi, database)
        val mockPriceIndex = generate24HourPriceIndices().random()

        val mockingFactory = object : WorkerFactory() {
            override fun createWorker(
                appContext: Context,
                workerClassName: String,
                workerParameters: WorkerParameters
            ): ListenableWorker = PriceLocationUpdateWorker(
                appContext, workerParameters, mockedApiRepository, locationClient
            )
        }

        runBlocking {

            // Assert empty database before any operations
            Truth.assertThat(mockedApiRepository.getAllPriceIndices().first()).hasSize(0)

            `when`(mockedApi.getCurrentPrice()).thenReturn(mockPriceIndex)

            val worker = TestListenableWorkerBuilder<PriceLocationUpdateWorker>(context)
                .setWorkerFactory(mockingFactory)
                .build()

            // Result is success
            val result = worker.startWork().await()
            Truth.assertThat(result).isEqualTo(ListenableWorker.Result.success())

            // Database populated with exactly one item
            val dbResult: List<PriceIndex> = mockedApiRepository.getAllPriceIndices().first()
            Truth.assertThat(dbResult).hasSize(1)

            // DB item equals the mocked (random) price index
            val latestDbPriceIndex: PriceIndex = dbResult.first()
            Truth.assertThat(latestDbPriceIndex).isEqualTo(mockPriceIndex)
        }
    }
}