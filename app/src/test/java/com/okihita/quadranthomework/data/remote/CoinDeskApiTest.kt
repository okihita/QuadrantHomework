package com.okihita.quadranthomework.data.remote

import com.google.common.truth.Truth.assertThat
import com.google.gson.Gson
import com.okihita.quadranthomework.data.entities.PriceIndex
import kotlinx.coroutines.runBlocking
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

@RunWith(JUnit4::class)
class CoinDeskApiTest {

    private lateinit var coinDeskApi: CoinDeskApi
    private lateinit var mockServer: MockWebServer

    @Before
    fun setup() {
        mockServer = MockWebServer()
        coinDeskApi = Retrofit.Builder()
            .baseUrl(mockServer.url(""))
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(CoinDeskApi::class.java)
    }

    @After
    fun teardown() {
        mockServer.shutdown()
    }

    @Test
    fun getCurrentPriceIndex_apiSuccess_receivedCorrectly() = runBlocking {

        val mockResponse = MockResponse().setBody(latestPriceIndexJson)
        val gsonObject = Gson().fromJson(latestPriceIndexJson, PriceIndex::class.java)
        mockServer.enqueue(mockResponse)

        val response = coinDeskApi.getCurrentPrice()

        assertThat(response).isNotNull()
        assertThat(response).isEqualTo(gsonObject)
    }

    private val latestPriceIndexJson = """
    {
      "time": {
        "updated": "Jul 27, 2022 07:45:00 UTC",
        "updatedISO": "2022-07-27T07:45:00+00:00",
        "updateduk": "Jul 27, 2022 at 08:45 BST"
      },
      "disclaimer": "This data was produced from the CoinDesk Bitcoin Price Index (USD). Non-USD currency data converted using hourly conversion rate from openexchangerates.org",
      "chartName": "Bitcoin",
      "bpi": {
        "USD": {
          "code": "USD",
          "symbol": "&#36;",
          "rate": "21,348.0434",
          "description": "United States Dollar",
          "rate_float": 21348.0434
        },
        "GBP": {
          "code": "GBP",
          "symbol": "&pound;",
          "rate": "17,838.2543",
          "description": "British Pound Sterling",
          "rate_float": 17838.2543
        },
        "EUR": {
          "code": "EUR",
          "symbol": "&euro;",
          "rate": "20,796.1111",
          "description": "Euro",
          "rate_float": 20796.1111
        }
      }
    }
""".trimIndent()

}