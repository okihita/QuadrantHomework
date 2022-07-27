package com.okihita.quadranthomework.ui

import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.work.WorkInfo
import coil.load
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.okihita.quadranthomework.R
import com.okihita.quadranthomework.data.entities.PriceIndex
import com.okihita.quadranthomework.data.entities.getISOZonedDateTime
import com.okihita.quadranthomework.databinding.FragmentHomeBinding
import com.okihita.quadranthomework.utils.fromDeviceToUtc
import com.okihita.quadranthomework.utils.getCurrentDeviceZonedDateTime
import com.okihita.quadranthomework.utils.toDateString
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class HomeFragment : Fragment(R.layout.fragment_home) {

    private val coinDeskVM by viewModels<CoinDeskViewModel>()

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private var isWorkerRunning = false // To avoid unnecessary initial call of reloadCache()

    private val chartEntries: MutableList<Entry> = mutableListOf()
    lateinit var priceIndexAdapter: PriceIndexAdapter

    private var selectedCurrency = "USD"

    private val requiredPermissionsList = arrayOf(
        android.Manifest.permission.ACCESS_COARSE_LOCATION,
        android.Manifest.permission.ACCESS_FINE_LOCATION,
        android.Manifest.permission.ACCESS_BACKGROUND_LOCATION,
    )

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentHomeBinding.bind(view)

        setupObservers()

        binding.tvDate.text =
            getCurrentDeviceZonedDateTime().fromDeviceToUtc().toDateString() + " (UTC)"
        setupPriceChart()
        setupButtons()
        setupRV()

        // Check for permissions
        when {
            // If all permissions are granted, then call start the work
            requiredPermissionsList.all {
                ContextCompat.checkSelfPermission(requireContext(), it) ==
                        PackageManager.PERMISSION_GRANTED
            } -> {
                coinDeskVM.startPriceLocationUpdateWork()
            }

            else -> {
                requestPermissionLauncher.launch(requiredPermissionsList)
            }
        }

        switchCurrency("USD")
    }

    private fun setupButtons() {

        binding.apply {
            ivUSD.load("https://countryflagsapi.com/png/us")
            ivGBP.load("https://countryflagsapi.com/png/gb")
            ivEUR.load("https://countryflagsapi.com/png/eu")
        }

        binding.ivUSD.setOnClickListener { switchCurrency("USD") }
        binding.ivGBP.setOnClickListener { switchCurrency("GBP") }
        binding.ivEUR.setOnClickListener { switchCurrency("EUR") }

        binding.btRefresh.setOnClickListener {
            coinDeskVM.reloadTodayItemsFromDatabase()
        }
    }

    private fun switchCurrency(currency: String) {

        selectedCurrency = currency

        val colorMatrix = ColorMatrix()
        colorMatrix.setSaturation(0f)
        val grayscaleFilter = ColorMatrixColorFilter(colorMatrix)

        binding.apply {
            ivUSD.colorFilter = grayscaleFilter
            ivGBP.colorFilter = grayscaleFilter
            ivEUR.colorFilter = grayscaleFilter

            when (currency) {
                "USD" -> ivUSD.clearColorFilter()
                "GBP" -> ivGBP.clearColorFilter()
                "EUR" -> ivEUR.clearColorFilter()
                else -> {}
            }
        }

        coinDeskVM.refreshCacheItems()
    }

    private fun setupRV() {
        priceIndexAdapter = PriceIndexAdapter()
        binding.rvRates.adapter = priceIndexAdapter
    }

    private fun setupObservers() {
        coinDeskVM.cacheItems.observe(viewLifecycleOwner) { cachedItems ->
            if (cachedItems != null && cachedItems.isNotEmpty()) {
                redrawChart(cachedItems)
                refreshRV(cachedItems)
            }
        }

        coinDeskVM.workInfo.observe(viewLifecycleOwner) {
            // For PeriodicWorkRequest, State.ENQUEUED means either that the work is started for the
            // first time, or a work is finished and another future-work is now enqueued.
            if (it.state == WorkInfo.State.ENQUEUED) {
                if (!isWorkerRunning) {
                    isWorkerRunning = true
                } else {
                    coinDeskVM.reloadTodayItemsFromDatabase()
                }
            }
        }
    }

    // https://developer.android.com/training/permissions/requesting
    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissionStatusMap ->
            if (permissionStatusMap.all { it.value }) { // If all permissions are granted
                coinDeskVM.startPriceLocationUpdateWork()
            } else {
                // TODO: Handle permission denied here
                Toast.makeText(activity, "Permission denied", Toast.LENGTH_SHORT).show()
            }
        }

    private fun redrawChart(cachedItems: List<PriceIndex>) {

        chartEntries.clear()

        cachedItems.forEach { priceIndex ->
            val hour = priceIndex.getISOZonedDateTime().hour
            val rate = priceIndex.bpi[selectedCurrency]?.rate_float

            chartEntries.add(Entry(hour.toFloat(), rate ?: 0f))
        }

        binding.chart.apply {

            axisLeft.apply {
                axisMinimum = cachedItems.minOf { it.bpi[selectedCurrency]?.rate_float ?: 0f } - 200
                axisMaximum = cachedItems.maxOf { it.bpi[selectedCurrency]?.rate_float ?: 0f } + 200
            }

            notifyDataSetChanged()
            invalidate()
        }
    }

    private fun refreshRV(cachedItems: List<PriceIndex>) {
        priceIndexAdapter.submitList(cachedItems.take(5))
        priceIndexAdapter.selectCurrency(selectedCurrency)
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

        val dataset = LineDataSet(chartEntries, "Hourly BTC price")
        binding.chart.data = LineData(dataset)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}