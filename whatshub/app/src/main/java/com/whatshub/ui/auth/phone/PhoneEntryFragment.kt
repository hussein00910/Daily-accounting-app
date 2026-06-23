package com.whatshub.ui.auth.phone

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import com.whatshub.R
import com.whatshub.databinding.FragmentPhoneEntryBinding

class PhoneEntryFragment : Fragment() {

    private var _binding: FragmentPhoneEntryBinding? = null
    private val binding get() = _binding!!

    private val viewModel: PhoneEntryViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPhoneEntryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.continueButton.setOnClickListener {
            val countryCode = binding.countryCodeInput.text?.toString()?.trim().orEmpty()
            val number = binding.phoneNumberInput.text?.toString()?.trim().orEmpty()
            if (number.isEmpty()) {
                binding.phoneNumberLayout.error = getString(R.string.error_invalid_phone)
                return@setOnClickListener
            }
            binding.phoneNumberLayout.error = null
            viewModel.requestOtp(countryCode + number)
        }

        viewModel.loading.observe(viewLifecycleOwner) { loading ->
            binding.progress.visibility = if (loading) View.VISIBLE else View.GONE
            binding.continueButton.isEnabled = !loading
        }

        viewModel.otpSent.observe(viewLifecycleOwner) { result ->
            result.onSuccess { phoneNumber ->
                findNavController().navigate(
                    R.id.otpVerifyFragment,
                    bundleOf("phoneNumber" to phoneNumber)
                )
            }.onFailure {
                Snackbar.make(binding.root, R.string.error_generic, Snackbar.LENGTH_LONG).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
