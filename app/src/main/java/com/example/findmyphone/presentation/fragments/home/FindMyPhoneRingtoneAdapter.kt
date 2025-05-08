package com.example.findmyphone.presentation.fragments.home

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.findmyphone.databinding.ItemRingtoneBinding

class FindMyPhoneRingtoneAdapter(
    private val callbackSelection: (RingtoneModels) -> Unit
) : ListAdapter<RingtoneModels, FindMyPhoneRingtoneAdapter.RingtoneViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RingtoneViewHolder {
        val binding = ItemRingtoneBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return RingtoneViewHolder(binding)
    }

    override fun onBindViewHolder(holder: RingtoneViewHolder, position: Int) {
        val item = getItem(position)
        holder.bind(item)
    }

    inner class RingtoneViewHolder(private val binding: ItemRingtoneBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: RingtoneModels) {
            binding.ringtone = item
            binding.executePendingBindings()
            binding.root.setOnClickListener {
                callbackSelection.invoke(item)
            }
        }

    }

    class DiffCallback : DiffUtil.ItemCallback<RingtoneModels>() {
        override fun areItemsTheSame(oldItem: RingtoneModels, newItem: RingtoneModels): Boolean {
            return oldItem.ringtoneTitle == newItem.ringtoneTitle
        }

        override fun areContentsTheSame(oldItem: RingtoneModels, newItem: RingtoneModels): Boolean {
            return oldItem == newItem
        }
    }
}
