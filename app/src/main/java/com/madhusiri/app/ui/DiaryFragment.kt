package com.madhusiri.app.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.snackbar.Snackbar
import com.madhusiri.app.R
import com.madhusiri.app.data.local.entity.HiveEntity
import com.madhusiri.app.databinding.DialogAddLogBinding
import com.madhusiri.app.databinding.FragmentDiaryBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class DiaryFragment : Fragment() {

    private var _binding: FragmentDiaryBinding? = null
    private val binding get() = _binding!!

    private val viewModel: DiaryViewModel by viewModels()
    private lateinit var adapter: HiveHealthAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDiaryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        observeViewModel()

        binding.fabAddLog.setOnClickListener {
            showAddLogSheet()
        }

        // Apply scroll awareness to shrink Extended FAB dynamically on scrolling layout frames
        binding.recyclerDiary.setOnScrollChangeListener { _, _, scrollY, _, oldScrollY ->
            if (scrollY > oldScrollY && binding.fabAddLog.isExtended) {
                binding.fabAddLog.shrink()
            } else if (scrollY < oldScrollY && !binding.fabAddLog.isExtended) {
                binding.fabAddLog.extend()
            }
        }
    }

    private fun setupRecyclerView() {
        adapter = HiveHealthAdapter()
        binding.recyclerDiary.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = this@DiaryFragment.adapter
        }
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.allLogs.collect { logs ->
                    adapter.submitList(logs) {
                        // Keep layout anchor up to top when modifying sequence models
                        binding.recyclerDiary.scrollToPosition(0)
                    }

                    // Dynamically toggle local empty state views when array length evaluations return blank
                    if (logs.isEmpty()) {
                        binding.layoutEmptyState.visibility = View.VISIBLE
                        binding.recyclerDiary.visibility = View.GONE
                    } else {
                        binding.layoutEmptyState.visibility = View.GONE
                        binding.recyclerDiary.visibility = View.VISIBLE
                    }
                }
            }
        }
    }

    private fun showAddLogSheet() {
        val sheetDialog = BottomSheetDialog(requireContext(), R.style.Theme_MadhuSiri)
        val sheetBinding = DialogAddLogBinding.inflate(layoutInflater)
        sheetDialog.setContentView(sheetBinding.root)

        // Setup Slider updates rendering context state score metrics smoothly
        sheetBinding.sliderHealth.addOnChangeListener { _, value, _ ->
            val scoreInt = value.toInt()
            sheetBinding.tvSliderScoreLabel.text = "$scoreInt/10"
            
            val colorRes = when {
                scoreInt >= 8 -> R.color.status_active
                scoreInt >= 5 -> R.color.status_warning
                else -> R.color.danger_red
            }
            sheetBinding.tvSliderScoreLabel.setTextColor(requireContext().getColor(colorRes))
        }

        sheetBinding.btnSaveLog.setOnClickListener {
            val honeyStr = sheetBinding.inputHoney.text.toString()
            val honey = honeyStr.toDoubleOrNull() ?: 0.0
            val score = sheetBinding.sliderHealth.value

            val notes = sheetBinding.inputNotes.text.toString()

            val newLog = HiveEntity(
                name = "My Hive",
                latitude = 0.0,
                longitude = 0.0,
                lastInspectionDate = System.currentTimeMillis(),
                honeyProductionKg = honey,
                healthScore = score,
                notes = notes
            )

            viewModel.insertLog(newLog)
            sheetDialog.dismiss()
            Snackbar.make(binding.root, getString(R.string.log_saved_offline), Snackbar.LENGTH_SHORT).show()
        }

        sheetDialog.show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
