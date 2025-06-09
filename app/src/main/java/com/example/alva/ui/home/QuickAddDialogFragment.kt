package com.example.alva.ui.home

import android.app.Dialog
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.alva.databinding.DialogQuickAddBinding
import com.example.alva.data.models.FoodEntry
import java.util.*

class QuickAddDialogFragment(
    private val onFoodSelected: (FoodEntry) -> Unit
) : DialogFragment() {

    private var _binding: DialogQuickAddBinding? = null
    private val binding get() = _binding!!

    private val commonFoods = listOf(
        FoodEntry(1, "Apple", 95, Date()),
        FoodEntry(2, "Banana", 105, Date()),
        FoodEntry(3, "Slice of Bread", 80, Date()),
        FoodEntry(4, "Cup of Coffee", 5, Date()),
        FoodEntry(5, "Glass of Water", 0, Date()),
        FoodEntry(6, "Egg", 70, Date()),
        FoodEntry(7, "Cup of Milk", 150, Date()),
        FoodEntry(8, "Orange", 85, Date())
    )

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        _binding = DialogQuickAddBinding.inflate(layoutInflater)

        setupRecyclerView()

        return AlertDialog.Builder(requireContext())
            .setTitle("Quick Add Common Foods")
            .setView(binding.root)
            .setNegativeButton("Cancel", null)
            .create()
    }

    private fun setupRecyclerView() {
        val adapter = QuickAddAdapter { foodEntry ->
            onFoodSelected(foodEntry)
            dismiss()
        }

        binding.recyclerViewQuickAdd.apply {
            this.adapter = adapter
            layoutManager = LinearLayoutManager(context)
        }

        adapter.updateFoods(commonFoods)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}