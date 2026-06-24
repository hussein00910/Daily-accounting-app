package com.whatshub.ui.newchat

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.whatshub.R
import com.whatshub.databinding.FragmentNewChatBinding

class NewChatFragment : Fragment() {

    private var _binding: FragmentNewChatBinding? = null
    private val binding get() = _binding!!

    private val viewModel: NewChatViewModel by viewModels()

    private lateinit var adapter: ProfileResultAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentNewChatBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.toolbar.setNavigationOnClickListener {
            findNavController().popBackStack()
        }

        adapter = ProfileResultAdapter { profile -> viewModel.selectProfile(profile) }
        binding.resultsRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.resultsRecyclerView.adapter = adapter

        binding.searchInput.addTextChangedListener {
            viewModel.setQuery(it?.toString().orEmpty())
        }

        viewModel.results.observe(viewLifecycleOwner) { results ->
            adapter.submitList(results)
            val isEmpty = results.isEmpty()
            binding.emptyState.visibility = if (isEmpty) View.VISIBLE else View.GONE
            binding.resultsRecyclerView.visibility = if (isEmpty) View.GONE else View.VISIBLE
        }

        viewModel.navigateToChat.observe(viewLifecycleOwner) { result ->
            result.onSuccess { (chatId, profile) ->
                findNavController().navigate(
                    R.id.action_newChatFragment_to_chatFragment,
                    bundleOf(
                        "chatId" to chatId,
                        "otherUserName" to (profile.displayName ?: profile.phone ?: profile.username),
                        "otherUserAvatarUrl" to profile.avatarUrl
                    )
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
