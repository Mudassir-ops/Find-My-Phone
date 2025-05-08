package com.example.findmyphone.presentation.fragments.home

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import com.example.findmyphone.R
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
            RingtoneModels(imageSrc = R.drawable.air_horn, ringtoneTitle = "Air Horn"),
            RingtoneModels(imageSrc = R.drawable.air_horn, ringtoneTitle = "Air Horn"),
            RingtoneModels(imageSrc = R.drawable.air_horn, ringtoneTitle = "Air Horn"),
            RingtoneModels(imageSrc = R.drawable.air_horn, ringtoneTitle = "Air Horn"),
            RingtoneModels(imageSrc = R.drawable.air_horn, ringtoneTitle = "Air Horn"),
            RingtoneModels(imageSrc = R.drawable.air_horn, ringtoneTitle = "Air Horn"),
            RingtoneModels(imageSrc = R.drawable.air_horn, ringtoneTitle = "Air Horn"),
            RingtoneModels(imageSrc = R.drawable.air_horn, ringtoneTitle = "Air Horn")
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

    private fun clickListeners() {}
}