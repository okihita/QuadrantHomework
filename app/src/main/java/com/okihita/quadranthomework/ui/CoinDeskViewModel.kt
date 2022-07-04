package com.okihita.quadranthomework.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.okihita.quadranthomework.data.entities.CoinDeskResponse
import com.okihita.quadranthomework.data.remote.CoinDeskApi
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CoinDeskViewModel @Inject constructor(
    private val coinDeskApi: CoinDeskApi
) : ViewModel() {

    private val _coinDeskResponse = MutableLiveData<CoinDeskResponse>()
    val coinDeskResponse: LiveData<CoinDeskResponse> = _coinDeskResponse

    fun callCoinDeskApi() {
        viewModelScope.launch {
            try {
                _coinDeskResponse.value = coinDeskApi.getCurrentPrice()
            } catch (e: Exception) {
                e.printStackTrace() // Network error or server error
            }
        }
    }
}