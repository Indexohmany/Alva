package com.example.alva.ui.dialogs

import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import com.example.alva.databinding.DialogNotificationSettingsBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class NotificationSettingsDialog : DialogFragment() {

    private var _binding: DialogNotificationSettingsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        _binding = DialogNotificationSettingsBinding.inflate(layoutInflater)
        
        return MaterialAlertDialogBuilder(requireContext())
            .setTitle("Notification Settings")
            .setView(binding.root)
            .setPositiveButton("Save") { _, _ ->
                saveSettings()
            }
            .setNegativeButton("Cancel", null)
            .create()
    }

    private fun saveSettings() {
        val settings = NotificationSettings(
            dailyReminder = binding.switchDailyReminder.isChecked,
            weeklyReport = binding.switchWeeklyReport.isChecked,
            goalAchievement = binding.switchGoalAchievement.isChecked,
            mealReminders = binding.switchMealReminders.isChecked
        )
        // TODO: Save settings to ViewModel
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    data class NotificationSettings(
        val dailyReminder: Boolean,
        val weeklyReport: Boolean,
        val goalAchievement: Boolean,
        val mealReminders: Boolean
    )
} 