package com.whatshub.ui.chatlist

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
import com.whatshub.R
import com.whatshub.databinding.FragmentChatListBinding

class ChatListFragment : Fragment() {
    private var _binding: FragmentChatListBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ChatListViewModel by viewModels()

    private lateinit var adapter: ChatListAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentChatListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = ChatListAdapter { item ->
            findNavController().navigate(
                R.id.action_chatListFragment_to_chatFragment,
                bundleOf(
                    "chatId" to item.chatId,
                    "otherUserName" to item.displayName,
                    "otherUserAvatarUrl" to item.avatarUrl
                )
            )
        }
        binding.chatRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.chatRecyclerView.adapter = adapter

        viewModel.visibleChats.observe(viewLifecycleOwner) { chats ->
            adapter.submitList(chats)
            val isEmpty = chats.isEmpty()
            binding.emptyState.visibility = if (isEmpty) View.VISIBLE else View.GONE
            binding.chatRecyclerView.visibility = if (isEmpty) View.GONE else View.VISIBLE
        }

        binding.filterChips.setOnCheckedStateChangeListener { _, checkedIds ->
            val filter = when (checkedIds.firstOrNull()) {
                R.id.chipUnread -> ChatFilter.UNREAD
                R.id.chipFavorites -> ChatFilter.FAVORITES
                R.id.chipGroups -> ChatFilter.GROUPS
                else -> ChatFilter.ALL
            }
            viewModel.setFilter(filter)
        }

        binding.searchInput.addTextChangedListener {
            viewModel.setSearchQuery(it?.toString().orEmpty())
        }

        binding.newChatFab.setOnClickListener {
            findNavController().navigate(R.id.action_chatListFragment_to_newChatFragment)
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.loadChats()
        viewModel.startRealtimeUpdates()
    }

    override fun onPause() {
        super.onPause()
        viewModel.stopRealtimeUpdates()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
