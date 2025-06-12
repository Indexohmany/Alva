package com.example.alva.ui.dialogs

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.example.alva.databinding.DialogActivityLevelBinding

class ActivityLevelDialog(
    private val currentLevel: String,
    private val onActivityLevelSelected: (String) -> Unit
) : DialogFragment() {

    private var _binding: DialogActivityLevelBinding? = null
    private val binding get() = _binding!!

    data class ActivityLevel(
        val name: String,
        val description: String,
        val examples: String
    )

    private val activityLevels = listOf(
        ActivityLevel(
            "Sedentary",
            "Little or no exercise",
            "Desk job, little/no exercise"
        ),
        ActivityLevel(
            "Light",
            "Light exercise 1-3 days/week",
            "Light exercise/sports 1-3 days/week"
        ),
        ActivityLevel(
            "Moderate",
            "Moderate exercise 3-5 days/week",
            "Moderate exercise/sports 3-5 days/week"
        ),
        ActivityLevel(
            "Active",
            "Hard exercise 6-7 days/week",
            "Hard exercise/sports 6-7 days/week"
        ),
        ActivityLevel(
            "Very Active",
            "Very hard exercise & physical job",
            "Very hard exercise/sports & physical job"
        )
    )

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogActivityLevelBinding.inflate(inflater, container, false)
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
        // Set title
        binding.textViewTitle.text = "Select Activity Level"
        binding.textViewSubtitle.text = "Choose the level that best describes your daily activity"

        // Setup radio buttons and descriptions
        setupRadioButtons()

        // Setup buttons
        binding.buttonCancel.setOnClickListener {
            dismiss()
        }

        binding.buttonConfirm.setOnClickListener {
            saveActivityLevel()
        }
    }

    private fun setupRadioButtons() {
        // Set current selection
        when (currentLevel) {
            "Sedentary" -> binding.radioSedentary.isChecked = true
            "Light" -> binding.radioLight.isChecked = true
            "Moderate" -> binding.radioModerate.isChecked = true
            "Active" -> binding.radioActive.isChecked = true
            "Very Active" -> binding.radioVeryActive.isChecked = true
        }

        // Set descriptions
        binding.textSedentaryDesc.text = activityLevels[0].description
        binding.textSedentaryExamples.text = activityLevels[0].examples

        binding.textLightDesc.text = activityLevels[1].description
        binding.textLightExamples.text = activityLevels[1].examples

        binding.textModerateDesc.text = activityLevels[2].description
        binding.textModerateExamples.text = activityLevels[2].examples

        binding.textActiveDesc.text = activityLevels[3].description
        binding.textActiveExamples.text = activityLevels[3].examples

        binding.textVeryActiveDesc.text = activityLevels[4].description
        binding.textVeryActiveExamples.text = activityLevels[4].examples

        // Add click listeners for the entire layout areas
        binding.layoutSedentary.setOnClickListener {
            binding.radioSedentary.isChecked = true
        }

        binding.layoutLight.setOnClickListener {
            binding.radioLight.isChecked = true
        }

        binding.layoutModerate.setOnClickListener {
            binding.radioModerate.isChecked = true
        }

        binding.layoutActive.setOnClickListener {
            binding.radioActive.isChecked = true
        }

        binding.layoutVeryActive.setOnClickListener {
            binding.radioVeryActive.isChecked = true
        }

        // Add radio button change listeners to update descriptions visibility
        setupRadioGroupListener()
    }

    private fun setupRadioGroupListener() {
        binding.radioGroup.setOnCheckedChangeListener { _, checkedId ->
            // You can add logic here to highlight the selected option
            // or show additional information based on selection
        }
    }

    private fun saveActivityLevel() {
        val selectedLevel = when (binding.radioGroup.checkedRadioButtonId) {
            binding.radioSedentary.id -> "Sedentary"
            binding.radioLight.id -> "Light"
            binding.radioModerate.id -> "Moderate"
            binding.radioActive.id -> "Active"
            binding.radioVeryActive.id -> "Very Active"
            else -> currentLevel // fallback to current level
        }

        onActivityLevelSelected(selectedLevel)
        dismiss()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}