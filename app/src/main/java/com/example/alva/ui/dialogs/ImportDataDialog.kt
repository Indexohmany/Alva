package com.example.alva.ui.dialogs

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.DialogFragment
import com.example.alva.databinding.DialogImportDataBinding
import java.io.BufferedReader
import java.io.InputStreamReader

class ImportDataDialog(
    private val onDataImported: (String) -> Unit
) : DialogFragment() {

    private var _binding: DialogImportDataBinding? = null
    private val binding get() = _binding!!

    private val filePickerLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                try {
                    val inputStream = requireContext().contentResolver.openInputStream(uri)
                    val reader = BufferedReader(InputStreamReader(inputStream))
                    val fileContent = reader.readText()
                    reader.close()

                    binding.editTextImportData.setText(fileContent)
                    Toast.makeText(context, "File loaded successfully", Toast.LENGTH_SHORT).show()
                } catch (e: Exception) {
                    Toast.makeText(context, "Error reading file: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogImportDataBinding.inflate(inflater, container, false)
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
            (resources.displayMetrics.heightPixels * 0.8).toInt()
        )
        dialog?.setCancelable(true)
    }

    private fun setupUI() {
        // Setup instructions
        binding.textViewInstructions.text = """
            Import your previously exported ALVA data:
            
            1. Select "Choose File" to pick an exported file, or
            2. Paste your export data directly into the text area below
            3. Click "Import" to restore your data
            
            Note: This will replace your current profile data.
        """.trimIndent()

        // Setup buttons
        binding.buttonChooseFile.setOnClickListener {
            openFilePicker()
        }

        binding.buttonCancel.setOnClickListener {
            dismiss()
        }

        binding.buttonImport.setOnClickListener {
            importData()
        }

        // Setup text area
        binding.editTextImportData.hint = "Paste your exported ALVA data here..."

        // Add sample data button for testing
        binding.buttonShowSample.setOnClickListener {
            showSampleFormat()
        }
    }

    private fun openFilePicker() {
        val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
            type = "*/*"
            addCategory(Intent.CATEGORY_OPENABLE)
            putExtra(Intent.EXTRA_MIME_TYPES, arrayOf("text/plain", "application/json", "*/*"))
        }

        filePickerLauncher.launch(Intent.createChooser(intent, "Select export file"))
    }

    private fun showSampleFormat() {
        val sampleData = """
            ALVA User Profile Export
            ========================
            Export Date: 2025-06-11 14:30:00
            Export Version: 1.0
            
            Profile Information:
            -------------------
            Name: User
            Email: user@example.com
            Age: 30 years
            Height: 175.0 cm
            Weight: 70.0 kg
            Gender: Male
            Activity Level: Moderate
            Calorie Goal: 2200 cal/day
            Dietary Preferences: Vegetarian
            Profile Created: 2025-01-01 10:00:00
            Last Updated: 2025-06-11 14:30:00
            
            JSON Data (for import):
            ----------------------
            {
              "exportVersion": "1.0",
              "exportDate": "2025-06-11 14:30:00",
              "userProfile": {
                "id": 1234567890,
                "name": "User",
                "email": "user@example.com",
                "age": 30,
                "height": 175.0,
                "weight": 70.0,
                "gender": "Male",
                "activityLevel": "Moderate",
                "calorieGoal": 2200,
                "profilePictureUri": "",
                "dietaryPreferences": "Vegetarian",
                "createdAt": "2025-01-01 10:00:00",
                "updatedAt": "2025-06-11 14:30:00"
              }
            }
        """.trimIndent()

        binding.editTextImportData.setText(sampleData)
        Toast.makeText(context, "Sample format loaded", Toast.LENGTH_SHORT).show()
    }

    private fun importData() {
        val importText = binding.editTextImportData.text.toString().trim()

        if (importText.isEmpty()) {
            Toast.makeText(context, "Please enter data to import", Toast.LENGTH_SHORT).show()
            return
        }

        // Basic validation
        if (!importText.contains("userProfile") || !importText.contains("{")) {
            Toast.makeText(context, "Invalid import format. Please check your data.", Toast.LENGTH_LONG).show()
            return
        }

        // Show confirmation dialog
        showConfirmationDialog(importText)
    }

    private fun showConfirmationDialog(importData: String) {
        val confirmDialog = androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("Confirm Import")
            .setMessage("This will replace your current profile data. Are you sure you want to continue?")
            .setPositiveButton("Import") { _, _ ->
                onDataImported(importData)
                dismiss()
            }
            .setNegativeButton("Cancel", null)
            .create()

        confirmDialog.show()
    }

    private fun validateImportData(data: String): Boolean {
        return try {
            // Basic validation - check if it contains required fields
            data.contains("userProfile") &&
                    data.contains("name") &&
                    data.contains("email") &&
                    data.contains("{") &&
                    data.contains("}")
        } catch (e: Exception) {
            false
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}