package com.chemscanner.omniscient.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.chemscanner.omniscient.R
import com.chemscanner.omniscient.marrow.data.models.ScanHistory // FIXED IMPORT
import com.chemscanner.omniscient.databinding.ItemHistoryBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class HistoryAdapter(
    private val onItemClick: (ScanHistory) -> Unit
) : ListAdapter<ScanHistory, HistoryAdapter.HistoryViewHolder>(ScanHistoryDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryViewHolder {
        val binding = ItemHistoryBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return HistoryViewHolder(binding)
    }

    override fun onBindViewHolder(holder: HistoryViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class HistoryViewHolder(
        private val binding: ItemHistoryBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: ScanHistory) {
            binding.chemicalName.text = item.chemicalName
            binding.scanType.text = item.scanType.replace('_', ' ').lowercase(Locale.getDefault()) // FIXED: item.scanType is String
            binding.dateTime.text = formatDate(item.scanDate)
            binding.confidenceText.text = String.format(Locale.getDefault(), "%.0f%%", item.confidenceScore * 100)

            // Click listener
            binding.root.setOnClickListener {
                onItemClick(item)
            }
        }
    }

    private fun formatDate(timestamp: Long): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
        return sdf.format(Date(timestamp))
    }
}

class ScanHistoryDiffCallback : DiffUtil.ItemCallback<ScanHistory>() {
    override fun areItemsTheSame(oldItem: ScanHistory, newItem: ScanHistory): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: ScanHistory, newItem: ScanHistory): Boolean {
        return oldItem == newItem
    }
}
