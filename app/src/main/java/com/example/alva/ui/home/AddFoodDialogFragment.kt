package com.example.alva.ui.home

import android.app.Dialog
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.example.alva.databinding.DialogAddFoodBinding

class AddFoodDialogFragment(
    private val onFoodAdded: (String, Int) -> Unit
) : DialogFragment() {

    private var _binding: DialogAddFoodBinding? = null
    private val binding get() = _binding!!

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        _binding = DialogAddFoodBinding.inflate(layoutInflater)

        return AlertDialog.Builder(requireContext())
            .setTitle("Add Food Manually")
            .setView(binding.root)
            .setPositiveButton("Add") { _, _ ->
                val foodName = binding.editTextFoodName.text.toString().trim()
                val caloriesText = binding.editTextCalories.text.toString().trim()

                if (foodName.isNotEmpty() && caloriesText.isNotEmpty()) {
                    val calories = caloriesText.toIntOrNull()
                    if (calories != null && calories > 0) {
                        onFoodAdded(foodName, calories)
                    } else {
                        Toast.makeText(context, "Please enter valid calories", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(context, "Please fill all fields", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .create()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
