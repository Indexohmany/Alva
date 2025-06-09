package com.example.alva.ui.dialogs

import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import com.example.alva.BuildConfig
import com.example.alva.databinding.DialogAboutBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class AboutDialog : DialogFragment() {

    private var _binding: DialogAboutBinding? = null
    private val binding get() = _binding!!

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        _binding = DialogAboutBinding.inflate(layoutInflater)
        
        binding.textViewAppVersion.text = "Version ${BuildConfig.VERSION_NAME}"
        binding.textViewAppDescription.text = "Alva is your personal nutrition assistant that helps you track your meals, monitor your calorie intake, and achieve your health goals."
        
        return MaterialAlertDialogBuilder(requireContext())
            .setTitle("About Alva")
            .setView(binding.root)
            .setPositiveButton("Close", null)
            .create()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
} 