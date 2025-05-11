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
import com.example.findmyphone.utils.MediaPlayerManager
import com.example.findmyphone.utils.SessionManager
import com.example.findmyphone.utils.viewBinding
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class RingtoneFragment : Fragment(R.layout.fragment_ringtone) {
    private val binding by viewBinding(FragmentRingtoneBinding::bind)
    private var myDeviceAppsAdapter: FindMyPhoneRingtoneAdapter? = null
    private val viewModel by viewModels<HomeViewModel>()
    private var mediaPlayerManager: MediaPlayerManager? = null

    @Inject
    lateinit var sessionManager: SessionManager

    private val onGoingPagesList: List<RingtoneModels> by lazy {
        listOf(
            RingtoneModels(
                imageSrc = R.drawable.air_horn,
                ringtoneTitle = "Air Horn",
                ringtoneRes = R.raw.air_horntone
            ), RingtoneModels(
                imageSrc = R.drawable.baby,
                ringtoneTitle = "Baby",
                ringtoneRes = R.raw.laughing_baby_remix
            ), RingtoneModels(
                imageSrc = R.drawable.dog, ringtoneTitle = "Dog", ringtoneRes = R.raw.dogsbarking
            ), RingtoneModels(
                imageSrc = R.drawable.doorbel,
                ringtoneTitle = "Doorbell",
                ringtoneRes = R.raw.door_bell
            ), RingtoneModels(
                imageSrc = R.drawable.gun, ringtoneTitle = "Gun", ringtoneRes = R.raw.gun
            ), RingtoneModels(
                imageSrc = R.drawable.horn, ringtoneTitle = "Horn", ringtoneRes = R.raw.horn
            ), RingtoneModels(
                imageSrc = R.drawable.laughing,
                ringtoneTitle = "Laughing",
                ringtoneRes = R.raw.joker_laugh
            ), RingtoneModels(
                imageSrc = R.drawable.police, ringtoneTitle = "Police", ringtoneRes = R.raw.police
            ), RingtoneModels(
                imageSrc = R.drawable.rooster,
                ringtoneTitle = "Rooster",
                ringtoneRes = R.raw.alarm_rooster
            ), RingtoneModels(
                imageSrc = R.drawable.ship, ringtoneTitle = "Ship", ringtoneRes = R.raw.ship_horn
            ), RingtoneModels(
                imageSrc = R.drawable.ship, ringtoneTitle = "Ship", ringtoneRes = R.raw.ship_horn
            ), RingtoneModels(
                imageSrc = R.drawable.ship, ringtoneTitle = "Ship", ringtoneRes = R.raw.ship_horn
            ), RingtoneModels(
                imageSrc = R.drawable.train, ringtoneTitle = "Train", ringtoneRes = R.raw.train_horn
            )
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

    override fun onDestroyView() {
        super.onDestroyView()
        mediaPlayerManager?.stop()
    }
}