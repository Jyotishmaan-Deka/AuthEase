package com.deadlyord.authease.ui.settings

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.deadlyord.authease.R
import com.deadlyord.authease.databinding.FragmentSettingsBinding
import com.deadlyord.authease.utils.showToast
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!

    private val viewModel: SettingsViewModel by viewModels()

    // File picker for import
    private val importLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri -> handleImport(uri) }
        }
    }

    // File picker for export
    private val exportLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri -> handleExport(uri) }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupToolbar()
        setupThemeSection()
        setupTransferSection()
        setupHelpSection()
        observeViewModel()
    }

    // NEW: Setup toolbar with home button
    private fun setupToolbar() {
        // Show the back/home button in toolbar
        (requireActivity() as AppCompatActivity).supportActionBar?.setDisplayHomeAsUpEnabled(true)
        (requireActivity() as AppCompatActivity).supportActionBar?.setHomeButtonEnabled(true)

        // Add menu provider for additional menu items
        requireActivity().addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: android.view.MenuInflater) {
                // Clear existing menu items (like settings)
                menu.clear()
                // Add home menu item
                menuInflater.inflate(R.menu.menu_settings, menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                return when (menuItem.itemId) {
                    android.R.id.home -> {
                        // Navigate back to home
                        findNavController().navigate(R.id.homeFragment)
                        true
                    }
                    R.id.action_home -> {
                        // Navigate to home fragment
                        findNavController().navigate(R.id.homeFragment)
                        true
                    }
                    else -> false
                }
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)
    }

    // ── Theme ──────────────────────────────────────────────────────────────────

    private fun setupThemeSection() {
        binding.itemTheme.setOnClickListener { showThemeDialog() }
    }

    private fun showThemeDialog() {
        val options = arrayOf(
            getString(R.string.theme_system),
            getString(R.string.theme_light),
            getString(R.string.theme_dark)
        )
        val currentMode = AppCompatDelegate.getDefaultNightMode()
        val checkedItem = when (currentMode) {
            AppCompatDelegate.MODE_NIGHT_NO -> 1
            AppCompatDelegate.MODE_NIGHT_YES -> 2
            else -> 0
        }

        MaterialAlertDialogBuilder(requireContext())
            .setTitle(getString(R.string.settings_theme))
            .setSingleChoiceItems(options, checkedItem) { dialog, which ->
                val mode = when (which) {
                    1 -> AppCompatDelegate.MODE_NIGHT_NO
                    2 -> AppCompatDelegate.MODE_NIGHT_YES
                    else -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
                }
                AppCompatDelegate.setDefaultNightMode(mode)
                viewModel.saveThemeMode(mode, requireContext())
                dialog.dismiss()
            }
            .show()
    }

    // ── Import / Export ────────────────────────────────────────────────────────

    private fun setupTransferSection() {
        binding.itemExport.setOnClickListener { confirmExport() }
        binding.itemImport.setOnClickListener { confirmImport() }
    }

    private fun confirmExport() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(getString(R.string.settings_export))
            .setMessage(getString(R.string.export_confirm_message))
            .setPositiveButton(getString(R.string.export_proceed)) { _, _ -> launchExportPicker() }
            .setNegativeButton(getString(R.string.cancel), null)
            .show()
    }

    private fun launchExportPicker() {
        val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "application/json"
            putExtra(Intent.EXTRA_TITLE, "authease_backup.json")
        }
        exportLauncher.launch(intent)
    }

    private fun handleExport(uri: Uri) {
        lifecycleScope.launch {
            val success = viewModel.exportAccounts(uri, requireContext().contentResolver)
            if (success) {
                requireContext().showToast(getString(R.string.export_success))
            } else {
                requireContext().showToast(getString(R.string.export_failed))
            }
        }
    }

    private fun confirmImport() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(getString(R.string.settings_import))
            .setMessage(getString(R.string.import_confirm_message))
            .setPositiveButton(getString(R.string.import_proceed)) { _, _ -> launchImportPicker() }
            .setNegativeButton(getString(R.string.cancel), null)
            .show()
    }

    private fun launchImportPicker() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "application/json"
        }
        importLauncher.launch(intent)
    }

    private fun handleImport(uri: Uri) {
        lifecycleScope.launch {
            val count = viewModel.importAccounts(uri, requireContext().contentResolver)
            if (count >= 0) {
                requireContext().showToast(getString(R.string.import_success, count))
            } else {
                requireContext().showToast(getString(R.string.import_failed))
            }
        }
    }

    // ── Help ───────────────────────────────────────────────────────────────────

    private fun setupHelpSection() {
        binding.itemHelp.setOnClickListener { showHelpDialog() }
        binding.itemAbout.setOnClickListener { showAboutDialog() }
    }

    private fun showHelpDialog() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(getString(R.string.settings_help))
            .setMessage(getString(R.string.help_message))
            .setPositiveButton(getString(R.string.ok), null)
            .show()
    }

    private fun showAboutDialog() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(getString(R.string.settings_about))
            .setMessage(getString(R.string.about_message))
            .setPositiveButton(getString(R.string.ok), null)
            .show()
    }

    // ── Observers ──────────────────────────────────────────────────────────────

    private fun observeViewModel() {
        // Theme label update
        viewModel.themeLabel.observe(viewLifecycleOwner) { label ->
            binding.itemThemeSubtitle.text = label
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}