package com.deadlyord.authease.ui.addaccount

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.deadlyord.authease.R
import com.deadlyord.authease.databinding.FragmentAddAccountBinding
import com.deadlyord.authease.utils.isValidBase32
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class AddAccountFragment : Fragment() {

    private var _binding: FragmentAddAccountBinding? = null
    private val binding get() = _binding!!

    private val viewModel: AddAccountViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddAccountBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupDropdowns()
        setupTextWatchers()
        setupButtons()
        observeUiState()
        observeNavigationEvents()
    }

    private fun setupDropdowns() {
        val algorithmAdapter = ArrayAdapter(
            requireContext(), android.R.layout.simple_dropdown_item_1line,
            listOf("SHA1", "SHA256", "SHA512")
        )
        binding.autoCompleteAlgorithm.setAdapter(algorithmAdapter)
        binding.autoCompleteAlgorithm.setText("SHA1", false)
        binding.autoCompleteAlgorithm.setOnItemClickListener { _, _, position, _ ->
            viewModel.updateAlgorithm(listOf("SHA1", "SHA256", "SHA512")[position])
        }

        val digitsAdapter = ArrayAdapter(
            requireContext(), android.R.layout.simple_dropdown_item_1line,
            listOf("6 digits", "8 digits")
        )
        binding.autoCompleteDigits.setAdapter(digitsAdapter)
        binding.autoCompleteDigits.setText("6 digits", false)
        binding.autoCompleteDigits.setOnItemClickListener { _, _, position, _ ->
            viewModel.updateDigits(if (position == 0) 6 else 8)
        }
    }

    private fun setupTextWatchers() {
        binding.editTextIssuer.addTextChangedListener(simpleWatcher { viewModel.updateIssuer(it) })
        binding.editTextAccountName.addTextChangedListener(simpleWatcher { viewModel.updateAccountName(it) })
        binding.editTextSecret.addTextChangedListener(simpleWatcher { secret ->
            val upper = secret.trim().uppercase()
            viewModel.updateSecret(upper)
            if (upper.isNotEmpty() && !upper.isValidBase32()) {
                binding.editTextSecret.error = "Invalid Base32 format"
            } else {
                binding.editTextSecret.error = null
            }
        })
    }

    private fun setupButtons() {
        binding.buttonAddAccount.setOnClickListener {
            val secret = binding.editTextSecret.text.toString().trim().uppercase()
            if (secret.isNotEmpty() && !secret.isValidBase32()) {
                Snackbar.make(binding.root, "Invalid Base32 secret key format", Snackbar.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            viewModel.addAccount()
        }
        binding.buttonScanQr.setOnClickListener {
            findNavController().navigate(R.id.action_addAccountFragment_to_qrScannerFragment)
        }
    }

    private fun observeUiState() {
        lifecycleScope.launch {
            viewModel.uiState.collect { state ->
                binding.progressBar.visibility = if (state.isLoading) View.VISIBLE else View.GONE
                binding.buttonAddAccount.isEnabled = !state.isLoading
                state.error?.let { error ->
                    Snackbar.make(binding.root, error, Snackbar.LENGTH_SHORT).show()
                    viewModel.clearError()
                }
            }
        }
    }

    private fun observeNavigationEvents() {
        lifecycleScope.launch {
            viewModel.navigationEvent.collect { event ->
                when (event) {
                    is NavigationEvent.NavigateBack -> findNavController().navigateUp()
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    /** Helper to avoid boilerplate TextWatcher implementations */
    private fun simpleWatcher(block: (String) -> Unit) = object : android.text.TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        override fun afterTextChanged(s: android.text.Editable?) { block(s.toString()) }
    }
}
