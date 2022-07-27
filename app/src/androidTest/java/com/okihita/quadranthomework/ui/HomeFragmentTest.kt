package com.okihita.quadranthomework.ui

import android.content.Context
import android.util.Log
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.work.Configuration
import androidx.work.testing.SynchronousExecutor
import androidx.work.testing.WorkManagerTestInitHelper
import com.google.common.truth.Truth
import com.okihita.quadranthomework.R
import com.okihita.quadranthomework.data.local.PriceIndexDatabase
import com.okihita.quadranthomework.launchFragmentInHiltContainer
import com.okihita.quadranthomework.utils.fromDeviceToUtc
import com.okihita.quadranthomework.utils.generateTodayUtcPriceIndices
import com.okihita.quadranthomework.utils.getCurrentDeviceZonedDateTime
import com.okihita.quadranthomework.utils.toDateString
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import org.hamcrest.Matchers.containsString
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import javax.inject.Inject

@ExperimentalCoroutinesApi
@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class HomeFragmentTest {

    @get:Rule
    val hiltAndroidRule = HiltAndroidRule(this)

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @Inject
    lateinit var database: PriceIndexDatabase

    @Before
    fun setup() {

        // https://developer.android.com/topic/libraries/architecture/workmanager/how-to/integration-testing#concepts
        val context = ApplicationProvider.getApplicationContext<Context>()
        val config = Configuration.Builder()
            .setMinimumLoggingLevel(Log.DEBUG)
            .setExecutor(SynchronousExecutor())
            .build()
        WorkManagerTestInitHelper.initializeTestWorkManager(context, config)

        hiltAndroidRule.inject()

        launchFragmentInHiltContainer<HomeFragment>()
    }

    @Test
    fun onStart_viewElementsShown() {
        onView(withText("Bitcoin Price Index")) // Title shown
            .check(matches(isDisplayed()))

        val dateString = getCurrentDeviceZonedDateTime().fromDeviceToUtc().toDateString() + " (UTC)"
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
    fun savingItemsToDatabase_databaseContainsItems() = runBlocking {

        val priceIndices = generateTodayUtcPriceIndices()

        priceIndices.forEach { database.priceIndexDao.addPriceIndex(it) }
        val dbPriceIndices = database.priceIndexDao.getAllPriceIndices()

        Truth.assertThat(dbPriceIndices).isNotEmpty()
        Truth.assertThat(dbPriceIndices).hasSize(24)
        Truth.assertThat(dbPriceIndices.first()).isEqualTo(priceIndices.first())
        Truth.assertThat(dbPriceIndices.last()).isEqualTo(priceIndices.last())
    }

    @Test
    fun withCustomDbContent_onEachFlagClick_rvChangesToRelevantRates() {

        runBlocking {
            generateTodayUtcPriceIndices().forEach { database.priceIndexDao.addPriceIndex(it) }
        }

        onView(withId(R.id.btRefresh)).perform(click())

        // After clicking the GBP flag, the RV should have at least one item which has
        // TextView containing "GBP" substring
        onView(withId(R.id.ivGBP)).perform(click())
        onView(withId(R.id.rvRates))
            .check(matches(hasDescendant(withText(containsString("GBP")))))

        onView(withId(R.id.ivEUR)).perform(click())
        onView(withId(R.id.rvRates))
            .check(matches(hasDescendant(withText(containsString("EUR")))))

        onView(withId(R.id.ivUSD)).perform(click())
        onView(withId(R.id.rvRates))
            .check(matches(hasDescendant(withText(containsString("USD")))))
    }
}