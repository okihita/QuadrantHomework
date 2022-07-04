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

    private val coinDeskViewModel by viewModels<CoinDeskViewModel>()

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentHomeBinding.bind(view)

        coinDeskViewModel.priceIndexResponse.observe(viewLifecycleOwner) {
            binding.tv.text = it.disclaimer
        }

        coinDeskViewModel.callCoinDeskApi()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}