package com.example.findmyphone.presentation.fragments.ongoingscreens

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.findmyphone.clapping.clapfinder.soundalert.R
import com.findmyphone.clapping.clapfinder.soundalert.databinding.FragmentOnGoingFragmentItemBinding
import com.example.findmyphone.domain.OnGoingScreenModel
import com.example.findmyphone.utils.AppConstants.ON_GOING_DATA_MODEL
import com.example.findmyphone.utils.viewBinding
import dagger.hilt.android.AndroidEntryPoint
import com.example.findmyphone.utils.parcelable

@AndroidEntryPoint
class OnGoingFragmentItem : Fragment(R.layout.fragment_on_going_fragment_item) {

    private val binding by viewBinding(FragmentOnGoingFragmentItemBinding::bind)
    private var onGoingScreenModel: OnGoingScreenModel? = null
    private val viewModel: OnGoingFragmentItemViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            onGoingScreenModel = arguments?.parcelable(ON_GOING_DATA_MODEL)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding?.onGoingModel = onGoingScreenModel
    }

}