package com.example.findmyphone.presentation.fragments.activatioDeactivationScreen

import android.os.Bundle
import android.view.View
import android.widget.SeekBar
import androidx.activity.addCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import com.airbnb.lottie.LottieDrawable
import com.example.findmyphone.R
import com.example.findmyphone.databinding.FragmentActivationDeactivationBinding
import com.example.findmyphone.presentation.viewmodels.HomeViewModel
import com.example.findmyphone.utils.viewBinding
import kotlinx.coroutines.launch

class ActivationDeactivationFragment : Fragment(R.layout.fragment_activation_deactivation) {
    private val binding by viewBinding(FragmentActivationDeactivationBinding::bind)
    private val viewModel by activityViewModels<HomeViewModel>()
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        observeServiceState()
        clickListeners()
    }

    private fun observeServiceState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.serviceState.flowWithLifecycle(lifecycle).collect { state ->
                binding?.btnActivate?.apply {
                    if (!state) {
                        setAnimation(R.raw.deactivate_aniamtion)
                        binding?.tvTapToActivate?.text =
                            context.getString(R.string.feature_is_deactivated)
                    } else {
                        setAnimation(R.raw.activate_animation)
                        binding?.tvTapToActivate?.text =
                            resources.getString(R.string.feature_is_activated)
                    }
                    repeatCount = LottieDrawable.INFINITE
                    playAnimation()
                }
            }
        }
    }

    private fun clickListeners() {
        binding?.apply {
            tvBackToHomePage.setOnClickListener {
                findNavController().navigateUp()
            }
        }
    }
}