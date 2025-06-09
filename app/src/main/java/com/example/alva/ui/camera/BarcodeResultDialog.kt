package com.example.alva.ui.camera

import android.app.Dialog
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.example.alva.databinding.DialogBarcodeResultBinding

class BarcodeResultDialog(
    private val productName: String,
    private val calories: Int,
    private val onConfirm: (Int) -> Unit,
    private val onCancel: () -> Unit
) : DialogFragment() {

    private var _binding: DialogBarcodeResultBinding? = null
    private val binding get() = _binding!!

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        _binding = DialogBarcodeResultBinding.inflate(layoutInflater)

        binding.apply {
            textProductName.text = productName
            editTextCalories.setText(calories.toString())
        }

        return AlertDialog.Builder(requireContext())
            .setTitle("Product Found!")
            .setView(binding.root)
            .setPositiveButton("Add to Log") { _, _ ->
                val finalCalories = binding.editTextCalories.text.toString().toIntOrNull() ?: calories
                onConfirm(finalCalories)
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