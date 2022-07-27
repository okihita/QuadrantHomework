package com.okihita.quadranthomework.ui

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.okihita.quadranthomework.R
import com.okihita.quadranthomework.data.entities.PriceIndex
import com.okihita.quadranthomework.data.entities.getUTCZonedDateTime
import com.okihita.quadranthomework.databinding.ItemPriceIndexInfoBinding
import com.okihita.quadranthomework.utils.fromUtcToDevice
import com.okihita.quadranthomework.utils.toDateString

class PriceIndexAdapter : ListAdapter<PriceIndex, PriceIndexAdapter.PriceIndexVH>(PriceIndexDC()) {

    private var selectedCurrency = ""

    fun selectCurrency(selectedCurrency: String) {
        this.selectedCurrency = selectedCurrency
        notifyItemRangeChanged(0, itemCount)
    }

    inner class PriceIndexVH(private val binding: ItemPriceIndexInfoBinding) :
        RecyclerView.ViewHolder(binding.root) {

        val context: Context = binding.root.context

        fun bind(priceIndex: PriceIndex) {

            binding.apply {
                tvTime.text = String.format(
                    context.getString(
                        R.string.itemPriceIndex_time,
                        priceIndex.getUTCZonedDateTime().toDateString("HH:mm"),
                        priceIndex.getUTCZonedDateTime().fromUtcToDevice()
                            .toDateString("dd MMM, HH:mm")
                    )
                )

                tvCurrencyAndRate.text = String.format(
                    context.getString(
                        R.string.itemPriceIndex_rate,
                        selectedCurrency, priceIndex.bpi[selectedCurrency]?.rate
                    )
                )

                tvLatLong.text = String.format(
                    context.getString(
                        R.string.itemPriceIndex_location,
                        priceIndex.location?.latitude,
                        priceIndex.location?.longitude
                    )
                )

                tvAddress.text = String.format(
                    context.getString(R.string.itemPriceIndex_address),
                    priceIndex.location?.address
                )
            }
        }
    }

    // Create new views
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PriceIndexVH {
        val inflatedView =
            ItemPriceIndexInfoBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PriceIndexVH(inflatedView)
    }

    // Replace the contents of a view
    override fun onBindViewHolder(holder: PriceIndexVH, position: Int) {
        val priceIndexItem = getItem(position)
        holder.bind(priceIndexItem)
    }

    private class PriceIndexDC : DiffUtil.ItemCallback<PriceIndex>() {
        override fun areItemsTheSame(old: PriceIndex, new: PriceIndex) = old.id == new.id
        override fun areContentsTheSame(old: PriceIndex, new: PriceIndex) = old == new
    }
}