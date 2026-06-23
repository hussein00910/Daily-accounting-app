package com.whatshub.ui.auth.otp

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.navOptions
import com.google.android.material.snackbar.Snackbar
import com.whatshub.R
import com.whatshub.databinding.FragmentOtpVerifyBinding

class OtpVerifyFragment : Fragment() {

    private var _binding: FragmentOtpVerifyBinding? = null
    private val binding get() = _binding!!

    private val viewModel: OtpVerifyViewModel by viewModels()

    private val phoneNumber: String by lazy {
        requireArguments().getString("phoneNumber").orEmpty()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentOtpVerifyBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.subtitle.text = getString(R.string.otp_subtitle, phoneNumber)

        binding.verifyButton.setOnClickListener {
            val code = binding.otpInput.text?.toString()?.trim().orEmpty()
            if (code.length != 6) {
                binding.otpLayout.error = getString(R.string.error_invalid_otp)
                return@setOnClickListener
            }
            binding.otpLayout.error = null
            viewModel.verify(phoneNumber, code)
        }

        binding.resendButton.setOnClickListener {
            viewModel.resend(phoneNumber)
        }

        viewModel.startResendCooldown()

        viewModel.resendSecondsLeft.observe(viewLifecycleOwner) { seconds ->
            if (seconds > 0) {
                binding.resendButton.isEnabled = false
                binding.resendButton.text = getString(R.string.resend_code_in, seconds)
            } else {
                binding.resendButton.isEnabled = true
                binding.resendButton.text = getString(R.string.action_resend_code)
            }
        }

        viewModel.loading.observe(viewLifecycleOwner) { loading ->
            binding.progress.visibility = if (loading) View.VISIBLE else View.GONE
            binding.verifyButton.isEnabled = !loading
        }

        viewModel.result.observe(viewLifecycleOwner) { result ->
            result.onSuccess { profileComplete ->
                val targetId = if (profileComplete) R.id.chatListFragment else R.id.profileSetupFragment
                findNavController().navigate(
                    targetId,
                    null,
                    navOptions { popUpTo(R.id.phoneEntryFragment) { inclusive = true } }
                )
            }.onFailure {
                Snackbar.make(binding.root, R.string.error_otp_failed, Snackbar.LENGTH_LONG).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
