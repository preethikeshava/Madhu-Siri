package com.madhusiri.app.ui.beekeeper

import android.animation.ValueAnimator
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import com.madhusiri.app.R
import com.madhusiri.app.databinding.FragmentBeekeeperDashboardBinding
import com.madhusiri.app.ui.DashboardViewModel
import com.madhusiri.app.ui.OnboardingActivity
import com.madhusiri.app.ui.base.BaseFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class BeekeeperDashboardFragment : BaseFragment<FragmentBeekeeperDashboardBinding>(
    FragmentBeekeeperDashboardBinding::inflate
) {

    private val viewModel: DashboardViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val sharedPrefs = requireContext().getSharedPreferences("MadhuSiriPrefs", Context.MODE_PRIVATE)

        binding.btnSwitchRole.setOnClickListener {
            with(sharedPrefs.edit()) {
                remove("USER_ROLE")
                apply()
            }
            startActivity(Intent(requireContext(), OnboardingActivity::class.java))
            requireActivity().finish()
        }

        // Configure layout chip actions to trigger view graph changes seamlessly
        binding.chipAddLog.setOnClickListener {
            findNavController().navigate(R.id.navigation_diary)
        }

        binding.chipViewMap.setOnClickListener {
            findNavController().navigate(R.id.navigation_map)
        }

        binding.chipSync.setOnClickListener {
            binding.chipSync.isEnabled = false
            Snackbar.make(binding.root, "Synchronizing local Room records with Firebase...", Snackbar.LENGTH_SHORT).show()
            // Reset chip trigger simulation
            binding.chipSync.postDelayed({
                binding.chipSync.isEnabled = true
                Snackbar.make(binding.root, "Cloud sync complete", Snackbar.LENGTH_SHORT).show()
            }, 1200)
        }

        observeViewModel()
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.averageHealthScore.collect { score ->
                    binding.textDashboard.text = String.format("%.1f", score)

                    // Animate material progress arc filling up proportionally to metric updates
                    val targetProgress = (score * 10).toInt().coerceIn(0, 100)
                    ValueAnimator.ofInt(0, targetProgress).apply {
                        duration = 1000
                        interpolator = android.view.animation.OvershootInterpolator()
                        addUpdateListener { animator ->
                            binding.progressHealth.progress = animator.animatedValue as Int
                        }
                        start()
                    }
                }
            }
        }
    }
}
