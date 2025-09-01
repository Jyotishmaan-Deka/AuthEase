package com.deadlyord.authease.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.deadlyord.authease.R
import com.deadlyord.authease.auth.CryptoHelper
import com.deadlyord.authease.databinding.FragmentHomeBinding
import com.deadlyord.authease.ui.AccountAdapter
import com.deadlyord.authease.utils.showToast
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private val viewModel: HomeViewModel by viewModels()
    private lateinit var adapter: AccountAdapter
    private lateinit var cryptoHelper: CryptoHelper
    private lateinit var biometricPrompt: BiometricPrompt

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        cryptoHelper = CryptoHelper(requireContext())
        setupBiometric()
        setupRecyclerView()
        setupFab()
        observeAccounts()
    }

    private fun setupBiometric() {
        val executor = ContextCompat.getMainExecutor(requireContext())
        biometricPrompt = BiometricPrompt(this, executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                    requireContext().showToast("Authentication error: $errString")
                }

                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    // Authentication successful, user can now view OTP codes
                    requireContext().showToast("Authentication successful")
                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                    requireContext().showToast("Authentication failed")
                }
            })
    }

    private fun setupRecyclerView() {
        adapter = AccountAdapter(
            onDeleteClick = { account ->
                viewModel.deleteAccount(account)
            },
            onCopyClick = { otp ->
                // Copy OTP to clipboard
                val clipboard = ContextCompat.getSystemService(requireContext(), android.content.ClipboardManager::class.java)
                val clip = android.content.ClipData.newPlainText("OTP", otp)
                clipboard?.setPrimaryClip(clip)
                requireContext().showToast("OTP copied to clipboard")
            }
        )

        binding.recyclerViewAccounts.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@HomeFragment.adapter
        }
    }

    private fun setupFab() {
        binding.fabAddAccount.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_addAccountFragment)
        }
    }

    private fun observeAccounts() {
        lifecycleScope.launch {
            viewModel.accounts.collect { accounts ->
                adapter.submitList(accounts)

                if (accounts.isEmpty()) {
                    binding.textViewEmpty.visibility = View.VISIBLE
                    binding.recyclerViewAccounts.visibility = View.GONE
                } else {
                    binding.textViewEmpty.visibility = View.GONE
                    binding.recyclerViewAccounts.visibility = View.VISIBLE
                }
            }
        }
    }

    private fun authenticateUser() {
        val biometricManager = BiometricManager.from(requireContext())
        when (biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_WEAK)) {
            BiometricManager.BIOMETRIC_SUCCESS -> {
                val promptInfo = BiometricPrompt.PromptInfo.Builder()
                    .setTitle("Authenticate to view OTP codes")
                    .setSubtitle("Use your fingerprint or face to unlock")
                    .setNegativeButtonText("Cancel")
                    .build()

                biometricPrompt.authenticate(promptInfo)
            }
            BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE -> {
                requireContext().showToast("No biometric features available on this device")
            }
            BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE -> {
                requireContext().showToast("Biometric features are currently unavailable")
            }
            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> {
                requireContext().showToast("Please set up biometric authentication in device settings")
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}