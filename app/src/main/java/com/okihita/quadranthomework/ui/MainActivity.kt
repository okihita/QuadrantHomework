package com.okihita.quadranthomework.ui

import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.okihita.quadranthomework.R
import com.okihita.quadranthomework.databinding.ActivityMainBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private val coinDeskViewModel: CoinDeskViewModel by viewModels()

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(R.layout.activity_main)

        // Setup observer
        coinDeskViewModel.coinDeskResponse.observe(this) {
            Toast.makeText(this, it.disclaimer, Toast.LENGTH_SHORT).show()
        }

        // Call VM function
        coinDeskViewModel.callCoinDeskApi()
    }
}