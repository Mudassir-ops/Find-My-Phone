package com.example.findmyphone.presentation.fragments.home

import android.app.Activity
import android.app.AppOpsManager
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.View
import android.widget.SeekBar
import androidx.activity.addCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import com.airbnb.lottie.LottieDrawable
import com.example.findmyphone.R
import com.example.findmyphone.data.core.DetectionServiceForeground
import com.example.findmyphone.data.other.DetectionRepository
import com.example.findmyphone.databinding.FragmentHomeFindMyPhoneBinding
import com.example.findmyphone.presentation.viewmodels.HomeViewModel
import com.example.findmyphone.utils.Logs
import com.example.findmyphone.utils.MediaPlayerManager
import com.example.findmyphone.utils.SessionManager
import com.example.findmyphone.utils.dialogs.ExitDialog
import com.example.findmyphone.utils.showExitDialog
import com.example.findmyphone.utils.showOverlay
import com.example.findmyphone.utils.viewBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class HomeFragmentFindMyPhone : Fragment(R.layout.fragment_home_find_my_phone) {
    private val binding by viewBinding(FragmentHomeFindMyPhoneBinding::bind)
    private var myDeviceAppsAdapter: FindMyPhoneRingtoneAdapter? = null
    private val viewModel by activityViewModels<HomeViewModel>()
    var exitDialog: ExitDialog? = null
    private var isServiceEnabled = false
    private var mediaPlayerManager: MediaPlayerManager? = null

    @Inject
    lateinit var sessionManager: SessionManager

    private val onGoingPagesList: List<RingtoneModels> by lazy {
        listOf(
            RingtoneModels(
                imageSrc = R.drawable.air_horn,
                ringtoneTitle = "Air Horn",
                ringtoneRes = R.raw.air_horntone
            ),
            RingtoneModels(
                imageSrc = R.drawable.baby,
                ringtoneTitle = "Baby",
                ringtoneRes = R.raw.laughing_baby_remix
            ),
            RingtoneModels(
                imageSrc = R.drawable.dog,
                ringtoneTitle = "Dog",
                ringtoneRes = R.raw.dogsbarking
            ),
            RingtoneModels(
                imageSrc = R.drawable.doorbel,
                ringtoneTitle = "Doorbell",
                ringtoneRes = R.raw.door_bell
            ),
            RingtoneModels(
                imageSrc = R.drawable.gun,
                ringtoneTitle = "Gun",
                ringtoneRes = R.raw.gun
            ),
            RingtoneModels(
                imageSrc = R.drawable.horn,
                ringtoneTitle = "Horn",
                ringtoneRes = R.raw.horn
            ),
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mediaPlayerManager = MediaPlayerManager(context ?: return)
        myDeviceAppsAdapter = FindMyPhoneRingtoneAdapter(callbackSelection = { ringtone ->
            mediaPlayerManager?.play(ringtone.ringtoneRes)
            sessionManager.setRingtone(ringtone.ringtoneRes)
        })
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        clickListeners()
        setupRingtoneRecyclerView()
        observeServiceState()
    }

    private fun setupRingtoneRecyclerView() {
        binding?.rvRingtones?.run {
            adapter = myDeviceAppsAdapter
            hasFixedSize()
        }
        myDeviceAppsAdapter?.submitList(onGoingPagesList)
    }

    private fun clickListeners() {
        binding?.apply {
            headerLayout.btnBack.visibility = View.GONE
            headerLayout.ivSettings.visibility = View.VISIBLE
            tvViewMore.setOnClickListener {
                val navController = it.findNavController()
                val currentDestId = navController.currentDestination?.id
                if (currentDestId == R.id.navigation_home_fragment) {
                    navController.navigate(R.id.action_navigation_home_fragment_to_navigation_home_ringtone)
                }
            }
            btnActivate.setOnClickListener {
                if (isServiceEnabled) {
                    val result = stopService()
                    if (result) {
                        navigateToActivationDeactivationScreen()
                    }
                } else {
                    val result = startService()
                    if (result) {
                        navigateToActivationDeactivationScreen()
                    }
                }
            }
            headerLayout.ivSettings.setOnClickListener {
                val navController = it.findNavController()
                val currentDestId = navController.currentDestination?.id
                if (currentDestId == R.id.navigation_home_fragment) {
                    navController.navigate(R.id.action_navigation_home_fragment_to_navigation_settings)
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
            seekBarVolume.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(
                    seekBar: SeekBar?,
                    progress: Int,
                    fromUser: Boolean
                ) {
                    tvVolumePercent.text = getString(R.string.volume_level, "$progress")
                    sessionManager.setVolumeLevel(progress)
                }

                override fun onStartTrackingTouch(seekBar: SeekBar?) {}
                override fun onStopTrackingTouch(seekBar: SeekBar?) {}
            })

        }
        activity?.onBackPressedDispatcher?.addCallback(viewLifecycleOwner) {
            this@HomeFragmentFindMyPhone.showExitDialog(fragmentManager = childFragmentManager)
        }

    }

    private fun startService(): Boolean {
        return try {
            viewModel.isServiceRunning(isServiceRunning = true)
            val ctx = context ?: return false
            val serviceIntent = Intent(ctx, DetectionServiceForeground::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.VANILLA_ICE_CREAM) {
                showOverlay(ctx) {
                    ContextCompat.startForegroundService(ctx, serviceIntent)
                }
            } else {
                ContextCompat.startForegroundService(ctx, serviceIntent)
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }


    private fun stopService(): Boolean {
        return try {
            val navController = findNavController()
            val currentDestId = navController.currentDestination?.id
            if (currentDestId == R.id.navigation_home_fragment) {
                navController.navigate(R.id.action_navigation_home_fragment_to_navigation_activation_deactivation_screen)
            }
            viewModel.isServiceRunning(isServiceRunning = false)
            context?.stopService(
                Intent(
                    context ?: return false,
                    DetectionServiceForeground::class.java
                )
            )
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    private fun observeServiceState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.serviceState.flowWithLifecycle(lifecycle).collect { state ->
                binding?.btnActivate?.apply {
                    isServiceEnabled = state
                    if (state) {
                        setAnimation(R.raw.deactivate_aniamtion)
                        binding?.tvTapToActivate?.text =
                            resources.getString(R.string.tap_to_deactivate)
                    } else {
                        setAnimation(R.raw.activate_animation)
                        binding?.tvTapToActivate?.text =
                            resources.getString(R.string.tap_to_activate)
                    }
                    repeatCount = LottieDrawable.INFINITE
                    playAnimation()
                }
            }
        }
    }

    private fun onResumeDefaultValues() {
        binding?.apply {
            switchFlashlight.isChecked = sessionManager.isFlashlightOn() == true
            tvVolumePercent.text =
                getString(R.string.volume_level, "${sessionManager.getVolumeLevel()}")
            seekBarVolume.progress = sessionManager.getVolumeLevel() ?: 0
        }
    }

    override fun onResume() {
        super.onResume()
        onResumeDefaultValues()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mediaPlayerManager?.stop()
    }

    private fun requestUsageStatsPermission() {
        if (!isUsageStatsPermissionGranted()) {
            val intent = Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)
            requestUsageStatsPermission.launch(intent)
        } else {
            startService()
        }
    }

    private fun isUsageStatsPermissionGranted(): Boolean {
        val appOps = context?.getSystemService(AppOpsManager::class.java)
        val mode = context?.packageName?.let {
            appOps?.checkOpNoThrow(
                AppOpsManager.OPSTR_GET_USAGE_STATS,
                android.os.Process.myUid(),
                it
            )
        }
        return mode == AppOpsManager.MODE_ALLOWED
    }

    private val requestUsageStatsPermission = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val resultService = startService()
            if (resultService) {
                navigateToActivationDeactivationScreen()
            }
            Log.d("UsageStats", "Permission granted")
        } else {
            // Permission not granted, inform the user
            Log.d("UsageStats", "Permission not granted")
        }
    }

    private fun navigateToActivationDeactivationScreen() {
        val navController = findNavController()
        val currentDestId = navController.currentDestination?.id
        if (currentDestId == R.id.navigation_home_fragment) {
            navController.navigate(R.id.action_navigation_home_fragment_to_navigation_activation_deactivation_screen)
        }
    }

}