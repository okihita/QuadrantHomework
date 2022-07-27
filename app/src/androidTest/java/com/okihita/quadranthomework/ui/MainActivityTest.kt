package com.okihita.quadranthomework.ui

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.rule.GrantPermissionRule
import com.okihita.quadranthomework.R
import org.hamcrest.Matchers.anyOf
import org.hamcrest.Matchers.startsWith
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@LargeTest
class MainActivityTest {

    @get:Rule
    val activityRule = ActivityScenarioRule(MainActivity::class.java)

    @Rule
    @JvmField
    val grantPermissionRule: GrantPermissionRule = GrantPermissionRule.grant(
        "android.permission.ACCESS_FINE_LOCATION",
        "android.permission.ACCESS_COARSE_LOCATION",
        "android.permission.ACCESS_BACKGROUND_LOCATION"
    )

    @Test
    fun onStart_bitcoinPriceIndexShown() {
        onView(withText("Bitcoin Price Index")).check(matches(isDisplayed()))
    }

    @Test
    fun onStart_timeShown() {
        onView(withText("refresh")).check(matches(isDisplayed()))
    }

    @Test
    fun onGbpFlagClick_recyclerViewContainsGbp() {

        // When
        onView(withId(R.id.ivGBP)).perform(click())
        // Then
        onView(withText("GBP")).check(matches(isDisplayed()))
    }

    @Test
    fun onEurFlagClick_recyclerViewContainsEur() {

        // When
        onView(withId(R.id.ivEUR)).perform(click())
        // Then
        onView(withText("EUR")).check(matches(isDisplayed()))
    }


}