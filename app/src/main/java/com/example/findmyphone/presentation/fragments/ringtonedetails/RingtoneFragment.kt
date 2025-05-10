package com.example.findmyphone.presentation.fragments.ringtonedetails

import androidx.fragment.app.viewModels
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.example.findmyphone.R
import com.example.findmyphone.databinding.FragmentHomeFindMyPhoneBinding
import com.example.findmyphone.databinding.FragmentRingtoneBinding
import com.example.findmyphone.presentation.fragments.home.FindMyPhoneRingtoneAdapter
import com.example.findmyphone.presentation.fragments.home.RingtoneModels
import com.example.findmyphone.presentation.viewmodels.HomeViewModel
import com.example.findmyphone.utils.viewBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class RingtoneFragment : Fragment(R.layout.fragment_ringtone) {
    private val binding by viewBinding(FragmentRingtoneBinding::bind)
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
            RingtoneModels(imageSrc = R.drawable.laughing, ringtoneTitle = "Laughing"),
            RingtoneModels(imageSrc = R.drawable.police, ringtoneTitle = "Police"),
            RingtoneModels(imageSrc = R.drawable.rooster, ringtoneTitle = "Rooster"),
            RingtoneModels(imageSrc = R.drawable.ship, ringtoneTitle = "Ship"),
            RingtoneModels(imageSrc = R.drawable.ship, ringtoneTitle = "Ship"),
            RingtoneModels(imageSrc = R.drawable.ship, ringtoneTitle = "Ship"),
            RingtoneModels(imageSrc = R.drawable.train, ringtoneTitle = "Train")
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
        binding?.headerLayout?.apply {
            ivSettings.visibility = View.GONE
            btnBack.setOnClickListener {
                findNavController().navigateUp()
            }
        }
    }
}