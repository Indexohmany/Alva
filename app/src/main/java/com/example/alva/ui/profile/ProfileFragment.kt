package com.example.alva.ui.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.alva.databinding.FragmentProfileBinding
import com.example.alva.ui.dialogs.ImagePickerDialog
import com.example.alva.ui.dialogs.CalorieGoalDialog
import com.example.alva.ui.dialogs.WeeklyStatsDialog
import com.example.alva.ui.dialogs.MonthlyStatsDialog
import com.example.alva.ui.dialogs.NotificationSettingsDialog
import com.example.alva.ui.dialogs.AboutDialog
import com.example.alva.ui.dialogs.LogoutConfirmationDialog

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    private lateinit var profileViewModel: ProfileViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        profileViewModel = ViewModelProvider(this)[ProfileViewModel::class.java]
        _binding = FragmentProfileBinding.inflate(inflater, container, false)

        setupUI()
        observeViewModel()

        return binding.root
    }

    private fun setupUI() {
        // Profile picture click listener
        binding.imageViewProfile.setOnClickListener {
            // Open image picker or camera for profile picture
            showImagePickerDialog()
        }

        // Save profile button
        binding.buttonSaveProfile.setOnClickListener {
            saveProfile()
        }

        // Calorie goal setting
        binding.buttonSetCalorieGoal.setOnClickListener {
            showCalorieGoalDialog()
        }

        // Stats card click listeners
        binding.cardWeeklyStats.setOnClickListener {
            // Navigate to detailed weekly stats
            showWeeklyStatsDialog()
        }

        binding.cardMonthlyStats.setOnClickListener {
            // Navigate to detailed monthly stats
            showMonthlyStatsDialog()
        }

        // Settings click listeners
        binding.textViewNotificationSettings.setOnClickListener {
            // Navigate to notification settings
            showNotificationSettings()
        }

        binding.textViewDataExport.setOnClickListener {
            // Export user data
            profileViewModel.exportUserData()
        }

        binding.textViewAbout.setOnClickListener {
            // Show about dialog
            showAboutDialog()
        }

        binding.textViewLogout.setOnClickListener {
            // Logout functionality
            showLogoutConfirmation()
        }
    }

    private fun observeViewModel() {
        profileViewModel.userProfile.observe(viewLifecycleOwner) { profile ->
            profile?.let {
                binding.textViewUserName.text = it.name
                binding.textViewUserEmail.text = it.email
                binding.textViewAge.text = "${it.age} years"
                binding.textViewHeight.text = "${it.height} cm"
                binding.textViewWeight.text = "${it.weight} kg"
                binding.textViewCalorieGoal.text = "${it.calorieGoal} cal/day"
                binding.textViewActivityLevel.text = it.activityLevel

                // Set profile picture if available
                if (it.profilePictureUri != null) {
                    // Load profile picture using image loading library
                    // Glide.with(this).load(it.profilePictureUri).into(binding.imageViewProfile)
                }
            }
        }

        profileViewModel.weeklyStats.observe(viewLifecycleOwner) { stats ->
            binding.textViewWeeklyAverage.text = "${stats.averageCalories} cal/day"
            binding.textViewWeeklyGoalAchieved.text = "${stats.daysGoalMet}/7 days"
        }

        profileViewModel.monthlyStats.observe(viewLifecycleOwner) { stats ->
            binding.textViewMonthlyAverage.text = "${stats.averageCalories} cal/day"
            binding.textViewMonthlyGoalAchieved.text = "${stats.daysGoalMet}/${stats.totalDays} days"
        }

        profileViewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }

        profileViewModel.successMessage.observe(viewLifecycleOwner) { message ->
            message?.let {
                Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
                profileViewModel.clearMessages()
            }
        }

        profileViewModel.errorMessage.observe(viewLifecycleOwner) { error ->
            error?.let {
                Toast.makeText(context, it, Toast.LENGTH_LONG).show()
                profileViewModel.clearMessages()
            }
        }
    }

    private fun saveProfile() {
        val name = binding.editTextName.text.toString().trim()
        val email = binding.editTextEmail.text.toString().trim()
        val age = binding.editTextAge.text.toString().toIntOrNull() ?: 0
        val height = binding.editTextHeight.text.toString().toFloatOrNull() ?: 0f
        val weight = binding.editTextWeight.text.toString().toFloatOrNull() ?: 0f

        if (validateProfileData(name, email, age, height, weight)) {
            profileViewModel.updateProfile(name, email, age, height, weight)
        }
    }

    private fun validateProfileData(name: String, email: String, age: Int, height: Float, weight: Float): Boolean {
        return when {
            name.isEmpty() -> {
                binding.editTextName.error = "Name is required"
                false
            }
            email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches() -> {
                binding.editTextEmail.error = "Valid email is required"
                false
            }
            age <= 0 || age > 150 -> {
                binding.editTextAge.error = "Enter a valid age"
                false
            }
            height <= 0 || height > 300 -> {
                binding.editTextHeight.error = "Enter a valid height"
                false
            }
            weight <= 0 || weight > 500 -> {
                binding.editTextWeight.error = "Enter a valid weight"
                false
            }
            else -> true
        }
    }

    private fun showImagePickerDialog() {
        ImagePickerDialog { imageUri ->
            profileViewModel.updateProfilePicture(imageUri)
        }.show(parentFragmentManager, "ImagePicker")
    }

    private fun showCalorieGoalDialog() {
        CalorieGoalDialog(
            currentGoal = profileViewModel.userProfile.value?.calorieGoal ?: 2000
        ) { newGoal ->
            profileViewModel.updateCalorieGoal(newGoal)
        }.show(parentFragmentManager, "CalorieGoal")
    }

    private fun showWeeklyStatsDialog() {
        WeeklyStatsDialog(
            stats = profileViewModel.weeklyStats.value
        ).show(parentFragmentManager, "WeeklyStats")
    }

    private fun showMonthlyStatsDialog() {
        MonthlyStatsDialog(
            stats = profileViewModel.monthlyStats.value
        ).show(parentFragmentManager, "MonthlyStats")
    }

    private fun showNotificationSettings() {
        NotificationSettingsDialog().show(parentFragmentManager, "NotificationSettings")
    }

    private fun showAboutDialog() {
        AboutDialog().show(parentFragmentManager, "About")
    }

    private fun showLogoutConfirmation() {
        LogoutConfirmationDialog {
            profileViewModel.logout()
            // Navigate to login screen or close app
        }.show(parentFragmentManager, "LogoutConfirmation")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}