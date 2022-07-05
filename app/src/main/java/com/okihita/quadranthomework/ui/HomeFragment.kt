package com.okihita.quadranthomework.ui

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.okihita.quadranthomework.R
import com.okihita.quadranthomework.databinding.FragmentHomeBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class HomeFragment : Fragment(R.layout.fragment_home) {

    private val coinDeskVM by viewModels<CoinDeskViewModel>()

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentHomeBinding.bind(view)

        coinDeskVM.latestPriceIndex.observe(viewLifecycleOwner) { latestPriceIndex ->

            binding.tvHello.text = "Latest price for USD:\n" +
                    latestPriceIndex.bpi["USD"]?.rate
        }

        coinDeskVM.dbItems.observe(viewLifecycleOwner) { savedItems ->

            var suffix = ""
            savedItems.forEach { priceIndexResponse ->
                suffix += "${priceIndexResponse.bpi["USD"]?.rate}\n"
            }

            binding.tvUSD.text = "There are ${savedItems.size} items in the database:\n" + suffix
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}