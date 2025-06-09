package com.example.alva.ui.dialogs

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.fragment.app.DialogFragment
import com.example.alva.databinding.DialogCalorieGoalBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class CalorieGoalDialog(
    private val currentGoal: Int,
    private val onGoalSet: (Int) -> Unit
) : DialogFragment() {

    private var _binding: DialogCalorieGoalBinding? = null
    private val binding get() = _binding!!

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        _binding = DialogCalorieGoalBinding.inflate(layoutInflater)
        
        return MaterialAlertDialogBuilder(requireContext())
            .setTitle("Set Daily Calorie Goal")
            .setView(binding.root)
            .setPositiveButton("Set") { _, _ ->
                val newGoal = binding.editTextCalorieGoal.text.toString().toIntOrNull() ?: currentGoal
                onGoalSet(newGoal)
            }
            .setNegativeButton("Cancel", null)
            .create()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // Set current goal in the input field
        binding.editTextCalorieGoal.setText(currentGoal.toString())
        
        // Show keyboard and focus on input field
        binding.editTextCalorieGoal.requestFocus()
        dialog?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val TAG = "CalorieGoalDialog"
    }
} 