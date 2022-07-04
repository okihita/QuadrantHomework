package com.okihita.quadranthomework.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.okihita.quadranthomework.data.entities.PriceIndexResponse
import com.okihita.quadranthomework.data.local.PriceIndexDatabase
import com.okihita.quadranthomework.data.remote.CoinDeskApi
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CoinDeskViewModel @Inject constructor(
    private val coinDeskApi: CoinDeskApi,
    private val priceIndexDatabase: PriceIndexDatabase
) : ViewModel() {

    private val _priceIndexResponse = MutableLiveData<PriceIndexResponse>()
    val priceIndexResponse: LiveData<PriceIndexResponse> = _priceIndexResponse

    private val _roomItemResponse = MutableLiveData<String>()
    val roomItemResponse = _roomItemResponse

    fun callCoinDeskApi() {
        viewModelScope.launch {
            try {

                val priceIndexResponse = coinDeskApi.getCurrentPrice()
                _priceIndexResponse.value = priceIndexResponse
                priceIndexDatabase.priceIndexDao().addPriceIndex(priceIndexResponse)

                val roomItem = priceIndexDatabase.priceIndexDao().getAllPriceIndices().first()
                _roomItemResponse.value = roomItem.bpi["USD"]?.rate ?: "No rate available"

            } catch (e: Exception) {
                e.printStackTrace() // Network error or server error
            }
        }
    }
}