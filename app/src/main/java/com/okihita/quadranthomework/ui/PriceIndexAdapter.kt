package com.okihita.quadranthomework.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.okihita.quadranthomework.data.entities.PriceIndex
import com.okihita.quadranthomework.data.entities.getUTCZonedDateTime
import com.okihita.quadranthomework.databinding.ItemPriceIndexInfoBinding
import com.okihita.quadranthomework.utils.fromUtcToDevice
import com.okihita.quadranthomework.utils.toDateString

class PriceIndexAdapter :
    ListAdapter<PriceIndex, PriceIndexAdapter.PriceIndexVH>(PriceIndexDiffCallback()) {

    private var selectedCurrency = "" // That will show in the RecyclerView

    fun selectCurrency(selectedCurrency: String) {
        this.selectedCurrency = selectedCurrency
        notifyItemRangeChanged(0, itemCount)
    }

    inner class PriceIndexVH(private val binding: ItemPriceIndexInfoBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(priceIndex: PriceIndex) {

            val priceIndexUtcTime = priceIndex.getUTCZonedDateTime().toDateString("HH:mm")
            val priceIndexDeviceTime = priceIndex.getUTCZonedDateTime().fromUtcToDevice()
                .toDateString("dd MMM, HH:mm")

            binding.apply {
                tvTime.text = "TIME: $priceIndexUtcTime ($priceIndexDeviceTime local time)"
                tvCurrencyAndRate.text =
                    "RATE: $selectedCurrency ${priceIndex.bpi[selectedCurrency]?.rate}"
                tvLatLong.text =
                    "LOCATION: ${priceIndex.location?.latitude}, ${priceIndex.location?.longitude}"
                tvAddress.text = "ADDRESS: ${priceIndex.location?.address}"
            }
        }
    }

    // Create new views
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PriceIndexVH {
        val inflatedView = ItemPriceIndexInfoBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return PriceIndexVH(inflatedView)
    }

    // Replace the contents of a view
    override fun onBindViewHolder(holder: PriceIndexVH, position: Int) {
        val priceIndexItem = getItem(position)
        holder.bind(priceIndexItem)
    }

    private class PriceIndexDiffCallback : DiffUtil.ItemCallback<PriceIndex>() {
        override fun areItemsTheSame(oldItem: PriceIndex, newItem: PriceIndex): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: PriceIndex, newItem: PriceIndex): Boolean {
            return oldItem == newItem
        }
    }
}