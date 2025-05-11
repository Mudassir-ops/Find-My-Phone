package com.example.findmyphone.presentation.fragments.settings

import android.os.Bundle
import android.view.View
import android.widget.SeekBar
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import com.example.findmyphone.R
import com.example.findmyphone.databinding.FragmentSettingsFindMyPhoneBinding
import com.example.findmyphone.utils.SessionManager
import com.example.findmyphone.utils.dialogs.RateUsDialog
import com.example.findmyphone.utils.showRateDialog
import com.example.findmyphone.utils.viewBinding
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class SettingsFindMyPhoneFragment : Fragment(R.layout.fragment_settings_find_my_phone) {
    private val binding by viewBinding(FragmentSettingsFindMyPhoneBinding::bind)
    private val viewModel by viewModels<SettingsFindMyPhoneViewModel>()
    var rateUsDialog: RateUsDialog? = null

    @Inject
    lateinit var sessionManager: SessionManager

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        clickListeners()
    }

    private fun clickListeners() {
        binding?.apply {
            headerLayout.apply {
                ivSettings.visibility = View.GONE
                btnBack.visibility = View.VISIBLE
                tvHeaderTitle.text = getString(R.string.setting)
                btnBack.setOnClickListener {
                    findNavController().navigateUp()
                }
            }
            layoutRateApp.setOnClickListener {
                this@SettingsFindMyPhoneFragment.showRateDialog(fragmentManager = childFragmentManager)
            }

            layoutHowToUse.setOnClickListener {
                val navController = it.findNavController()
                val currentDestId = navController.currentDestination?.id
                if (currentDestId == R.id.navigation_settings) {
                    navController.navigate(R.id.action_navigation_settings_to_navigation_how_to_use)
                }
            }
            switchFlashlight.setOnClickListener {
                val isOn = switchFlashlight.isChecked
                if (isOn) {
                    sessionManager.setFlashlightState(true)
                } else {
                    sessionManager.setFlashlightState(false)
                }
            }
            seekBarFlash.max = 3
            seekBarFlash.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(
                    seekBar: SeekBar?,
                    progress: Int,
                    fromUser: Boolean
                ) {
                    val flashlightDuration = when (progress) {
                        0 -> 400L   // Very short
                        1 -> 800L   // Short
                        2 -> 1000L  // Long
                        3 -> 1200L  // Very long
                        else -> 800L
                    }
                    sessionManager.setFlashlightThreshold(flashlightDuration)
                    sessionManager.setVolumeLevel(progress)
                }

                override fun onStartTrackingTouch(seekBar: SeekBar?) {}
                override fun onStopTrackingTouch(seekBar: SeekBar?) {}
            })
        }
    }

    private fun onResumeDefaultValues() {
        binding?.apply {
            seekBarFlash.max = 3
            val threshold = sessionManager.getFlashlightThreshold()
            val progress = when (threshold) {
                400L -> 0
                800L -> 1
                1000L -> 2
                1200L -> 3
                else -> 1
            }
            seekBarFlash.progress = progress

        }
    }

    override fun onResume() {
        super.onResume()
        onResumeDefaultValues()
    }
}