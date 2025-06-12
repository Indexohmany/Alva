package com.example.alva.ui.dialogs

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.example.alva.databinding.DialogCalorieGoalBinding

class CalorieGoalDialog(
    private val currentGoal: Int,
    private val recommendedGoal: Int = 2000,
    private val onGoalUpdated: (Int) -> Unit
) : DialogFragment() {

    private var _binding: DialogCalorieGoalBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogCalorieGoalBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupDialog()
        setupUI()
    }

    private fun setupDialog() {
        dialog?.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        dialog?.setCancelable(true)
    }

    private fun setupUI() {
        // Set current goal
        binding.editTextCalorieGoal.setText(currentGoal.toString())
        binding.sliderCalorieGoal.value = currentGoal.toFloat()

        // Show recommended goal information
        binding.textViewRecommendedGoal.text = "Recommended: $recommendedGoal cal/day"

        // Show comparison
        updateGoalComparison(currentGoal)

        // Setup slider
        binding.sliderCalorieGoal.valueFrom = 1000f
        binding.sliderCalorieGoal.valueTo = 5000f
        binding.sliderCalorieGoal.stepSize = 50f

        binding.sliderCalorieGoal.addOnChangeListener { _, value, fromUser ->
            if (fromUser) {
                val goalValue = value.toInt()
                binding.editTextCalorieGoal.setText(goalValue.toString())
                updateGoalComparison(goalValue)
            }
        }

        // Setup text field
        binding.editTextCalorieGoal.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                val text = s.toString()
                if (text.isNotEmpty()) {
                    val goal = text.toIntOrNull()
                    if (goal != null && goal >= 1000 && goal <= 5000) {
                        binding.sliderCalorieGoal.value = goal.toFloat()
                        updateGoalComparison(goal)
                        binding.textInputLayoutCalorieGoal.error = null
                    } else {
                        binding.textInputLayoutCalorieGoal.error = "Goal must be between 1000-5000 calories"
                    }
                }
            }
        })

        // Quick select buttons
        binding.buttonUseRecommended.setOnClickListener {
            setGoal(recommendedGoal)
        }

        binding.buttonMaintenance.setOnClickListener {
            setGoal(recommendedGoal)
        }

        binding.buttonWeightLoss.setOnClickListener {
            setGoal(recommendedGoal - 500) // 500 calorie deficit for 0.5 kg/week loss
        }

        binding.buttonWeightGain.setOnClickListener {
            setGoal(recommendedGoal + 500) // 500 calorie surplus for 0.5 kg/week gain
        }

        // Setup goal explanations
        setupGoalExplanations()

        // Setup buttons
        binding.buttonCancel.setOnClickListener {
            dismiss()
        }

        binding.buttonSave.setOnClickListener {
            saveGoal()
        }
    }

    private fun setupGoalExplanations() {
        binding.textViewWeightLossDesc.text = "Weight Loss: ${recommendedGoal - 500} cal/day (0.5 kg/week loss)"
        binding.textViewMaintenanceDesc.text = "Maintenance: $recommendedGoal cal/day (maintain current weight)"
        binding.textViewWeightGainDesc.text = "Weight Gain: ${recommendedGoal + 500} cal/day (0.5 kg/week gain)"
    }

    private fun setGoal(goal: Int) {
        val clampedGoal = goal.coerceIn(1000, 5000)
        binding.editTextCalorieGoal.setText(clampedGoal.toString())
        binding.sliderCalorieGoal.value = clampedGoal.toFloat()
        updateGoalComparison(clampedGoal)
    }

    private fun updateGoalComparison(goal: Int) {
        val difference = goal - recommendedGoal
        val comparisonText = when {
            difference == 0 -> "Matches recommended goal"
            difference > 0 -> "+$difference above recommended (weight gain)"
            else -> "${difference} below recommended (weight loss)"
        }

        binding.textViewGoalComparison.text = comparisonText

        // Set color based on goal type
        val color = when {
            difference == 0 -> android.R.color.holo_green_dark
            Math.abs(difference) <= 200 -> android.R.color.holo_orange_dark
            difference > 200 -> android.R.color.holo_blue_dark
            else -> android.R.color.holo_red_dark
        }

        binding.textViewGoalComparison.setTextColor(resources.getColor(color, null))

        // Show weekly weight change estimate
        val weeklyChange = (difference * 7) / 7700.0 // 7700 calories = 1 kg
        val weeklyChangeText = when {
            Math.abs(weeklyChange) < 0.05 -> "Maintain current weight"
            weeklyChange > 0 -> String.format("~%.1f kg/week gain", weeklyChange)
            else -> String.format("~%.1f kg/week loss", Math.abs(weeklyChange))
        }

        binding.textViewWeeklyChange.text = weeklyChangeText
    }

    private fun saveGoal() {
        val goalText = binding.editTextCalorieGoal.text.toString()
        val goal = goalText.toIntOrNull()

        if (goal == null) {
            binding.textInputLayoutCalorieGoal.error = "Please enter a valid number"
            return
        }

        if (goal < 1000 || goal > 5000) {
            binding.textInputLayoutCalorieGoal.error = "Goal must be between 1000-5000 calories"
            return
        }

        onGoalUpdated(goal)
        dismiss()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val TAG = "CalorieGoalDialog"
    }
}