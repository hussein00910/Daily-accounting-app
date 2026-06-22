package com.mohaseb.soft.ui.settings

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.google.android.material.snackbar.Snackbar
import com.mohaseb.soft.data.entity.Settings
import com.mohaseb.soft.databinding.FragmentSettingsBinding
import java.io.File

class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!

    private val viewModel: SettingsViewModel by viewModels()
    private var currentSettings: Settings? = null

    private val openDocumentLauncher = registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        uri?.let { restoreFromUri(it) }
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

        viewModel.settings.observe(viewLifecycleOwner) { settings ->
            currentSettings = settings
            binding.etCompanyName.setText(settings.companyName)
            binding.etCompanyAddress.setText(settings.companyAddress)
            binding.etCompanyPhone.setText(settings.companyPhone)
            binding.etTaxNumber.setText(settings.taxNumber)
            binding.etCurrency.setText(settings.currency)
            binding.switchAutoBackup.isChecked = settings.autoBackup
        }

        viewModel.message.observe(viewLifecycleOwner) { resId ->
            Snackbar.make(binding.root, resId, Snackbar.LENGTH_SHORT).show()
        }

        binding.btnSave.setOnClickListener {
            val base = currentSettings ?: Settings()
            viewModel.saveSettings(
                base.copy(
                    companyName = binding.etCompanyName.text.toString(),
                    companyAddress = binding.etCompanyAddress.text.toString(),
                    companyPhone = binding.etCompanyPhone.text.toString(),
                    taxNumber = binding.etTaxNumber.text.toString(),
                    currency = binding.etCurrency.text.toString(),
                    autoBackup = binding.switchAutoBackup.isChecked
                )
            )
        }

        binding.btnBackupNow.setOnClickListener { viewModel.backupNow() }
        binding.btnRestoreBackup.setOnClickListener { openDocumentLauncher.launch(arrayOf("application/json")) }
        binding.btnExportCsv.setOnClickListener { viewModel.exportFullDataset() }
    }

    private fun restoreFromUri(uri: Uri) {
        val tempFile = File(requireContext().cacheDir, "restore_temp.json")
        requireContext().contentResolver.openInputStream(uri)?.use { input ->
            tempFile.outputStream().use { output -> input.copyTo(output) }
        }
        viewModel.restoreBackup(tempFile.absolutePath)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
