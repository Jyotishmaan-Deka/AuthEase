package com.deadlyord.authease.ui.home

import android.app.KeyguardManager
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
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
    private var isAuthenticating = false

    // Fix 2: Launcher for device credential (pattern/PIN/password) fallback
    private val deviceCredentialLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == android.app.Activity.RESULT_OK) {
                viewModel.setAuthenticated(true)
                showContent()
                requireContext().showToast("Authenticated successfully")
            } else {
                // Fix 1: User cancelled pattern — do NOT open the app
                requireActivity().finish()
            }
        }

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

        if (!viewModel.isAuthenticated.value) {
            // Fix 6: Show auth overlay immediately — hide codes until authenticated
            showAuthOverlay()
            authenticateUser()
        } else {
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
                    if (errorCode == BiometricPrompt.ERROR_NEGATIVE_BUTTON ||
                        errorCode == BiometricPrompt.ERROR_USER_CANCELED) {
                        // Fix 2: User tapped "Use Pattern" — launch device credential
                        launchPatternUnlock()
                    } else {
                        // Fix 1: Hardware error — close app, do not expose codes
                        requireActivity().finish()
                    }
                }

                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    isAuthenticating = false
                    viewModel.setAuthenticated(true)
                    if (isAdded && view != null) {
                        requireContext().showToast("Authenticated successfully")
                        showContent()
                    }
                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                    isAuthenticating = false
                    if (isAdded && view != null) {
                        requireContext().showToast("Authentication failed. Please try again.")
                        // Overlay stays visible — codes remain hidden
                    }
                }
            })
    }

    // Fix 6: Full-screen lock overlay hides codes during authentication
    private fun showAuthOverlay() {
        if (_binding == null) return
        binding.authOverlay.visibility = View.VISIBLE
        binding.recyclerViewAccounts.visibility = View.GONE
        binding.layoutEmpty.visibility = View.GONE
        binding.fabAddAccount.isEnabled = false
        binding.fabAddAccount.alpha = 0.5f
    }

    private fun showContent() {
        if (_binding == null) return
        binding.authOverlay.visibility = View.GONE
        binding.fabAddAccount.isEnabled = true
        binding.fabAddAccount.alpha = 1f
        // Restore correct list/empty state
        val isEmpty = adapter.currentList.isEmpty()
        binding.layoutEmpty.visibility = if (isEmpty) View.VISIBLE else View.GONE
        binding.recyclerViewAccounts.visibility = if (isEmpty) View.GONE else View.VISIBLE
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
                if (_binding != null) {
                    adapter.submitList(accounts)
                    // Only update visibility once authenticated
                    if (viewModel.isAuthenticated.value) {
                        val isEmpty = accounts.isEmpty()
                        binding.layoutEmpty.visibility = if (isEmpty) View.VISIBLE else View.GONE
                        binding.recyclerViewAccounts.visibility =
                            if (isEmpty) View.GONE else View.VISIBLE
                    }
                }
            }
        }
    }

    private fun authenticateUser() {
        if (isAuthenticating) return

        val biometricManager = BiometricManager.from(requireContext())
        when (biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_WEAK)) {
            BiometricManager.BIOMETRIC_SUCCESS -> {
                isAuthenticating = true
                // Fix 2: Negative button label tells user they can use pattern instead
                val promptInfo = BiometricPrompt.PromptInfo.Builder()
                    .setTitle(getString(R.string.auth_title))
                    .setSubtitle(getString(R.string.auth_subtitle))
                    .setNegativeButtonText(getString(R.string.auth_use_pattern))
                    .build()
                biometricPrompt.authenticate(promptInfo)
            }
            // Fix 1 & 2: No biometrics available — go straight to pattern/PIN
            else -> launchPatternUnlock()
        }
    }

    // Fix 2: System pattern/PIN/password as a proper alternative
    private fun launchPatternUnlock() {
        val keyguardManager =
            requireContext().getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager

        if (keyguardManager.isDeviceSecure) {
            @Suppress("DEPRECATION")
            val intent = keyguardManager.createConfirmDeviceCredentialIntent(
                getString(R.string.auth_title),
                getString(R.string.auth_pattern_subtitle)
            )
            if (intent != null) {
                deviceCredentialLauncher.launch(intent)
            } else {
                // Cannot create intent — close for safety
                requireActivity().finish()
            }
        } else {
            // No lock screen configured — warn and close
            requireContext().showToast(getString(R.string.auth_no_lock_screen))
            requireActivity().finish()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
