package com.okihita.quadranthomework.utils

import androidx.lifecycle.MutableLiveData

fun <T> MutableLiveData<T>.refresh() {
    this.value = this.value // Calls MLD with the same value to trigger observer e.g. for UI updates
}