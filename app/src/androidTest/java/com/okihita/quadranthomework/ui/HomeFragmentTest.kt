package com.okihita.quadranthomework.ui

import android.util.Log
import android.util.Pair
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.GrantPermissionRule
import androidx.work.Configuration
import androidx.work.testing.SynchronousExecutor
import androidx.work.testing.WorkManagerTestInitHelper
import com.google.common.truth.Truth
import com.okihita.quadranthomework.R
import com.okihita.quadranthomework.data.entities.PriceIndex
import com.okihita.quadranthomework.data.local.PriceIndexDatabase
import com.okihita.quadranthomework.launchFragmentInHiltContainer
import com.okihita.quadranthomework.utils.fromDeviceToUtc
import com.okihita.quadranthomework.utils.generate24HourPriceIndices
import com.okihita.quadranthomework.utils.getSystemZonedDateTime
import com.okihita.quadranthomework.utils.toDateString
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.hamcrest.Matchers.containsString
import org.junit.*
import org.junit.runner.RunWith
import org.junit.runners.MethodSorters
import javax.inject.Inject
import kotlin.random.Random

@ExperimentalCoroutinesApi
@HiltAndroidTest
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@RunWith(AndroidJUnit4::class)
class HomeFragmentTest {

    @get:Rule
    val hiltAndroidRule = HiltAndroidRule(this)

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    val grantPermissionRule: GrantPermissionRule = GrantPermissionRule.grant(
        android.Manifest.permission.ACCESS_COARSE_LOCATION,
        android.Manifest.permission.ACCESS_FINE_LOCATION,
        android.Manifest.permission.ACCESS_BACKGROUND_LOCATION,
    )

    @Inject
    lateinit var database: PriceIndexDatabase

    @Before
    fun setup() {

        // https://developer.android.com/topic/libraries/architecture/workmanager/how-to/integration-testing#concepts
        val config = Configuration.Builder()
            .setMinimumLoggingLevel(Log.DEBUG)
            .setExecutor(SynchronousExecutor())
            .build()
        WorkManagerTestInitHelper.initializeTestWorkManager(
            ApplicationProvider.getApplicationContext(),
            config
        )

        hiltAndroidRule.inject()

        launchFragmentInHiltContainer<HomeFragment>()
    }

    @After
    fun teardown() {
        database.clearAllTables()
        database.close()
    }

    @Test
    fun test01_onStart_viewElementsShown() {

        val dateString = getSystemZonedDateTime().fromDeviceToUtc().toDateString() + " (UTC)"
        onView(withId(R.id.tvDate)) // Date shown with the correct UTC timezone
            .check(matches(withText(dateString)))

        onView(withId(R.id.chart)) // Chart is shown
            .check(matches(isDisplayed()))

        // All flag images are shown
        onView(withId(R.id.ivUSD)).check(matches(isDisplayed()))
        onView(withId(R.id.ivGBP)).check(matches(isDisplayed()))
        onView(withId(R.id.ivEUR)).check(matches(isDisplayed()))

        // All flag images are clickable
        onView(withId(R.id.ivUSD)).check(matches(isClickable()))
        onView(withId(R.id.ivGBP)).check(matches(isClickable()))
        onView(withId(R.id.ivEUR)).check(matches(isClickable()))

        // RV and refresh button are displayed
        onView(withId(R.id.rvRates)).check(matches(isDisplayed()))
        onView(withId(R.id.btRefresh)).check(matches(withText("REFRESH")))
    }

    @Test
    fun test02_savingItemsToDatabase_databaseContains24UniqueItems() = runBlocking {

        val priceIndices: List<PriceIndex> = generate24HourPriceIndices()

        priceIndices.forEach { database.priceIndexDao.addPriceIndex(it) }
        val dbPriceIndices: List<PriceIndex> = database.priceIndexDao.getAllPriceIndices().first()

        Truth.assertThat(dbPriceIndices).isNotEmpty()
        Truth.assertThat(dbPriceIndices).hasSize(24)
        Truth.assertThat(dbPriceIndices.first()).isEqualTo(priceIndices.first())
        Truth.assertThat(dbPriceIndices.last()).isEqualTo(priceIndices.last())
        Truth.assertThat(dbPriceIndices.first()).isNotEqualTo(priceIndices.last())
    }

    @Test
    fun test03_savingOneItemToDatabase_chartUpdatedListUpdated() {

        val priceIndex = generate24HourPriceIndices().random()
        runBlocking { database.priceIndexDao.addPriceIndex(priceIndex) }

        val priceIndexRate = priceIndex.bpi["USD"]!!.rate_float.toString()

        onView(withId(R.id.rvRates)) // RV must contain at least one item with "TIME" text
            .check(matches(hasDescendant(withText(containsString(priceIndexRate)))))

        onView(withText(containsString(priceIndexRate))) // A chart-point text with rate is shown
            .check(matches(isDisplayed()))
    }

    @Test
    fun test04_addingFourItemsEachSecondUntil24_usdChartUpdatedListUpdated() {

        val priceIndices = generate24HourPriceIndices()

        priceIndices.forEach { priceIndex ->
            val priceIndexRateString = priceIndex.bpi["USD"]!!.rate_float.toString()
            val priceIndexRateFloat = priceIndex.bpi["USD"]!!.rate

            Thread.sleep(1000)
            runBlocking { database.priceIndexDao.addPriceIndex(priceIndex) }

            onView(withText(containsString(priceIndexRateString)))
            onView(withId(R.id.rvRates))
                .check(matches(hasDescendant(withText(containsString(priceIndexRateFloat)))))
        }
    }

    @Test
    fun test05_withTestDbContent_onEachFlagClick_listChangesToRelevantRates() {

        generate24HourPriceIndices().forEach { priceIndex ->
            runBlocking { database.priceIndexDao.addPriceIndex(priceIndex) }
        }

        // After clicking any flag, the RV should have at least one item which has
        // TextView containing the flag's corresponding currency.
        onView(withId(R.id.ivGBP)).perform(click())
        onView(withId(R.id.rvRates))
            .check(matches(hasDescendant(withText(containsString("GBP")))))

        onView(withId(R.id.ivEUR)).perform(click())
        onView(withId(R.id.rvRates))
            .check(matches(hasDescendant(withText(containsString("EUR")))))

        onView(withId(R.id.ivUSD)).perform(click())
        onView(withId(R.id.rvRates))
            .check(matches(hasDescendant(withText(containsString("USD")))))

        // Do multiple times, randomized
        val flagCurrencyPairs = listOf<Pair<Int, String>>(
            Pair(R.id.ivGBP, "GBP"),
            Pair(R.id.ivEUR, "EUR"),
            Pair(R.id.ivUSD, "USD")
        )
        repeat(10) {
            val randomPair = flagCurrencyPairs[Random.nextInt(0, 2)]

            Thread.sleep(250)
            onView(withId(randomPair.first)).perform(click())
            onView(withId(R.id.rvRates))
                .check(matches(hasDescendant(withText(containsString(randomPair.second)))))
        }
    }
}