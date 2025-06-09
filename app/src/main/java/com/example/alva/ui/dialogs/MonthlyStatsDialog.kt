package com.example.alva.ui.dialogs

import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import com.example.alva.databinding.DialogMonthlyStatsBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.example.alva.data.models.MonthlyStats

class MonthlyStatsDialog(
    private val stats: MonthlyStats?
) : DialogFragment() {

    private var _binding: DialogMonthlyStatsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        _binding = DialogMonthlyStatsBinding.inflate(layoutInflater)
        
        stats?.let {
            binding.textViewAverageCalories.text = "${it.averageCalories} cal/day"
            binding.textViewDaysGoalMet.text = "${it.daysGoalMet}/${it.totalDays} days"
            binding.textViewTotalCalories.text = "${it.totalCalories} cal"
            binding.textViewBestWeek.text = "Week ${it.bestWeek}: ${it.bestWeekCalories} cal"
            binding.textViewWorstWeek.text = "Week ${it.worstWeek}: ${it.worstWeekCalories} cal"
        }

        return MaterialAlertDialogBuilder(requireContext())
            .setTitle("Monthly Statistics")
            .setView(binding.root)
            .setPositiveButton("Close", null)
            .create()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
} 