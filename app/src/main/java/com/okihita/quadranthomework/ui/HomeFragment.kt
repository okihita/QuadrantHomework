package com.okihita.quadranthomework.ui

import android.graphics.Color
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.work.WorkInfo
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.okihita.quadranthomework.R
import com.okihita.quadranthomework.data.entities.PriceIndex
import com.okihita.quadranthomework.data.entities.getDateTime
import com.okihita.quadranthomework.databinding.FragmentHomeBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class HomeFragment : Fragment(R.layout.fragment_home) {

    private val coinDeskVM by viewModels<CoinDeskViewModel>()

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private var isWorkerRunning = false // To avoid unnecessary initial call of reloadCache()

    private val chartEntries: MutableList<Entry> = mutableListOf()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentHomeBinding.bind(view)

        setupPriceChart()

        coinDeskVM.cacheItems.observe(viewLifecycleOwner) { cachedItems ->
            if (cachedItems.isNotEmpty()) {
                redrawChart(cachedItems)
                refreshText(cachedItems)
            }
        }

        coinDeskVM.workInfo.observe(viewLifecycleOwner) {

            // For PeriodicWorkRequest, State.ENQUEUED means either that the work is started for the
            // first time, or a work is finished and another future-work is now enqueued.
            if (it.state == WorkInfo.State.ENQUEUED) {
                if (!isWorkerRunning) {
                    isWorkerRunning = true
                } else {
                    coinDeskVM.reloadCache()
                }
            }
        }
    }

    private fun redrawChart(cachedItems: List<PriceIndex>) {
        cachedItems.forEach { priceIndex ->
            val hour = priceIndex.getDateTime().hour
            val rate = priceIndex.bpi["USD"]?.rate_float

            chartEntries.add(Entry(hour.toFloat(), rate ?: 0f))
        }

        binding.chart.apply {

            axisLeft.apply {
                axisMinimum = cachedItems.minOf { it.bpi["USD"]?.rate_float ?: 0f } - 200
                axisMaximum = cachedItems.maxOf { it.bpi["USD"]?.rate_float ?: 0f } + 200
            }

            notifyDataSetChanged()
            invalidate()
        }
    }

    private fun refreshText(cachedItems: List<PriceIndex>) {
        var allItems = ""
        cachedItems.forEach { priceIndex ->
            val hour = priceIndex.getDateTime().hour
            val rate = priceIndex.bpi["USD"]?.rate_float
            allItems += "USD ${rate}, for hour ${hour}\n"
        }

        binding.tvUSD.text = allItems
    }

    // Set styles and generate initial data
    private fun setupPriceChart() {
        binding.chart.apply {

            setBackgroundColor(Color.WHITE)
            description.isEnabled = false

            setTouchEnabled(true)
            isScaleYEnabled = false
            isDragYEnabled = false

            isScaleXEnabled = true
            isDragXEnabled = true

            // Y-Axis
            axisRight.apply {
                isEnabled = false
            }

            axisLeft.apply {
                axisMaximum = 25000f
                axisMinimum = 15000f
                setDrawGridLines(false)
            }

            // X-Axis
            xAxis.apply {

                setDrawGridLines(false)
                position = XAxis.XAxisPosition.BOTTOM

                axisMinimum = 0f
                axisMaximum = 23f
                labelCount = 24
                isGranularityEnabled = true
                granularity = 1f
            }
        }

        val dataset = LineDataSet(chartEntries, "Hourly BTC price in USD")
        binding.chart.data = LineData(dataset)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}