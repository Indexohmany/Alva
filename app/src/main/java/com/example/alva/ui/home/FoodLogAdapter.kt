package com.example.alva.ui.home

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.alva.databinding.ItemFoodEntryBinding
import com.example.alva.data.models.FoodEntry
import java.text.SimpleDateFormat
import java.util.*

class FoodLogAdapter(
    private val onDeleteClick: (FoodEntry) -> Unit
) : RecyclerView.Adapter<FoodLogAdapter.FoodEntryViewHolder>() {

    private var foodEntries = listOf<FoodEntry>()
    private val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())

    fun updateFoodEntries(entries: List<FoodEntry>) {
        foodEntries = entries
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FoodEntryViewHolder {
        val binding = ItemFoodEntryBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return FoodEntryViewHolder(binding)
    }

    override fun onBindViewHolder(holder: FoodEntryViewHolder, position: Int) {
        holder.bind(foodEntries[position])
    }

    override fun getItemCount() = foodEntries.size

    inner class FoodEntryViewHolder(
        private val binding: ItemFoodEntryBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(foodEntry: FoodEntry) {
            binding.apply {
                textFoodName.text = foodEntry.name
                textCalories.text = "${foodEntry.calories} cal"
                textTime.text = timeFormat.format(foodEntry.timestamp)
                textSource.text = foodEntry.source

                buttonDelete.setOnClickListener {
                    onDeleteClick(foodEntry)
                }
            }
        }
    }
}