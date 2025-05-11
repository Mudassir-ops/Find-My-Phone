package com.example.findmyphone.presentation.fragments.settings

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import com.example.findmyphone.R
import com.example.findmyphone.databinding.FragmentSettingsFindMyPhoneBinding
import com.example.findmyphone.utils.dialogs.RateUsDialog
import com.example.findmyphone.utils.showRateDialog
import com.example.findmyphone.utils.viewBinding

class SettingsFindMyPhoneFragment : Fragment(R.layout.fragment_settings_find_my_phone) {
    private val binding by viewBinding(FragmentSettingsFindMyPhoneBinding::bind)
    private val viewModel by viewModels<SettingsFindMyPhoneViewModel>()
    var rateUsDialog: RateUsDialog? = null

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

        }
    }
}