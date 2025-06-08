package com.example.findmyphone.presentation.fragments.settings

import android.os.Bundle
import android.util.Log
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
import com.example.findmyphone.utils.showTimePicker
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
                viewStartPicker.setOnClickListener {
                    context?.showTimePicker(
                        hour = 10,
                        minute = 30,
                        is24HourView = false
                    ) { selectedHour, selectedMinute ->
                        tvStartPicker.text =
                            getString(R.string.start_time, selectedHour, selectedMinute)
                        sessionManager.setStartTime(startTime = "$selectedHour:$selectedMinute")
                        Log.d("TimePicker", "Selected time: $selectedHour:$selectedMinute")
                    }
                }
                viewEndPicker.setOnClickListener {
                    context?.showTimePicker(
                        hour = 10,
                        minute = 30,
                        is24HourView = false
                    ) { selectedHour, selectedMinute ->
                        tvEndPicker.text =
                            getString(R.string.end_time, selectedHour, selectedMinute)
                        sessionManager.setEndTime(endTime = "$selectedHour:$selectedMinute")
                        Log.d("TimePicker", "Selected time: $selectedHour:$selectedMinute")
                    }
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
            switchScheduleDeactivation.setOnClickListener {
                val isOn = switchScheduleDeactivation.isChecked
                if (isOn) {
                    sessionManager.setDeactivationMode(true)
                } else {
                    sessionManager.setDeactivationMode(false)
                }
            }

            seekBarFlash.max = 3
            seekBarFlash.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(
                    seekBar: SeekBar?, progress: Int, fromUser: Boolean
                ) {
                    val flashlightDuration = when (progress) {
                        0 -> 400L   // Very short
                        1 -> 800L   // Short
                        2 -> 1000L  // Long
                        3 -> 1200L  // Very long
                        else -> 800L
                    }
                    sessionManager.setFlashlightThreshold(flashlightDuration)
                }

                override fun onStartTrackingTouch(seekBar: SeekBar?) {}
                override fun onStopTrackingTouch(seekBar: SeekBar?) {}
            })


            seekBarVolume.max = 2
            seekBarVolume.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(
                    seekBar: SeekBar?, progress: Int, fromUser: Boolean
                ) {
                    val soundSensitivity = when (progress) {
                        0 -> 0   // Low
                        1 -> 1   // Medium
                        2 -> 2  // High
                        else -> 1
                    }
                    sessionManager.setSoundSensitivityLevel(soundSensitivity)
                }

                override fun onStartTrackingTouch(seekBar: SeekBar?) {}
                override fun onStopTrackingTouch(seekBar: SeekBar?) {}
            })
        }
    }

    private fun onResumeDefaultValues() {
        binding?.apply {
            switchFlashlight.isChecked = sessionManager.isFlashlightOn() == true
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
            seekBarVolume.progress = sessionManager.getSoundSensitivityLevel() ?: 0
            switchScheduleDeactivation.isChecked = sessionManager.getDeactivationMode() == true
            tvStartPicker.text=sessionManager.getStartTime()
            tvEndPicker.text=sessionManager.getEndTime()
        }
    }

    override fun onResume() {
        super.onResume()
        onResumeDefaultValues()
    }
}