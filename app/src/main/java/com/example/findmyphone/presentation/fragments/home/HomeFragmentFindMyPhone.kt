package com.example.findmyphone.presentation.fragments.home

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.addCallback
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import com.airbnb.lottie.LottieDrawable
import com.example.findmyphone.R
import com.example.findmyphone.data.core.DetectionServiceForeground
import com.example.findmyphone.databinding.FragmentHomeFindMyPhoneBinding
import com.example.findmyphone.presentation.viewmodels.HomeViewModel
import com.example.findmyphone.utils.Logs
import com.example.findmyphone.utils.dialogs.ExitDialog
import com.example.findmyphone.utils.showExitDialog
import com.example.findmyphone.utils.viewBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class HomeFragmentFindMyPhone : Fragment(R.layout.fragment_home_find_my_phone) {
    private val binding by viewBinding(FragmentHomeFindMyPhoneBinding::bind)
    private var myDeviceAppsAdapter: FindMyPhoneRingtoneAdapter? = null
    private val viewModel by activityViewModels<HomeViewModel>()
    var exitDialog: ExitDialog? = null
    private var isServiceEnabled = false
    private val onGoingPagesList: List<RingtoneModels> by lazy {
        listOf(
            RingtoneModels(imageSrc = R.drawable.air_horn, ringtoneTitle = "Air Horn"),
            RingtoneModels(imageSrc = R.drawable.baby, ringtoneTitle = "Baby"),
            RingtoneModels(imageSrc = R.drawable.dog, ringtoneTitle = "Dog"),
            RingtoneModels(imageSrc = R.drawable.doorbel, ringtoneTitle = "Doorbell"),
            RingtoneModels(imageSrc = R.drawable.gun, ringtoneTitle = "Gun"),
            RingtoneModels(imageSrc = R.drawable.horn, ringtoneTitle = "Horn"),
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        myDeviceAppsAdapter = FindMyPhoneRingtoneAdapter(callbackSelection = { ringtone ->

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
                Logs.createLog("isServiceEnabled-->$isServiceEnabled")
                if (isServiceEnabled) {
                    stopService()
                } else {
                    startService()
                }
            }
            headerLayout.ivSettings.setOnClickListener {
                val navController = it.findNavController()
                val currentDestId = navController.currentDestination?.id
                if (currentDestId == R.id.navigation_home_fragment) {
                    navController.navigate(R.id.action_navigation_home_fragment_to_navigation_settings)
                }
            }
        }
        activity?.onBackPressedDispatcher?.addCallback(viewLifecycleOwner) {
            this@HomeFragmentFindMyPhone.showExitDialog(fragmentManager = childFragmentManager)
        }

    }

    private fun startService() {
        try {
            viewModel.isServiceRunning(isServiceRunning = true)
            ContextCompat.startForegroundService(
                context ?: return, Intent(context ?: return, DetectionServiceForeground::class.java)
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun stopService() {
        try {
            viewModel.isServiceRunning(isServiceRunning = false)
            context?.stopService(Intent(context ?: return, DetectionServiceForeground::class.java))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun observeServiceState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.serviceState.flowWithLifecycle(lifecycle).collect { state ->
                binding?.btnActivate?.apply {
                    Logs.createLog("isServiceEnabledObserveServiceState-->$state")
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
}