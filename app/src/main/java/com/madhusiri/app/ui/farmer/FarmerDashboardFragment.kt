package com.madhusiri.app.ui.farmer

import android.animation.ValueAnimator
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.android.material.snackbar.Snackbar
import com.madhusiri.app.R
import com.madhusiri.app.databinding.FragmentFarmerDashboardBinding
import com.madhusiri.app.services.AlertService
import com.madhusiri.app.ui.DashboardViewModel
import com.madhusiri.app.ui.OnboardingActivity
import com.madhusiri.app.ui.base.BaseFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class FarmerDashboardFragment : BaseFragment<FragmentFarmerDashboardBinding>(
    FragmentFarmerDashboardBinding::inflate
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

        binding.switchBroadcastAlert.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                triggerSprayingAlert()
                Snackbar.make(binding.root, getString(R.string.dashboard_alert_broadcasted), Snackbar.LENGTH_LONG)
                    .setAction("UNDO") {
                        binding.switchBroadcastAlert.isChecked = false
                    }.show()
                // Animate statistics counter preview update
                animateCounter(binding.tvBeekeepersNotified, 3, 5)
            } else {
                Snackbar.make(binding.root, getString(R.string.dashboard_alert_deactivated), Snackbar.LENGTH_SHORT).show()
                animateCounter(binding.tvBeekeepersNotified, 5, 3)
            }
        }

        // Initialize animated entries
        animateCounter(binding.tvBeekeepersNotified, 0, 3)
        animateCounter(binding.tvBeekeepersUrgent, 0, 1)

        observeWeather()
    }

    private fun animateCounter(textView: TextView, start: Int, end: Int) {
        ValueAnimator.ofInt(start, end).apply {
            duration = 800
            interpolator = android.view.animation.DecelerateInterpolator()
            addUpdateListener { animator ->
                textView.text = animator.animatedValue.toString()
            }
            start()
        }
    }

    private fun observeWeather() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.weatherState.collect { weather ->
                    if (weather != null && weather.isHighWindRisk) {
                        // Custom fade-in + slide reveal instead of blunt layout pop
                        if (binding.cardWeatherWarning.visibility != View.VISIBLE) {
                            binding.cardWeatherWarning.alpha = 0f
                            binding.cardWeatherWarning.visibility = View.VISIBLE
                            binding.cardWeatherWarning.translationY = -20f
                            binding.cardWeatherWarning.animate()
                                .alpha(1f)
                                .translationY(0f)
                                .setDuration(300)
                                .start()
                        }
                        binding.tvWeatherDesc.text = "Wind speed is above 15km/h (${weather.windSpeedKmh}km/h). Spray drift risk is high."
                    } else {
                        binding.cardWeatherWarning.visibility = View.GONE
                    }
                }
            }
        }
    }

    private fun triggerSprayingAlert() {
        val intent = Intent(requireContext(), AlertService::class.java)
        requireContext().startService(intent)
    }
}
