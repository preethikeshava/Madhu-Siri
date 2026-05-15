package com.madhusiri.app.ui.assistant

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.madhusiri.app.databinding.FragmentAssistantBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.UUID

@AndroidEntryPoint
class AssistantFragment : Fragment() {

    private var _binding: FragmentAssistantBinding? = null
    private val binding get() = _binding!!

    private lateinit var chatAdapter: ChatAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAssistantBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupChatRecyclerView()
        setupClickListeners()
    }

    private fun setupChatRecyclerView() {
        chatAdapter = ChatAdapter()
        binding.rvChat.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = chatAdapter
        }
    }

    private fun setupClickListeners() {
        binding.toolbar.setNavigationOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }

        binding.btnSend.setOnClickListener {
            val messageText = binding.etMessage.text.toString().trim()
            if (messageText.isNotEmpty()) {
                addMessage(ChatMessage(text = messageText, isUser = true))
                binding.etMessage.text?.clear()
                simulateAiResponse(messageText)
            }
        }
    }

    private fun addMessage(message: ChatMessage) {
        val currentList = chatAdapter.currentList.toMutableList()
        currentList.add(message)
        chatAdapter.submitList(currentList) {
            binding.rvChat.scrollToPosition(chatAdapter.itemCount - 1)
        }
    }

    private fun simulateAiResponse(userMessage: String) {
        CoroutineScope(Dispatchers.Main).launch {
            delay(1000) // Simulate network delay
            val aiResponseText = when {
                userMessage.contains("hello", ignoreCase = true) -> "Hello there! How can I assist you with your bee farming today?"
                userMessage.contains("spraying times", ignoreCase = true) -> "I can help you check optimal spraying times based on weather data and nearby hive locations. What\'s your location?"
                userMessage.contains("hive health", ignoreCase = true) -> "To give you insights on hive health, I need access to your recent hive health logs. Do you want to check them?"
                else -> "I\'m still learning, but I can help you with questions about bee farming, hive management, and spraying alerts."
            }
            addMessage(ChatMessage(text = aiResponseText, isUser = false))
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
