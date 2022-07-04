package com.okihita.quadranthomework.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.okihita.quadranthomework.data.entities.PriceIndexResponse
import com.okihita.quadranthomework.data.local.PriceIndexDatabase
import com.okihita.quadranthomework.data.remote.CoinDeskApi
import com.okihita.quadranthomework.data.repository.CoinDeskRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CoinDeskViewModel @Inject constructor(
    private val repository: CoinDeskRepository
) : ViewModel() {

    private val _priceIndexResponse = MutableLiveData<PriceIndexResponse>()
    val priceIndexResponse: LiveData<PriceIndexResponse> = _priceIndexResponse

    private val _roomItemResponse = MutableLiveData<String>()
    val roomItemResponse = _roomItemResponse

    fun callCoinDeskApi() {
        viewModelScope.launch {
            try {

                val priceIndexResponse = repository.callCoinDeskApi()
                _priceIndexResponse.value = priceIndexResponse

                repository.insertPriceIndexResponse(priceIndexResponse)
                val roomItem = repository.getAllPriceIndexResponse().first()
                _roomItemResponse.value = roomItem.bpi["USD"]?.rate ?: "No rate available"

            } catch (e: Exception) {
                e.printStackTrace() // Network error or server error
            }
        }
    }
}