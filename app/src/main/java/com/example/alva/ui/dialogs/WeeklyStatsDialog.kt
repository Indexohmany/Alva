package com.example.alva.ui.dialogs

import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import com.example.alva.databinding.DialogWeeklyStatsBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.example.alva.data.models.WeeklyStats

class WeeklyStatsDialog(
    private val stats: WeeklyStats?
) : DialogFragment() {

    private var _binding: DialogWeeklyStatsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        _binding = DialogWeeklyStatsBinding.inflate(layoutInflater)
        
        stats?.let {
            binding.textViewAverageCalories.text = "${it.averageCalories} cal/day"
            binding.textViewDaysGoalMet.text = "${it.daysGoalMet}/7 days"
            binding.textViewTotalCalories.text = "${it.totalCalories} cal"
            binding.textViewBestDay.text = "${it.bestDay}: ${it.bestDayCalories} cal"
            binding.textViewWorstDay.text = "${it.worstDay}: ${it.worstDayCalories} cal"
        }

        return MaterialAlertDialogBuilder(requireContext())
            .setTitle("Weekly Statistics")
            .setView(binding.root)
            .setPositiveButton("Close", null)
            .create()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
} 