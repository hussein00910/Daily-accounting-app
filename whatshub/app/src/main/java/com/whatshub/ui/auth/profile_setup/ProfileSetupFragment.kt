package com.whatshub.ui.auth.profile_setup

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.navOptions
import com.google.android.material.snackbar.Snackbar
import com.whatshub.R
import com.whatshub.databinding.FragmentProfileSetupBinding

class ProfileSetupFragment : Fragment() {

    private var _binding: FragmentProfileSetupBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ProfileSetupViewModel by viewModels()

    private var pickedAvatarUri: Uri? = null

    private val pickImage = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) {
            pickedAvatarUri = uri
            binding.avatarImage.setImageURI(uri)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileSetupBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val pickPhoto = { pickImage.launch("image/*") }
        binding.avatarImage.setOnClickListener { pickPhoto() }
        binding.changePhotoLabel.setOnClickListener { pickPhoto() }

        binding.doneButton.setOnClickListener {
            val name = binding.nameInput.text?.toString()?.trim().orEmpty()
            if (name.isEmpty()) {
                binding.nameLayout.error = getString(R.string.error_invalid_name)
                return@setOnClickListener
            }
            binding.nameLayout.error = null
            val avatarBytes = pickedAvatarUri?.let { uri ->
                requireContext().contentResolver.openInputStream(uri)?.use { it.readBytes() }
            }
            viewModel.saveProfile(name, avatarBytes)
        }

        viewModel.loading.observe(viewLifecycleOwner) { loading ->
            binding.progress.visibility = if (loading) View.VISIBLE else View.GONE
            binding.doneButton.isEnabled = !loading
        }

        viewModel.saved.observe(viewLifecycleOwner) { result ->
            result.onSuccess {
                findNavController().navigate(
                    R.id.chatListFragment,
                    null,
                    navOptions { popUpTo(R.id.profileSetupFragment) { inclusive = true } }
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
