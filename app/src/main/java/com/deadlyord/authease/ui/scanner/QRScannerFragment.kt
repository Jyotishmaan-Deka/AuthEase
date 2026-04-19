package com.deadlyord.authease.ui.scanner

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.FrameLayout
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.OptIn
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.deadlyord.authease.R
import com.deadlyord.authease.databinding.FragmentQrScannerBinding
import com.deadlyord.authease.utils.showToast
import com.google.mlkit.vision.barcode.BarcodeScanner
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

@AndroidEntryPoint
class QRScannerFragment : Fragment() {

    private var _binding: FragmentQrScannerBinding? = null
    private val binding get() = _binding!!

    private val viewModel: QRScannerViewModel by viewModels()

    private var cameraExecutor: ExecutorService? = null
    private var barcodeScanner: BarcodeScanner? = null
    private var isScanning = true

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            startCamera()
        } else {
            requireContext().showToast("Camera permission is required to scan QR codes")
            findNavController().navigateUp()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentQrScannerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        barcodeScanner = BarcodeScanning.getClient()
        cameraExecutor = Executors.newSingleThreadExecutor()

        checkCameraPermission()
        observeViewModel()
        setupButtons()
//        setupScanAnimation()
    }

    private fun setupScanAnimation() {
        // Find the overlay view and add scan line animation if it exists
        val overlayView = binding.root.findViewById<View>(R.id.scanOverlay)
        if (overlayView != null) {
            try {
                val scanAnimation = AnimationUtils.loadAnimation(
                    requireContext(),
                    R.anim.scan_animate
                )
                overlayView.startAnimation(scanAnimation)
            } catch (e: Exception) {
                // Animation file might not exist, that's fine
            }
        }
    }

    private fun setupButtons() {
        binding.buttonSimulateScan.setOnClickListener {
            isScanning = false // Pause camera scanning while showing demo
            showSimulateScanDialog()
        }

        binding.buttonManualEntry.setOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun showSimulateScanDialog() {
        val sampleQRCodes = arrayOf(
            "Test Account (SHA1, 6 digits)",
            "Test Account (SHA256, 8 digits)",
            "GitHub Account (Demo)",
            "Custom QR code"
        )

        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("Simulate QR Scan")
            .setMessage("Choose a sample QR code to simulate scanning:")
            .setItems(sampleQRCodes) { _, which ->
                when (which) {
                    0 -> {
                        val testQR = "otpauth://totp/TestApp:demo@test.com?secret=JBSWY3DPEHPK3PXP&issuer=TestApp&algorithm=SHA1&digits=6"
                        viewModel.processQRCode(testQR)
                    }
                    1 -> {
                        val testQR = "otpauth://totp/SecureApp:secure@test.com?secret=JBSWY3DPEHPK3PXP&issuer=SecureApp&algorithm=SHA256&digits=8"
                        viewModel.processQRCode(testQR)
                    }
                    2 -> {
                        val testQR = "otpauth://totp/GitHub:username@gmail.com?secret=JBSWY3DPEHPK3PXP&issuer=GitHub&algorithm=SHA1&digits=6"
                        viewModel.processQRCode(testQR)
                    }
                    3 -> {
                        showCustomQRInputDialog()
                    }
                }
            }
            .setNegativeButton("Cancel") { _, _ ->
                isScanning = true // Resume scanning
            }
            .show()
    }

    private fun showCustomQRInputDialog() {
        val input = android.widget.EditText(requireContext())
        input.hint = "otpauth://totp/Example:user@example.com?secret=SECRETKEY&issuer=Example"
        input.setText("otpauth://totp/Example:test@example.com?secret=JBSWY3DPEHPK3PXP&issuer=Example")

        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("Enter QR Code URI")
            .setMessage("Paste or type the otpauth:// URI:")
            .setView(input)
            .setPositiveButton("Simulate") { _, _ ->
                val uri = input.text.toString().trim()
                if (uri.isNotEmpty()) {
                    viewModel.processQRCode(uri)
                } else {
                    requireContext().showToast("Please enter a valid URI")
                    isScanning = true
                }
            }
            .setNegativeButton("Cancel") { _, _ ->
                isScanning = true // Resume scanning
            }
            .show()
    }

    private fun checkCameraPermission() {
        when {
            ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED -> {
                startCamera()
            }
            else -> {
                requestPermissionLauncher.launch(Manifest.permission.CAMERA)
            }
        }
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())

        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()

            // Find the FrameLayout that will hold the camera preview
            val cameraContainer = binding.root.findViewById<FrameLayout>(R.id.cameraContainer)

            if (cameraContainer != null) {
                // Clear any existing views in the container
                cameraContainer.removeAllViews()

                // Create and add PreviewView
                val previewView = androidx.camera.view.PreviewView(requireContext()).apply {
                    layoutParams = FrameLayout.LayoutParams(
                        FrameLayout.LayoutParams.MATCH_PARENT,
                        FrameLayout.LayoutParams.MATCH_PARENT
                    )
                    scaleType = androidx.camera.view.PreviewView.ScaleType.FILL_CENTER
                }
                cameraContainer.addView(previewView)

                val preview = Preview.Builder()
                    .build()
                    .also {
                        it.setSurfaceProvider(previewView.surfaceProvider)
                    }

                val imageAnalysis = ImageAnalysis.Builder()
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                    .build()
                    .also {
                        it.setAnalyzer(cameraExecutor!!) { imageProxy ->
                            if (isScanning) {
                                processImageProxy(imageProxy)
                            } else {
                                imageProxy.close()
                            }
                        }
                    }

                val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

                try {
                    cameraProvider.unbindAll()
                    cameraProvider.bindToLifecycle(
                        this, cameraSelector, preview, imageAnalysis
                    )
                } catch (e: Exception) {
                    e.printStackTrace()
                    requireContext().showToast("Failed to start camera: ${e.message}")
                }
            } else {
                requireContext().showToast("Camera container not found")
            }
        }, ContextCompat.getMainExecutor(requireContext()))
    }

    @OptIn(ExperimentalGetImage::class)
    private fun processImageProxy(imageProxy: androidx.camera.core.ImageProxy) {
        val mediaImage = imageProxy.image
        if (mediaImage != null) {
            val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)

            barcodeScanner?.process(image)
                ?.addOnSuccessListener { barcodes ->
                    for (barcode in barcodes) {
                        val qrContent = barcode.rawValue
                        if (!qrContent.isNullOrEmpty()) {
                            isScanning = false // Stop scanning after finding a QR code
                            viewModel.processQRCode(qrContent)
                            break
                        }
                    }
                }
                ?.addOnCompleteListener {
                    imageProxy.close()
                }
        } else {
            imageProxy.close()
        }
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            viewModel.navigationEvent.collect { event ->
                when (event) {
                    is QRScannerNavigationEvent.NavigateBackWithAccount -> {
                        requireContext().showToast("Account added successfully!")
                        findNavController().navigateUp()
                    }
                    is QRScannerNavigationEvent.ShowError -> {
                        requireContext().showToast(event.message)
                        // Resume scanning after error
                        isScanning = true
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        cameraExecutor?.shutdown()
        barcodeScanner?.close()
        _binding = null
    }
}