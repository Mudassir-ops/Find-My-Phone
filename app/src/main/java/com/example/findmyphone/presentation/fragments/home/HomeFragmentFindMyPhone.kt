package com.example.findmyphone.presentation.fragments.home

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.findNavController
import com.example.findmyphone.R
import com.example.findmyphone.data.core.DetectionServiceForeground
import com.example.findmyphone.databinding.FragmentHomeFindMyPhoneBinding
import com.example.findmyphone.presentation.viewmodels.HomeViewModel
import com.example.findmyphone.utils.viewBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class HomeFragmentFindMyPhone : Fragment(R.layout.fragment_home_find_my_phone) {
    private val binding by viewBinding(FragmentHomeFindMyPhoneBinding::bind)
    private var myDeviceAppsAdapter: FindMyPhoneRingtoneAdapter? = null
    private val viewModel by viewModels<HomeViewModel>()
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
                startService()
            }
            headerLayout.ivSettings.setOnClickListener {
                val navController = it.findNavController()
                val currentDestId = navController.currentDestination?.id
                if (currentDestId == R.id.navigation_home_fragment) {
                    navController.navigate(R.id.action_navigation_home_fragment_to_navigation_settings)
                }
            }
        }
    }

    private fun startService() {
        try {
            ContextCompat.startForegroundService(
                context ?: return,
                Intent(context ?: return, DetectionServiceForeground::class.java)
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}