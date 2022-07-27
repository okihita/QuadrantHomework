package com.okihita.quadranthomework.ui

import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.ListAdapter
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView.AdapterDataObserver
import coil.load
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.okihita.quadranthomework.R
import com.okihita.quadranthomework.data.entities.PriceIndex
import com.okihita.quadranthomework.data.entities.getUTCZonedDateTime
import com.okihita.quadranthomework.databinding.FragmentHomeBinding
import com.okihita.quadranthomework.utils.fromDeviceToUtc
import com.okihita.quadranthomework.utils.getSystemZonedDateTime
import com.okihita.quadranthomework.utils.toDateString
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest

@AndroidEntryPoint
class HomeFragment : Fragment(R.layout.fragment_home) {

    private val coinDeskVM by viewModels<MainViewModel>()

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private var isWorkerRunning = false // To avoid unnecessary initial call of reloadCache()

    private val chartEntries: MutableList<Entry> = mutableListOf()
    private lateinit var priceIndexAdapter: PriceIndexAdapter

    private var chosenCurrency = "USD"

    private val requiredPermissionsList = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        arrayOf(
            android.Manifest.permission.ACCESS_COARSE_LOCATION,
            android.Manifest.permission.ACCESS_FINE_LOCATION,
            android.Manifest.permission.ACCESS_BACKGROUND_LOCATION,
        )
    } else {
        arrayOf(
            android.Manifest.permission.ACCESS_COARSE_LOCATION,
            android.Manifest.permission.ACCESS_FINE_LOCATION
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentHomeBinding.bind(view)

        setupObservers()

        binding.tvDate.text = String.format(
            getString(R.string.homeFragment_tv_dateLabel),
            getSystemZonedDateTime().fromDeviceToUtc().toDateString()
        )
        setupChart()
        setupButtons()
        setupRV()

        // Check for permissions
        when {
            // If all permissions are granted, then start the background work
            requiredPermissionsList.all {
                ContextCompat.checkSelfPermission(requireContext(), it) ==
                        PackageManager.PERMISSION_GRANTED
            } -> coinDeskVM.startPriceLocationUpdateWork()
            else -> requestPermissionLauncher.launch(requiredPermissionsList)
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
            // Doing nothing because both chart and list are updated instantly when db updates
        }
    }

    private fun switchCurrency(currency: String) {

        chosenCurrency = currency

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

        priceIndexAdapter.selectCurrency(currency)
    }

    private fun setupRV() {
        priceIndexAdapter = PriceIndexAdapter()
        binding.rvRates.adapter = priceIndexAdapter

        // Scroll to top whenever a new data (from the latest hour) is inserted
        priceIndexAdapter.registerAdapterDataObserver(object : AdapterDataObserver() {
            override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
                binding.rvRates.scrollToPosition(positionStart)
            }
        })

        binding.rvRates.itemAnimator = null // Disable blinking animation when content's changed
    }

    private fun setupObservers() {

        viewLifecycleOwner.lifecycleScope.launchWhenResumed {
            coinDeskVM.priceIndicesFlow.collectLatest { priceIndices ->
                println("Price indices retrieved. Size: ${priceIndices.size}")
                redrawChart(priceIndices)
                priceIndexAdapter.submitList(priceIndices.asReversed())
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

    private fun redrawChart(priceIndices: List<PriceIndex>) {

        if (priceIndices.isEmpty()) return

        chartEntries.clear()

        priceIndices.forEach { priceIndex ->
            val hour = priceIndex.getUTCZonedDateTime().hour
            val rate = priceIndex.bpi[chosenCurrency]?.rate_float
            chartEntries.add(Entry(hour.toFloat(), rate ?: 0f))
        }

        binding.chart.apply {
            axisLeft.apply {
                axisMinimum = priceIndices.minOf { it.bpi[chosenCurrency]?.rate_float ?: 0f } - 200
                axisMaximum = priceIndices.maxOf { it.bpi[chosenCurrency]?.rate_float ?: 0f } + 200
            }

            notifyDataSetChanged()
            invalidate()
        }
    }

    // Set styles and generate initial data
    private fun setupChart() {
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