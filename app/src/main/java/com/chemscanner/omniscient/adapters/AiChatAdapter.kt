package com.chemscanner.omniscient.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.chemscanner.omniscient.marrow.data.models.ChatMessage // FIXED IMPORT
import com.chemscanner.omniscient.databinding.ItemChatMessageBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AiChatAdapter(
    private val messages: List<ChatMessage>
) : RecyclerView.Adapter<AiChatAdapter.ChatViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatViewHolder {
        val binding = ItemChatMessageBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ChatViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ChatViewHolder, position: Int) {
        holder.bind(messages[position])
    }

    override fun getItemCount(): Int = messages.size

    inner class ChatViewHolder(
        private val binding: ItemChatMessageBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(message: ChatMessage) {
            if (message.role == "user") { // FIXED: role field check
                // User message
                binding.userMessageLayout.visibility = android.view.View.VISIBLE
                binding.aiMessageLayout.visibility = android.view.View.GONE
                binding.userMessageText.text = message.content
                binding.userTimestamp.text = formatDate(Date(message.timestamp))
            } else {
                // AI message
                binding.userMessageLayout.visibility = android.view.View.GONE
                binding.aiMessageLayout.visibility = android.view.View.VISIBLE
                binding.aiMessageText.text = message.content
                binding.aiTimestamp.text = formatDate(Date(message.timestamp))
                binding.aiIcon.setImageResource(android.R.drawable.ic_menu_info_details)
            }
        }
    }

    private fun formatDate(date: Date): String {
        val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
        return sdf.format(date)
    }
}
