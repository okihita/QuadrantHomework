package com.okihita.quadranthomework.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.okihita.quadranthomework.data.entities.PriceIndex
import com.okihita.quadranthomework.data.entities.getDateTime
import com.okihita.quadranthomework.databinding.ItemPriceIndexInfoBinding

class PriceIndexAdapter : RecyclerView.Adapter<PriceIndexAdapter.PriceIndexVH>() {

    private var priceIndices: MutableList<PriceIndex> = mutableListOf()
    private var selectedCurrency = "" // That will show in the RecyclerView

    fun submitList(newItems: List<PriceIndex>) {
        priceIndices.clear()
        priceIndices.addAll(newItems)
        notifyDataSetChanged()
    }

    fun selectCurrency(selectedCurrency: String) {
        this.selectedCurrency = selectedCurrency
    }

    inner class PriceIndexVH(private val binding: ItemPriceIndexInfoBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(priceIndex: PriceIndex) {
            binding.apply {
                tvTime.text = "${priceIndex.getDateTime().hour}:00 UTC"
                tvCurrency.text = selectedCurrency
                tvRate.text = priceIndex.bpi[selectedCurrency]?.rate
                tvLatLong.text =
                    "${priceIndex.location?.latitude}, ${priceIndex.location?.longitude}"
                tvAddress.text = "${priceIndex.location?.address}"
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
        val priceIndexItem = priceIndices[position]
        holder.bind(priceIndexItem)
    }

    override fun getItemCount() = priceIndices.size
}