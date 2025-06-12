package com.example.alva.ui.camera

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.OptIn
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.alva.R
import com.example.alva.databinding.FragmentCameraBinding
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class CameraFragment : Fragment() {

    private var _binding: FragmentCameraBinding? = null
    private val binding get() = _binding!!

    private lateinit var cameraViewModel: CameraViewModel
    private var imageCapture: ImageCapture? = null
    private var cameraExecutor: ExecutorService? = null
    private var isBarcodeScanMode = false
    private var camera: Camera? = null
    private var isFlashOn = false

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            startCamera()
        } else {
            Toast.makeText(context, "Camera permission is required", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        cameraViewModel = ViewModelProvider(this)[CameraViewModel::class.java]
        _binding = FragmentCameraBinding.inflate(inflater, container, false)

        setupUI()
        observeViewModel()

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (allPermissionsGranted()) {
            startCamera()
        } else {
            requestPermissionLauncher.launch(Manifest.permission.CAMERA)
        }

        cameraExecutor = Executors.newSingleThreadExecutor()
    }

    private fun setupUI() {
        // Set initial mode to food capture
        setFoodCaptureMode()

        binding.buttonCaptureFood.setOnClickListener {
            if (isBarcodeScanMode) {
                setFoodCaptureMode()
            } else {
                capturePhoto()
            }
        }

        binding.buttonScanBarcode.setOnClickListener {
            if (!isBarcodeScanMode) {
                setBarcodeScanMode()
            }
        }

        binding.buttonToggleFlash.setOnClickListener {
            toggleFlash()
        }
    }

    private fun setFoodCaptureMode() {
        isBarcodeScanMode = false
        binding.cardModeIndicator.visibility = View.GONE
        binding.barcodeOverlay.visibility = View.GONE
        binding.textInstructions.text = "Point camera at food to capture and analyze calories"

        // Update button appearance
        binding.buttonCaptureFood.setImageResource(R.drawable.ic_camera_24)
        binding.buttonCaptureFood.contentDescription = "Capture food photo"

        Toast.makeText(context, "Food capture mode", Toast.LENGTH_SHORT).show()
    }

    private fun setBarcodeScanMode() {
        isBarcodeScanMode = true
        binding.cardModeIndicator.visibility = View.VISIBLE
        binding.barcodeOverlay.visibility = View.VISIBLE
        binding.textModeIndicator.text = "Scanning for barcodes..."
        binding.textInstructions.text = "Align barcode within the frame to scan"

        // Update button appearance
        binding.buttonCaptureFood.setImageResource(R.drawable.ic_cancel_24)
        binding.buttonCaptureFood.contentDescription = "Cancel barcode scan"

        Toast.makeText(context, "Barcode scan mode - align barcode in frame", Toast.LENGTH_SHORT).show()
    }

    private fun observeViewModel() {
        cameraViewModel.recognizedFood.observe(viewLifecycleOwner) { foodInfo ->
            foodInfo?.let {
                showFoodRecognitionResult(it.name, it.estimatedCalories)
            }
        }

        cameraViewModel.scannedProduct.observe(viewLifecycleOwner) { productInfo ->
            productInfo?.let {
                showBarcodeResult(it.name, it.calories)
            }
        }

        cameraViewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())

        cameraProviderFuture.addListener({
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

            val preview = Preview.Builder()
                .build()
                .also {
                    it.setSurfaceProvider(binding.viewFinder.surfaceProvider)
                }

            imageCapture = ImageCapture.Builder()
                .build()

            val imageAnalyzer = ImageAnalysis.Builder()
                .build()
                .also {
                    it.setAnalyzer(cameraExecutor!!) { imageProxy ->
                        if (isBarcodeScanMode) {
                            scanBarcode(imageProxy)
                        } else {
                            imageProxy.close() // Close immediately if not in barcode mode
                        }
                    }
                }

            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            try {
                cameraProvider.unbindAll()
                camera = cameraProvider.bindToLifecycle(
                    this, cameraSelector, preview, imageCapture, imageAnalyzer
                )

                // Update flash button state
                updateFlashButton()
            } catch (exc: Exception) {
                Toast.makeText(context, "Failed to start camera", Toast.LENGTH_SHORT).show()
            }

        }, ContextCompat.getMainExecutor(requireContext()))
    }

    private fun capturePhoto() {
        val imageCapture = imageCapture ?: return

        binding.progressBar.visibility = View.VISIBLE

        // Create image capture callback
        val outputFileOptions = ImageCapture.OutputFileOptions.Builder(
            requireContext().cacheDir.resolve("captured_food_${System.currentTimeMillis()}.jpg")
        ).build()

        imageCapture.takePicture(
            outputFileOptions,
            ContextCompat.getMainExecutor(requireContext()),
            object : ImageCapture.OnImageSavedCallback {
                override fun onError(exception: ImageCaptureException) {
                    binding.progressBar.visibility = View.GONE
                    Toast.makeText(context, "Photo capture failed: ${exception.message}", Toast.LENGTH_SHORT).show()
                }

                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    output.savedUri?.let { uri ->
                        cameraViewModel.recognizeFood(uri)
                    }
                }
            }
        )
    }

    @OptIn(ExperimentalGetImage::class) private fun scanBarcode(imageProxy: ImageProxy) {
        val mediaImage = imageProxy.image
        if (mediaImage != null) {
            val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)

            val scanner = BarcodeScanning.getClient()
            scanner.process(image)
                .addOnSuccessListener { barcodes ->
                    for (barcode in barcodes) {
                        when (barcode.valueType) {
                            Barcode.TYPE_PRODUCT -> {
                                barcode.displayValue?.let { code ->
                                    cameraViewModel.lookupProduct(code)
                                    setFoodCaptureMode() // Return to food capture mode
                                }
                            }
                        }
                    }
                }
                .addOnFailureListener {
                    // Handle failure silently for continuous scanning
                }
                .addOnCompleteListener {
                    imageProxy.close()
                }
        } else {
            imageProxy.close()
        }
    }

    private fun toggleFlash() {
        camera?.let { cam ->
            val cameraControl = cam.cameraControl
            if (cam.cameraInfo.hasFlashUnit()) {
                isFlashOn = !isFlashOn
                cameraControl.enableTorch(isFlashOn)
                updateFlashButton()

                val flashStatus = if (isFlashOn) "on" else "off"
                Toast.makeText(context, "Flash $flashStatus", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(context, "Flash not available", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun updateFlashButton() {
        camera?.let { cam ->
            if (cam.cameraInfo.hasFlashUnit()) {
                val flashIcon = if (isFlashOn) R.drawable.ic_flash_on_24 else R.drawable.ic_flash_off_24
                binding.buttonToggleFlash.setImageResource(flashIcon)
                binding.buttonToggleFlash.alpha = 1.0f
            } else {
                binding.buttonToggleFlash.setImageResource(R.drawable.ic_flash_off_24)
                binding.buttonToggleFlash.alpha = 0.5f
            }
        }
    }

    private fun showFoodRecognitionResult(foodName: String, calories: Int) {
        FoodRecognitionResultDialog(
            foodName = foodName,
            estimatedCalories = calories,
            onConfirm = { confirmedName, confirmedCalories ->
                cameraViewModel.addRecognizedFood(confirmedName, confirmedCalories)
                Toast.makeText(context, "Added $confirmedName to your log", Toast.LENGTH_SHORT).show()
            },
            onCancel = {
                // User cancelled
            }
        ).show(parentFragmentManager, "FoodRecognitionResult")
    }

    private fun showBarcodeResult(productName: String, calories: Int) {
        BarcodeResultDialog(
            productName = productName,
            calories = calories,
            onConfirm = { confirmedCalories ->
                cameraViewModel.addScannedProduct(productName, confirmedCalories)
                Toast.makeText(context, "Added $productName to your log", Toast.LENGTH_SHORT).show()
            },
            onCancel = {
                // User cancelled
            }
        ).show(parentFragmentManager, "BarcodeResult")
    }

    private fun allPermissionsGranted() = ContextCompat.checkSelfPermission(
        requireContext(), Manifest.permission.CAMERA
    ) == PackageManager.PERMISSION_GRANTED

    override fun onDestroyView() {
        super.onDestroyView()
        cameraExecutor?.shutdown()
        _binding = null
    }
}