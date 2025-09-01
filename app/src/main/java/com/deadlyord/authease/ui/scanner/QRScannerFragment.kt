package com.deadlyord.authease.ui.scanner


import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.deadlyord.authease.databinding.FragmentQrScannerBinding
import com.deadlyord.authease.utils.showToast
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class QRScannerFragment : Fragment() {

    private var _binding: FragmentQrScannerBinding? = null
    private val binding get() = _binding!!

    private val viewModel: QRScannerViewModel by viewModels()

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

        checkCameraPermission()
        observeViewModel()

        binding.buttonManualEntry.setOnClickListener {
            findNavController().navigateUp()
        }
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
        // For now, we'll simulate QR scanning with a button for testing
        binding.buttonSimulateScan.setOnClickListener {
            // Simulate scanning a QR code
            val testQRCode = "otpauth://totp/TestApp:user@test.com?secret=JBSWY3DPEHPK3PXP&issuer=TestApp"
            viewModel.processQRCode(testQRCode)
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
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}