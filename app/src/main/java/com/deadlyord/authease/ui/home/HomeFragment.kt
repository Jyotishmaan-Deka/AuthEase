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
            else -> { /* allow access */ }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
