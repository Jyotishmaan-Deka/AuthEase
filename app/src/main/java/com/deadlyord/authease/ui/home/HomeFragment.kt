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
    private lateinit var biometricPrompt: BiometricPrompt
    private var isAuthenticating = false // Prevent multiple auth prompts

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

        setupBiometric()
        setupRecyclerView()
        setupFab()
        observeAccounts()

        // Only authenticate if not already authenticated
        // StateFlow value needs .value to access
        if (!viewModel.isAuthenticated.value) {
            authenticateUser()
        } else {
            // Already authenticated, just ensure content is visible
            showContent()
        }
    }

    private fun setupBiometric() {
        val executor = ContextCompat.getMainExecutor(requireContext())
        biometricPrompt = BiometricPrompt(this, executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                    isAuthenticating = false
                    // Still allow access but mark as not authenticated
                    viewModel.setAuthenticated(false)
                    showContent()
                    // Optional: Show error message
                    requireContext().showToast("Authentication error: $errString")
                }

                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    isAuthenticating = false
                    viewModel.setAuthenticated(true)
                    requireContext().showToast("Authenticated successfully")
                    showContent()
                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                    isAuthenticating = false
                    requireContext().showToast("Authentication failed. Please try again.")
                    // Keep showing content or retry?
                    // For better UX, you might want to retry
                    showContent()
                }
            })
    }

    private fun showContent() {
        // Ensure UI is interactive
        binding.recyclerViewAccounts.alpha = 1f
        binding.fabAddAccount.isEnabled = true
        binding.fabAddAccount.alpha = 1f

        // If you want to hide content until authenticated, uncomment below:
        // binding.recyclerViewAccounts.visibility = View.VISIBLE
        // binding.layoutEmpty.visibility = if (adapter.itemCount == 0) View.VISIBLE else View.GONE
    }

    private fun setupRecyclerView() {
        adapter = AccountAdapter(
            onDeleteClick = { account -> viewModel.deleteAccount(account) },
            onCopyClick = { otp ->
                val clipboard = ContextCompat.getSystemService(
                    requireContext(), android.content.ClipboardManager::class.java
                )
                clipboard?.setPrimaryClip(android.content.ClipData.newPlainText("OTP", otp))
                requireContext().showToast(getString(R.string.otp_copied))
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
                binding.layoutEmpty.visibility = if (accounts.isEmpty()) View.VISIBLE else View.GONE
                binding.recyclerViewAccounts.visibility = if (accounts.isEmpty()) View.GONE else View.VISIBLE
            }
        }
    }

    private fun authenticateUser() {
        // Prevent multiple auth prompts from showing simultaneously
        if (isAuthenticating) return

        val biometricManager = BiometricManager.from(requireContext())
        when (biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_WEAK)) {
            BiometricManager.BIOMETRIC_SUCCESS -> {
                isAuthenticating = true
                val promptInfo = BiometricPrompt.PromptInfo.Builder()
                    .setTitle(getString(R.string.auth_title))
                    .setSubtitle(getString(R.string.auth_subtitle))
                    .setNegativeButtonText(getString(R.string.auth_cancel))
                    .build()
                biometricPrompt.authenticate(promptInfo)
            }
            BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE -> {
                // No biometric hardware available
                requireContext().showToast("Biometric hardware not available")
                viewModel.setAuthenticated(true) // Allow access anyway
                showContent()
            }
            BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE -> {
                // Biometric hardware is currently unavailable
                requireContext().showToast("Biometric hardware unavailable")
                viewModel.setAuthenticated(true) // Allow access anyway
                showContent()
            }
            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> {
                // No biometrics enrolled
                requireContext().showToast("No biometrics enrolled. Please set up in device settings.")
                viewModel.setAuthenticated(true) // Allow access anyway
                showContent()
            }
            else -> {
                // Silently fall through on other cases
                viewModel.setAuthenticated(true)
                showContent()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

/*@AndroidEntryPoint
class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private val viewModel: HomeViewModel by viewModels()
    private lateinit var adapter: AccountAdapter
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

        setupBiometric()
        setupRecyclerView()
        setupFab()
        observeAccounts()

        // FIX: authenticateUser() was defined but never called — invoke it on screen entry
        authenticateUser()
    }

    private fun setupBiometric() {
        val executor = ContextCompat.getMainExecutor(requireContext())
        biometricPrompt = BiometricPrompt(this, executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                    // Gracefully degrade — allow access even if biometric is cancelled/unavailable
                }

                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    requireContext().showToast("Authenticated")
                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                    requireContext().showToast("Authentication failed")
                }
            })
    }

    private fun setupRecyclerView() {
        adapter = AccountAdapter(
            onDeleteClick = { account -> viewModel.deleteAccount(account) },
            onCopyClick = { otp ->
                val clipboard = ContextCompat.getSystemService(
                    requireContext(), android.content.ClipboardManager::class.java
                )
                clipboard?.setPrimaryClip(android.content.ClipData.newPlainText("OTP", otp))
                requireContext().showToast(getString(R.string.otp_copied))
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
                // Updated IDs to match new fragment_home.xml layout
                binding.layoutEmpty.visibility = if (accounts.isEmpty()) View.VISIBLE else View.GONE
                binding.recyclerViewAccounts.visibility = if (accounts.isEmpty()) View.GONE else View.VISIBLE
            }
        }
    }

    private fun authenticateUser() {
        val biometricManager = BiometricManager.from(requireContext())
        when (biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_WEAK)) {
            BiometricManager.BIOMETRIC_SUCCESS -> {
                val promptInfo = BiometricPrompt.PromptInfo.Builder()
                    .setTitle(getString(R.string.auth_title))
                    .setSubtitle(getString(R.string.auth_subtitle))
                    .setNegativeButtonText(getString(R.string.auth_cancel))
                    .build()
                biometricPrompt.authenticate(promptInfo)
            }
            // Silently fall through on devices without biometrics
            else -> { *//* allow access *//* }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}*/
