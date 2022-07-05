package com.okihita.quadranthomework.ui

import android.graphics.Color
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.okihita.quadranthomework.R
import com.okihita.quadranthomework.databinding.FragmentHomeBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlin.random.Random

@AndroidEntryPoint
class HomeFragment : Fragment(R.layout.fragment_home) {

    private val coinDeskVM by viewModels<CoinDeskViewModel>()

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentHomeBinding.bind(view)

        setupChart()
        coinDeskVM.latestPriceIndex.observe(viewLifecycleOwner) { priceIndex ->
            binding.tvHello.text = "Latest price for USD:\n" + priceIndex.bpi["USD"]?.rate
        }

        val entries: MutableList<Entry> = mutableListOf()
        (0..24).forEach {
            entries.add(Entry(it.toFloat(), Random.nextFloat() * 40000f))
        }

        val dataset = LineDataSet(entries, "Price in USD")
        binding.chart.data = LineData(dataset)


        coinDeskVM.dbItems.observe(viewLifecycleOwner) { savedItems ->

            var allItems = ""
            savedItems.forEach { priceIndexResponse ->
                allItems += "${priceIndexResponse.bpi["USD"]?.rate}, "
            }
            binding.tvUSD.text = allItems
        }
    }

    private fun setupChart() {
        binding.chart.apply {
            setBackgroundColor(Color.WHITE)
            description.isEnabled = false
            setTouchEnabled(false)

            // Y-Axis
            axisRight.apply {
                isEnabled = false
            }

            axisLeft.apply {
                mAxisMaximum = 40000f
                axisMinimum = 0f
                setDrawGridLines(false)
            }

            // X-Axis
            xAxis.apply {
                setDrawGridLines(false)
                position = XAxis.XAxisPosition.BOTTOM
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}