package com.example.alva.ui.home

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.alva.databinding.ItemQuickAddFoodBinding
import com.example.alva.data.models.FoodEntry

class QuickAddAdapter(
    private val onFoodClick: (FoodEntry) -> Unit
) : RecyclerView.Adapter<QuickAddAdapter.QuickAddViewHolder>() {

    private var foods = listOf<FoodEntry>()

    fun updateFoods(newFoods: List<FoodEntry>) {
        foods = newFoods
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): QuickAddViewHolder {
        val binding = ItemQuickAddFoodBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return QuickAddViewHolder(binding)
    }

    override fun onBindViewHolder(holder: QuickAddViewHolder, position: Int) {
        holder.bind(foods[position])
    }

    override fun getItemCount() = foods.size

    inner class QuickAddViewHolder(
        private val binding: ItemQuickAddFoodBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(food: FoodEntry) {
            binding.apply {
                textFoodName.text = food.name
                textCalories.text = "${food.calories} cal"

                root.setOnClickListener {
                    onFoodClick(food)
                }
            }
        }
    }
}