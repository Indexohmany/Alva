package com.example.alva.ui.camera

import android.app.Dialog
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.example.alva.databinding.DialogFoodRecognitionBinding

class FoodRecognitionResultDialog(
    private val foodName: String,
    private val estimatedCalories: Int,
    private val onConfirm: (String, Int) -> Unit,
    private val onCancel: () -> Unit
) : DialogFragment() {

    private var _binding: DialogFoodRecognitionBinding? = null
    private val binding get() = _binding!!

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        _binding = DialogFoodRecognitionBinding.inflate(layoutInflater)

        binding.apply {
            editTextFoodName.setText(foodName)
            editTextCalories.setText(estimatedCalories.toString())
            textRecognitionConfidence.text = "Recognition confidence: High"
        }

        return AlertDialog.Builder(requireContext())
            .setTitle("Food Recognized!")
            .setView(binding.root)
            .setPositiveButton("Add to Log") { _, _ ->
                val finalName = binding.editTextFoodName.text.toString().trim()
                val finalCalories = binding.editTextCalories.text.toString().toIntOrNull() ?: estimatedCalories
                onConfirm(finalName, finalCalories)
            }
            .setNegativeButton("Cancel") { _, _ ->
                onCancel()
            }
            .create()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}