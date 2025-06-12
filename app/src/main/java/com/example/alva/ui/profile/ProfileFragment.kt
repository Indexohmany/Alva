package com.example.alva.ui.profile

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.example.alva.R
import com.example.alva.databinding.FragmentProfileBinding
import com.example.alva.ui.dialogs.CalorieGoalDialog
import com.example.alva.ui.dialogs.WeeklyStatsDialog
import com.example.alva.ui.dialogs.MonthlyStatsDialog
import com.example.alva.ui.dialogs.NotificationSettingsDialog
import com.example.alva.ui.dialogs.AboutDialog
import com.example.alva.ui.dialogs.LogoutConfirmationDialog
import com.example.alva.ui.dialogs.EditProfileDialog
import com.example.alva.ui.dialogs.ActivityLevelDialog
import com.example.alva.ui.dialogs.ImportDataDialog
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.example.alva.ui.dialogs.AvatarPickerDialog
import com.example.alva.data.models.Avatar

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    private lateinit var profileViewModel: ProfileViewModel

    companion object {
        private const val TAG = "ProfileFragment"
    }

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
        Log.d(TAG, "Setting up UI")

        // Profile picture click listener
        binding.imageViewProfile.setOnClickListener {
            Log.d(TAG, "Profile picture clicked")
            showImagePickerDialog()
        }

        // Edit profile button
        binding.buttonEditProfile.setOnClickListener {
            Log.d(TAG, "Edit profile clicked")
            showEditProfileDialog()
        }

        // Activity level click listener
        binding.layoutActivityLevel.setOnClickListener {
            Log.d(TAG, "Activity level clicked")
            showActivityLevelDialog()
        }

        // Calorie goal setting
        binding.buttonSetCalorieGoal.setOnClickListener {
            Log.d(TAG, "Set calorie goal clicked")
            showCalorieGoalDialog()
        }

        // Stats card click listeners
        binding.cardWeeklyStats.setOnClickListener {
            Log.d(TAG, "Weekly stats clicked")
            showWeeklyStatsDialog()
        }

        binding.cardMonthlyStats.setOnClickListener {
            Log.d(TAG, "Monthly stats clicked")
            showMonthlyStatsDialog()
        }

        // Settings click listeners
        binding.textViewNotificationSettings.setOnClickListener {
            Log.d(TAG, "Notification settings clicked")
            showNotificationSettings()
        }

        binding.textViewDataExport.setOnClickListener {
            Log.d(TAG, "Data export clicked")
            profileViewModel.exportUserData()
        }

        // Import Data option
        binding.textViewDataImport.setOnClickListener {
            Log.d(TAG, "Data import clicked")
            showImportDataDialog()
        }

        binding.textViewAbout.setOnClickListener {
            Log.d(TAG, "About clicked")
            showAboutDialog()
        }

        binding.textViewLogout.setOnClickListener {
            Log.d(TAG, "Logout clicked")
            showLogoutConfirmation()
        }

        // BMI info click listener
        binding.layoutBmiInfo.setOnClickListener {
            Log.d(TAG, "BMI info clicked")
            showBMIInfoDialog()
        }
    }

    private fun observeViewModel() {
        Log.d(TAG, "Setting up view model observers")

        profileViewModel.userProfile.observe(viewLifecycleOwner) { profile ->
            Log.d(TAG, "User profile updated: ${profile?.name}")
            profile?.let {
                // Update profile display
                binding.textViewUserName.text = it.name
                binding.textViewUserEmail.text = it.email
                binding.textViewAge.text = "${it.age} years"
                binding.textViewHeight.text = "${it.height} cm"
                binding.textViewWeight.text = "${it.weight} kg"
                binding.textViewCalorieGoal.text = "${it.calorieGoal} cal/day"
                binding.textViewActivityLevel.text = it.activityLevel
                binding.textViewGender.text = it.gender

                // Update BMI information
                val bmi = profileViewModel.calculateBMI()
                val bmiCategory = profileViewModel.getBMICategory()
                binding.textViewBmi.text = String.format("%.1f", bmi)
                binding.textViewBmiCategory.text = bmiCategory

                // Set BMI category color
                val bmiColor = when (bmiCategory) {
                    "Severely Underweight" -> R.color.bmi_severely_underweight
                    "Underweight" -> R.color.bmi_underweight
                    "Moderately Underweight" -> R.color.bmi_underweight
                    "Normal weight" -> R.color.bmi_normal
                    "Slightly Overweight" -> R.color.bmi_slightly_overweight
                    "Overweight" -> R.color.bmi_overweight
                    "Moderately Obese" -> R.color.bmi_moderately_obese
                    "Severely Obese" -> R.color.bmi_obese
                    "Very Severely Obese" -> R.color.bmi_obese
                    else -> R.color.text_secondary
                }
                binding.textViewBmiCategory.setTextColor(resources.getColor(bmiColor, null))

                // Load profile picture with simplified error handling
                loadProfilePicture(it.profilePictureUri)

                // Show recommended calories
                val recommendedCalories = profileViewModel.getRecommendedCalories()
                binding.textViewRecommendedCalories.text = "$recommendedCalories cal/day"

                // Show goal vs recommended comparison
                val goalDifference = it.calorieGoal - recommendedCalories
                if (goalDifference != 0) {
                    val differenceText = if (goalDifference > 0) {
                        "+${goalDifference} above recommended"
                    } else {
                        "${goalDifference} below recommended"
                    }
                    binding.textViewGoalDifference.text = differenceText
                    binding.textViewGoalDifference.visibility = View.VISIBLE
                } else {
                    binding.textViewGoalDifference.visibility = View.GONE
                }
            }
        }

        profileViewModel.weeklyStats.observe(viewLifecycleOwner) { stats ->
            stats?.let {
                binding.textViewWeeklyAverage.text = "${it.averageCalories} cal/day"
                binding.textViewWeeklyGoalAchieved.text = "${it.daysGoalMet}/7 days"
                binding.textViewWeeklyTotal.text = "${it.totalCalories} cal total"

                // Show weekly progress percentage
                val weeklyProgress = (it.daysGoalMet * 100) / 7
                binding.progressBarWeekly.progress = weeklyProgress
                binding.textViewWeeklyProgress.text = "$weeklyProgress%"
            }
        }

        profileViewModel.monthlyStats.observe(viewLifecycleOwner) { stats ->
            stats?.let {
                binding.textViewMonthlyAverage.text = "${it.averageCalories} cal/day"
                binding.textViewMonthlyGoalAchieved.text = "${it.daysGoalMet}/${it.totalDays} days"
                binding.textViewMonthlyTotal.text = "${it.totalCalories} cal total"

                // Show monthly progress percentage
                val monthlyProgress = if (it.totalDays > 0) {
                    (it.daysGoalMet * 100) / it.totalDays
                } else 0
                binding.progressBarMonthly.progress = monthlyProgress
                binding.textViewMonthlyProgress.text = "$monthlyProgress%"
            }
        }

        profileViewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            Log.d(TAG, "Loading state changed: $isLoading")
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE

            // Disable interaction during loading
            binding.cardProfile.isEnabled = !isLoading
            binding.cardWeeklyStats.isEnabled = !isLoading
            binding.cardMonthlyStats.isEnabled = !isLoading
        }

        profileViewModel.successMessage.observe(viewLifecycleOwner) { message ->
            message?.let {
                Log.d(TAG, "Success message: $it")
                Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
                profileViewModel.clearMessages()
            }
        }

        profileViewModel.errorMessage.observe(viewLifecycleOwner) { error ->
            error?.let {
                Log.e(TAG, "Error message: $it")
                Toast.makeText(context, it, Toast.LENGTH_LONG).show()
                profileViewModel.clearMessages()
            }
        }

        // Observe export file URI
        profileViewModel.exportFileUri.observe(viewLifecycleOwner) { fileUri ->
            fileUri?.let {
                Log.d(TAG, "Export file URI received: $it")
                shareExportedFile(it)
                profileViewModel.clearExportUri()
            }
        }
    }

    private fun loadProfilePicture(profilePictureUri: String?) {
        if (!profilePictureUri.isNullOrEmpty()) {
            try {
                val avatarId = profilePictureUri.toIntOrNull()
                if (avatarId != null) {
                    val avatar = Avatar.getAvatarById(avatarId)
                    if (avatar != null) {

                        Glide.with(this)
                            .load("file:///android_asset/${avatar.assetPath}")
                            .apply(RequestOptions.circleCropTransform())
                            .placeholder(R.drawable.ic_person_placeholder)
                            .error(R.drawable.ic_person_placeholder)
                            .into(binding.imageViewProfile)

                        return
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Exception loading avatar", e)
            }
        }

        // Fallback to default image
        Glide.with(this).clear(binding.imageViewProfile)
        binding.imageViewProfile.setImageResource(R.drawable.ic_person_placeholder)
    }

    private fun showImagePickerDialog() {
        val currentAvatarId = profileViewModel.getCurrentAvatarId()

        AvatarPickerDialog(
            currentAvatarId = currentAvatarId
        ) { selectedAvatarId ->
            profileViewModel.updateProfileAvatar(selectedAvatarId)
        }.show(parentFragmentManager, "AvatarPicker")
    }


    private fun showEditProfileDialog() {
        val currentProfile = profileViewModel.userProfile.value
        currentProfile?.let { profile ->
            EditProfileDialog(
                currentProfile = profile
            ) { updatedProfile ->
                profileViewModel.updateProfile(
                    updatedProfile.name,
                    updatedProfile.email,
                    updatedProfile.age,
                    updatedProfile.height,
                    updatedProfile.weight,
                    updatedProfile.gender
                )
            }.show(parentFragmentManager, "EditProfile")
        }
    }

    private fun showActivityLevelDialog() {
        val currentLevel = profileViewModel.userProfile.value?.activityLevel ?: "Moderate"
        ActivityLevelDialog(
            currentLevel = currentLevel
        ) { newLevel ->
            profileViewModel.updateActivityLevel(newLevel)
        }.show(parentFragmentManager, "ActivityLevel")
    }

    private fun showCalorieGoalDialog() {
        CalorieGoalDialog(
            currentGoal = profileViewModel.userProfile.value?.calorieGoal ?: 2000,
            recommendedGoal = profileViewModel.getRecommendedCalories()
        ) { newGoal ->
            profileViewModel.updateCalorieGoal(newGoal)
        }.show(parentFragmentManager, "CalorieGoal")
    }

    private fun showWeeklyStatsDialog() {
        val stats = profileViewModel.weeklyStats.value
        if (stats != null) {
            WeeklyStatsDialog(stats = stats).show(parentFragmentManager, "WeeklyStats")
        } else {
            Toast.makeText(context, "No weekly data available", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showMonthlyStatsDialog() {
        val stats = profileViewModel.monthlyStats.value
        if (stats != null) {
            MonthlyStatsDialog(stats = stats).show(parentFragmentManager, "MonthlyStats")
        } else {
            Toast.makeText(context, "No monthly data available", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showNotificationSettings() {
        NotificationSettingsDialog().show(parentFragmentManager, "NotificationSettings")
    }

    private fun showImportDataDialog() {
        ImportDataDialog { importedData ->
            profileViewModel.importUserData(importedData)
        }.show(parentFragmentManager, "ImportData")
    }

    private fun showBMIInfoDialog() {
        val bmi = profileViewModel.calculateBMI()
        val category = profileViewModel.getBMICategory()

        val bmiInfo = """
            Your BMI: %.1f
            Category: %s
            
            BMI Categories:
            • Underweight: < 18.5
            • Normal weight: 18.5 - 24.9
            • Overweight: 25 - 29.9
            • Obese: ≥ 30
            
            Note: BMI is a general indicator and may not account for muscle mass, bone density, and other factors.
        """.trimIndent().format(bmi, category)

        MaterialAlertDialogBuilder(requireContext())
            .setTitle("BMI Information")
            .setMessage(bmiInfo)
            .setPositiveButton("OK", null)
            .show()
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

    private fun shareExportedFile(fileUri: android.net.Uri) {
        try {
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_STREAM, fileUri)
                putExtra(Intent.EXTRA_SUBJECT, "Alva Profile Export")
                putExtra(Intent.EXTRA_TEXT, "Here is my exported Alva profile data.")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            // Create a more descriptive chooser
            val chooserIntent = Intent.createChooser(shareIntent, "Save Export File")

            // Add option to save to Downloads via file manager
            val targetedShareIntents = mutableListOf<Intent>()

            // Try to add Google Drive, Dropbox, or file manager options
            val pm = requireActivity().packageManager
            val resolveInfos = pm.queryIntentActivities(shareIntent, 0)

            for (resolveInfo in resolveInfos) {
                val packageName = resolveInfo.activityInfo.packageName
                if (packageName.contains("drive") ||
                    packageName.contains("dropbox") ||
                    packageName.contains("files") ||
                    packageName.contains("documents")) {

                    val targetedIntent = Intent(shareIntent).apply {
                        setPackage(packageName)
                    }
                    targetedShareIntents.add(targetedIntent)
                }
            }

            if (targetedShareIntents.isNotEmpty()) {
                chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, targetedShareIntents.toTypedArray())
            }

            startActivity(chooserIntent)

            // Show helpful message
            Toast.makeText(
                context,
                "Choose 'Files' or 'Downloads' to save to your device, or select any app to share",
                Toast.LENGTH_LONG
            ).show()

        } catch (e: Exception) {
            Log.e(TAG, "Error sharing file", e)
            Toast.makeText(context, "Error sharing file: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}