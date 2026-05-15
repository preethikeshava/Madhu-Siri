package com.madhusiri.app.ui

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.android.material.snackbar.Snackbar
import com.madhusiri.app.MainActivity
import com.madhusiri.app.R
import com.madhusiri.app.databinding.ActivityOnboardingBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class OnboardingActivity : AppCompatActivity() {

    private lateinit var binding: ActivityOnboardingBinding

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val locationGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: false
        if (locationGranted) {
            proceedToMain()
        } else {
            Snackbar.make(binding.root, "Location mapping access is required to visualize safety zones", Snackbar.LENGTH_INDEFINITE)
                .setAction("GRANT") { checkPermissionsAndProceed() }
                .show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        // Render system bars natively supporting custom top brand gradient illustrations
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(Color.TRANSPARENT)
        )
        super.onCreate(savedInstanceState)

        val sharedPrefs = getSharedPreferences("MadhuSiriPrefs", Context.MODE_PRIVATE)
        if (sharedPrefs.contains("USER_ROLE")) {
            proceedToMain()
            return
        }

        binding = ActivityOnboardingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Apply smooth bottom-sheet reveal intro animations
        binding.sheetRoleSelection.translationY = 200f
        binding.sheetRoleSelection.alpha = 0f
        binding.sheetRoleSelection.animate()
            .translationY(0f)
            .alpha(1f)
            .setDuration(600)
            .setInterpolator(android.view.animation.DecelerateInterpolator(1.5f))
            .start()

        // Apply top header drop delay animation
        binding.layoutBranding.alpha = 0f
        binding.layoutBranding.animate()
            .alpha(1f)
            .setStartDelay(200)
            .setDuration(500)
            .start()

        binding.cardFarmer.setOnClickListener {
            // Apply quick tactile state visual transformation click simulation
            animateSelectionFeedback(binding.cardFarmer) {
                saveRole("FARMER")
                checkPermissionsAndProceed()
            }
        }

        binding.cardBeekeeper.setOnClickListener {
            animateSelectionFeedback(binding.cardBeekeeper) {
                saveRole("BEEKEEPER")
                checkPermissionsAndProceed()
            }
        }
    }

    private fun animateSelectionFeedback(view: View, onComplete: () -> Unit) {
        view.animate()
            .scaleX(0.96f)
            .scaleY(0.96f)
            .setDuration(80)
            .withEndAction {
                view.animate()
                    .scaleX(1f)
                    .scaleY(1f)
                    .setDuration(80)
                    .withEndAction { onComplete() }
                    .start()
            }.start()
    }

    private fun saveRole(role: String) {
        val sharedPrefs = getSharedPreferences("MadhuSiriPrefs", Context.MODE_PRIVATE)
        sharedPrefs.edit().putString("USER_ROLE", role).apply()
    }

    private fun checkPermissionsAndProceed() {
        val permissions = mutableListOf(Manifest.permission.ACCESS_FINE_LOCATION)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions.add(Manifest.permission.POST_NOTIFICATIONS)
        }

        val allGranted = permissions.all {
            ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
        }

        if (allGranted) {
            proceedToMain()
        } else {
            requestPermissionLauncher.launch(permissions.toTypedArray())
        }
    }

    private fun proceedToMain() {
        startActivity(Intent(this, MainActivity::class.java))
        // Apply tailored window page view slide animations instead of standard OS pop
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
        finish()
    }
}
