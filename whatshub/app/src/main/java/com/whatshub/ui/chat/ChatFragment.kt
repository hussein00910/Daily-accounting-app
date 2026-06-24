package com.whatshub.ui.chat

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import coil.load
import com.google.android.material.snackbar.Snackbar
import com.whatshub.R
import com.whatshub.databinding.FragmentChatBinding

class ChatFragment : Fragment() {

    private var _binding: FragmentChatBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ChatViewModel by viewModels()

    private lateinit var adapter: MessageAdapter

    private val typingHandler = Handler(Looper.getMainLooper())
    private val typingIdleRunnable = Runnable { viewModel.setTyping(false) }

    private var lastFailedBody: String? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentChatBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val chatId = arguments?.getString("chatId").orEmpty()
        val otherUserName = arguments?.getString("otherUserName")
        val otherUserAvatarUrl = arguments?.getString("otherUserAvatarUrl")

        binding.toolbar.setNavigationOnClickListener {
            findNavController().popBackStack()
        }
        binding.nameText.text = otherUserName.orEmpty()
        binding.avatarImage.load(otherUserAvatarUrl) {
            placeholder(R.drawable.ic_default_avatar)
            error(R.drawable.ic_default_avatar)
        }

        adapter = MessageAdapter()
        binding.messageRecyclerView.layoutManager = LinearLayoutManager(requireContext()).apply {
            stackFromEnd = true
        }
        binding.messageRecyclerView.adapter = adapter

        viewModel.messages.observe(viewLifecycleOwner) { messages ->
            adapter.submitList(messages) {
                if (messages.isNotEmpty()) {
                    binding.messageRecyclerView.scrollToPosition(messages.size - 1)
                }
            }
        }

        viewModel.otherTyping.observe(viewLifecycleOwner) { typing ->
            if (typing) {
                binding.subtitleText.visibility = View.VISIBLE
                binding.subtitleText.text = getString(R.string.typing_indicator)
            } else {
                binding.subtitleText.visibility = View.GONE
            }
        }

        viewModel.sendFailed.observe(viewLifecycleOwner) { failedBody ->
            if (failedBody != null) {
                lastFailedBody = failedBody
                Snackbar.make(binding.root, R.string.message_send_failed, Snackbar.LENGTH_LONG)
                    .setAction(R.string.action_retry_send) {
                        viewModel.sendMessage(failedBody)
                    }
                    .show()
                viewModel.consumeSendFailedEvent()
            }
        }

        binding.sendButton.setOnClickListener {
            val text = binding.messageInput.text?.toString()?.trim().orEmpty()
            if (text.isNotEmpty()) {
                viewModel.sendMessage(text)
                binding.messageInput.setText("")
                viewModel.setTyping(false)
                typingHandler.removeCallbacks(typingIdleRunnable)
            }
        }

        binding.messageInput.addTextChangedListener {
            viewModel.setTyping(true)
            typingHandler.removeCallbacks(typingIdleRunnable)
            typingHandler.postDelayed(typingIdleRunnable, 2000)
        }

        viewModel.loadInitial(chatId)
        viewModel.startObserving()
    }

    override fun onDestroyView() {
        typingHandler.removeCallbacks(typingIdleRunnable)
        viewModel.stopObserving()
        super.onDestroyView()
        _binding = null
    }
}
