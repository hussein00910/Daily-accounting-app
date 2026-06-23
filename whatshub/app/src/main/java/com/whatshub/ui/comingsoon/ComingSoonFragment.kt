package com.whatshub.ui.comingsoon

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.whatshub.R
import com.whatshub.databinding.FragmentComingSoonBinding

class ComingSoonFragment : Fragment() {

    private var _binding: FragmentComingSoonBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentComingSoonBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val titleRes = arguments?.getInt("titleRes", 0) ?: 0
        if (titleRes != 0) {
            binding.title.text = getString(titleRes)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
