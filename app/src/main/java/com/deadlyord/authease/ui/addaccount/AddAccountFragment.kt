package com.deadlyord.authease.ui.addaccount

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.deadlyord.authease.R
import com.deadlyord.authease.databinding.FragmentAddAccountBinding
import com.deadlyord.authease.utils.isValidBase32
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class AddAccountFragment : Fragment() {

    private var _binding: FragmentAddAccountBinding? = null
    private val binding get() = _binding!!

    private val viewModel: AddAccountViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddAccountBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupViews()
        observeUiState()
        observeNavigationEvents()
    }

    private fun setupViews() {
        binding.apply {
            editTextIssuer.addTextChangedListener(object : android.text.TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
                override fun afterTextChanged(s: android.text.Editable?) {
                    viewModel.updateIssuer(s.toString())
                    viewModel.clearError()
                }
            })

            editTextAccountName.addTextChangedListener(object : android.text.TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
                override fun afterTextChanged(s: android.text.Editable?) {
                    viewModel.updateAccountName(s.toString())
                    viewModel.clearError()
                }
            })

            editTextSecret.addTextChangedListener(object : android.text.TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
                override fun afterTextChanged(s: android.text.Editable?) {
                    val secret = s.toString().trim().uppercase()
                    viewModel.updateSecret(secret)
                    viewModel.clearError()

                    // Validate Base32 in real-time
                    if (secret.isNotEmpty() && !secret.isValidBase32()) {
                        editTextSecret.error = "Invalid Base32 format"
                    } else {
                        editTextSecret.error = null
                    }
                }
            })

            buttonAddAccount.setOnClickListener {
                val secret = editTextSecret.text.toString().trim().uppercase()
                if (secret.isNotEmpty() && !secret.isValidBase32()) {
                    Toast.makeText(requireContext(), "Invalid Base32 secret key format", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                viewModel.addAccount()
            }

            buttonScanQr.setOnClickListener {
                findNavController().navigate(R.id.action_addAccountFragment_to_qrScannerFragment)
            }
        }
    }

    private fun observeUiState() {
        lifecycleScope.launch {
            viewModel.uiState.collect { state ->
                binding.progressBar.visibility = if (state.isLoading) View.VISIBLE else View.GONE
                binding.buttonAddAccount.isEnabled = !state.isLoading

                state.error?.let { error ->
                    Toast.makeText(requireContext(), error, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun observeNavigationEvents() {
        lifecycleScope.launch {
            viewModel.navigationEvent.collect { event ->
                when (event) {
                    is NavigationEvent.NavigateBack -> {
                        findNavController().navigateUp()
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