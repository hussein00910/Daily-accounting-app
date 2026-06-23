package com.whatshub.ui.splash

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.navOptions
import com.whatshub.R
import com.whatshub.databinding.FragmentSplashBinding

class SplashFragment : Fragment() {

    private var _binding: FragmentSplashBinding? = null
    private val binding get() = _binding!!

    private val viewModel: SplashViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSplashBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.destination.observe(viewLifecycleOwner) { destination ->
            val targetId = when (destination) {
                SplashDestination.PhoneEntry -> R.id.phoneEntryFragment
                SplashDestination.ProfileSetup -> R.id.profileSetupFragment
                SplashDestination.ChatList -> R.id.chatListFragment
            }
            findNavController().navigate(
                targetId,
                null,
                navOptions { popUpTo(R.id.splashFragment) { inclusive = true } }
            )
        }

        viewModel.resolveDestination()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
