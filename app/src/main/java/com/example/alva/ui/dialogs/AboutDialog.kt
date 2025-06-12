package com.example.alva.ui.dialogs

import android.app.Dialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.example.alva.BuildConfig
import com.example.alva.databinding.DialogAboutBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import androidx.core.net.toUri

class AboutDialog : DialogFragment() {

    private var _binding: DialogAboutBinding? = null
    private val binding get() = _binding!!

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        _binding = DialogAboutBinding.inflate(layoutInflater)

        setupAboutContent()
        setupClickListeners()

        return MaterialAlertDialogBuilder(requireContext())
            .setTitle("About Alva")
            .setView(binding.root)
            .setPositiveButton("Close", null)
            .setNeutralButton("GitHub") { _, _ ->
                openGitHubRepository()
            }
            .create()
    }

    private fun setupAboutContent() {
        binding.apply {
            // App version and build info
            textViewAppVersion.text = "Version ${BuildConfig.VERSION_NAME} (Build ${BuildConfig.VERSION_CODE})"

            // Academic project description
            textViewAppDescription.text = buildString {
                append("Alva is a sophisticated Android application developed as an academic project ")
                append("demonstrating advanced mobile development skills, modern architectural patterns, ")
                append("and integration of cutting-edge AI technologies.\n\n")
                append("This application showcases proficiency in Android development, ")
                append("software engineering principles, and emerging technology adoption ")
                append("through practical implementation of nutrition tracking and AI-powered assistance.")
            }

            // Academic information section
            textViewAcademicInfo.text = buildString {
                append("🎯 LEARNING OBJECTIVES DEMONSTRATED:\n")
                append("• Advanced Android Development with Kotlin\n")
                append("• MVVM Architecture & Repository Pattern\n")
                append("• Room Database & Complex Query Design\n")
                append("• REST API Integration & Error Handling\n")
                append("• AI Technology Integration (OpenAI & Gemini)\n")
                append("• Computer Vision & ML Kit Implementation\n")
                append("• Material Design 3 & User Experience\n")
                append("• Performance Optimization & Memory Management")
            }

            // Technical features section
            textViewTechnicalFeatures.text = buildString {
                append("🛠️ TECHNICAL FEATURES:\n\n")
                append("• Dual AI Integration (OpenAI + Google Gemini)\n")
                append("• Computer Vision Food Recognition\n")
                append("• Barcode Scanning with ML Kit\n")
                append("• Comprehensive Room Database\n")
                append("• Advanced Statistical Analysis\n")
                append("• Offline-First Architecture\n")
                append("• Material Design 3 Implementation\n")
                append("• Reactive Programming with LiveData\n")
                append("• Coroutines for Asynchronous Operations\n")
                append("• Comprehensive Error Handling\n\n")
            }

            // Development info
            textViewDevelopmentInfo.text = buildString {
                append("💻 DEVELOPMENT INFORMATION:\n\n")
                append("Platform: Android (API 24-35)\n")
                append("Language: Kotlin ${getKotlinVersion()}\n")
                append("Architecture: MVVM + Repository Pattern\n")
                append("Build System: Gradle with Kotlin DSL\n")
                append("Database: Room Persistence Library\n")
                append("Networking: Retrofit + OkHttp\n")
                append("UI Framework: Material Design 3\n")
                append("Camera: CameraX + ML Kit\n")
                append("AI APIs: OpenAI GPT + Google Gemini\n\n")

            }

            // Contact and repository info
            textViewContactInfo.text = buildString {
                append("REPOSITORY:\n\n")
                append("📱 GitHub: https://github.com/Indexohmany/Alva.git\n\n")
                append("📋 PROJECT DOCUMENTATION:\n")
                append("• Complete technical documentation\n")
                append("• UML diagrams and architecture overview\n")
                append("• Academic assessment criteria mapping\n\n")
                append("🎓 ACADEMIC CONTEXT:\n")
                append("This project was developed to demonstrate mastery of ")
                append("modern Android development practices, clean architecture principles, ")
                append("and integration of emerging AI technologies in mobile applications.")
            }

            // Copyright and license
            textViewCopyright.text = buildString {
                append("© 2025 Indexohmany - Academic Project\n")
                append("Developed for educational purposes\n")
                append("Licensed under MIT License for academic use\n\n")
                append("⚠️ ACADEMIC PROJECT NOTICE:\n")
                append("This application was created as part of an academic curriculum ")
                append("to demonstrate advanced software development skills and modern ")
                append("technology integration. The project showcases practical implementation ")
                append("of computer science concepts in a real-world application context.")
            }

            // Links
            buttonViewGitHub.setOnClickListener {
                openGitHubRepository()
            }
        }
    }

    private fun setupClickListeners() {
        // Add click listener for expandable sections if needed
        binding.textViewAcademicInfo.setOnClickListener {
            // Could implement expandable sections for detailed academic info
        }
    }

    private fun openGitHubRepository() {
        try {
            val intent = Intent(Intent.ACTION_VIEW).apply {
                data = "https://github.com/Indexohmany/Alva.git".toUri()
            }
            startActivity(intent)
        } catch (e: Exception) {
            // Handle case where no browser is available
            // Fallback: show the URL in a Toast
            Toast.makeText(requireContext(), "No browser found. Visit:\n\"https://github.com/Indexohmany/Alva.git\"", Toast.LENGTH_LONG).show()
        }
    }

    private fun getKotlinVersion(): String {
        return try {
            val version = KotlinVersion.CURRENT
            "${version.major}.${version.minor}.${version.patch}"
        } catch (e: Exception) {
            "Unknown"
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}