package com.example.alva.ui.dialogs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.fragment.app.DialogFragment
import com.example.alva.R
import com.example.alva.data.models.UserProfile
import com.example.alva.databinding.DialogEditProfileBinding

class EditProfileDialog(
    private val currentProfile: UserProfile,
    private val onProfileUpdated: (UserProfile) -> Unit
) : DialogFragment() {

    private var _binding: DialogEditProfileBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogEditProfileBinding.inflate(inflater, container, false)
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
        // Pre-fill current profile data
        binding.editTextName.setText(currentProfile.name)
        binding.editTextEmail.setText(currentProfile.email)
        binding.editTextAge.setText(currentProfile.age.toString())
        binding.editTextHeight.setText(currentProfile.height.toString())
        binding.editTextWeight.setText(currentProfile.weight.toString())

        // Setup gender dropdown - ONLY Male and Female options
        val genderOptions = arrayOf("Male", "Female")
        val genderAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, genderOptions)
        binding.spinnerGender.setAdapter(genderAdapter)

        // Set current gender - handle legacy values
        val currentGender = when {
            currentProfile.gender.equals("Male", true) -> "Male"
            currentProfile.gender.equals("Female", true) -> "Female"
            else -> "Male" // Default to Male if not specified or other value
        }

        // Set the text and make it non-editable but clickable
        binding.spinnerGender.setText(currentGender, false)
        binding.spinnerGender.keyListener = null // Make it non-editable
        binding.spinnerGender.isFocusable = false
        binding.spinnerGender.isCursorVisible = false

        // Force dropdown to show when clicked
        binding.spinnerGender.setOnClickListener {
            binding.spinnerGender.showDropDown()
        }

        // Setup buttons
        binding.buttonCancel.setOnClickListener {
            dismiss()
        }

        binding.buttonSave.setOnClickListener {
            saveProfile()
        }

        // Add input validation
        setupValidation()

        // Setup BMI preview calculation
        setupBMIPreview()
    }

    private fun setupValidation() {
        // Real-time validation for age
        binding.editTextAge.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                validateAge()
            }
        }

        // Real-time validation for height
        binding.editTextHeight.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                validateHeight()
                updateBMIPreview()
            }
        }

        // Real-time validation for weight
        binding.editTextWeight.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                validateWeight()
                updateBMIPreview()
            }
        }

        // Real-time validation for email
        binding.editTextEmail.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                validateEmail()
            }
        }
    }

    private fun setupBMIPreview() {
        // Add text watchers for real-time BMI calculation
        binding.editTextHeight.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: android.text.Editable?) {
                updateBMIPreview()
            }
        })

        binding.editTextWeight.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: android.text.Editable?) {
                updateBMIPreview()
            }
        })

        // Initial BMI calculation
        updateBMIPreview()
    }

    private fun updateBMIPreview() {
        val heightText = binding.editTextHeight.text.toString()
        val weightText = binding.editTextWeight.text.toString()

        val height = heightText.toFloatOrNull()
        val weight = weightText.toFloatOrNull()

        if (height != null && weight != null && height > 0 && weight > 0) {
            val heightInMeters = height / 100
            val bmi = weight / (heightInMeters * heightInMeters)

            val bmiCategory = when {
                bmi < 18.5 -> "Underweight"
                bmi < 25.0 -> "Normal weight"
                bmi < 30.0 -> "Overweight"
                else -> "Obese"
            }

            binding.textViewBmiPreview.text = String.format("BMI: %.1f (%s)", bmi, bmiCategory)
        } else {
            binding.textViewBmiPreview.text = "Enter height and weight to calculate"
        }
    }

    private fun validateAge(): Boolean {
        val ageText = binding.editTextAge.text.toString()
        val age = ageText.toIntOrNull()

        return when {
            ageText.isEmpty() -> {
                binding.textInputLayoutAge.error = "Age is required"
                false
            }
            age == null || age <= 0 -> {
                binding.textInputLayoutAge.error = "Enter a valid age"
                false
            }
            age < 13 -> {
                binding.textInputLayoutAge.error = "Age must be at least 13"
                false
            }
            age > 150 -> {
                binding.textInputLayoutAge.error = "Age must be less than 150"
                false
            }
            else -> {
                binding.textInputLayoutAge.error = null
                true
            }
        }
    }

    private fun validateHeight(): Boolean {
        val heightText = binding.editTextHeight.text.toString()
        val height = heightText.toFloatOrNull()

        return when {
            heightText.isEmpty() -> {
                binding.textInputLayoutHeight.error = "Height is required"
                false
            }
            height == null || height <= 0 -> {
                binding.textInputLayoutHeight.error = "Enter a valid height"
                false
            }
            height < 100 -> {
                binding.textInputLayoutHeight.error = "Height must be at least 100 cm"
                false
            }
            height > 300 -> {
                binding.textInputLayoutHeight.error = "Height must be less than 300 cm"
                false
            }
            else -> {
                binding.textInputLayoutHeight.error = null
                true
            }
        }
    }

    private fun validateWeight(): Boolean {
        val weightText = binding.editTextWeight.text.toString()
        val weight = weightText.toFloatOrNull()

        return when {
            weightText.isEmpty() -> {
                binding.textInputLayoutWeight.error = "Weight is required"
                false
            }
            weight == null || weight <= 0 -> {
                binding.textInputLayoutWeight.error = "Enter a valid weight"
                false
            }
            weight < 20 -> {
                binding.textInputLayoutWeight.error = "Weight must be at least 20 kg"
                false
            }
            weight > 500 -> {
                binding.textInputLayoutWeight.error = "Weight must be less than 500 kg"
                false
            }
            else -> {
                binding.textInputLayoutWeight.error = null
                true
            }
        }
    }

    private fun validateEmail(): Boolean {
        val email = binding.editTextEmail.text.toString().trim()

        return when {
            email.isEmpty() -> {
                binding.textInputLayoutEmail.error = "Email is required"
                false
            }
            !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches() -> {
                binding.textInputLayoutEmail.error = "Enter a valid email address"
                false
            }
            else -> {
                binding.textInputLayoutEmail.error = null
                true
            }
        }
    }

    private fun validateName(): Boolean {
        val name = binding.editTextName.text.toString().trim()

        return when {
            name.isEmpty() -> {
                binding.textInputLayoutName.error = "Name is required"
                false
            }
            name.length < 2 -> {
                binding.textInputLayoutName.error = "Name must be at least 2 characters"
                false
            }
            else -> {
                binding.textInputLayoutName.error = null
                true
            }
        }
    }

    private fun validateGender(): Boolean {
        val selectedGender = binding.spinnerGender.text.toString()
        return when {
            selectedGender.isEmpty() || (selectedGender != "Male" && selectedGender != "Female") -> {
                // Show error by highlighting the spinner
                binding.spinnerGender.error = "Please select a gender"
                false
            }
            else -> {
                binding.spinnerGender.error = null
                true
            }
        }
    }

    private fun saveProfile() {
        // Validate all fields
        val isNameValid = validateName()
        val isEmailValid = validateEmail()
        val isAgeValid = validateAge()
        val isHeightValid = validateHeight()
        val isWeightValid = validateWeight()
        val isGenderValid = validateGender()

        if (!isNameValid || !isEmailValid || !isAgeValid || !isHeightValid || !isWeightValid || !isGenderValid) {
            return
        }

        // Create updated profile
        val updatedProfile = currentProfile.copy(
            name = binding.editTextName.text.toString().trim(),
            email = binding.editTextEmail.text.toString().trim(),
            age = binding.editTextAge.text.toString().toInt(),
            height = binding.editTextHeight.text.toString().toFloat(),
            weight = binding.editTextWeight.text.toString().toFloat(),
            gender = binding.spinnerGender.text.toString()
        )

        onProfileUpdated(updatedProfile)
        dismiss()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}