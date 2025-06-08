package com.example.findmyphone.presentation.fragments.ongoingscreens

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.navigation.fragment.findNavController
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.findmyphone.R
import com.example.findmyphone.databinding.FragmentOnGoingParentBinding
import com.example.findmyphone.domain.OnGoingScreenModel
import com.example.findmyphone.utils.AppConstants.ON_GOING_DATA_MODEL
import com.example.findmyphone.utils.SessionManager
import com.example.findmyphone.utils.viewBinding
import dagger.hilt.android.AndroidEntryPoint
import java.lang.ref.WeakReference
import javax.inject.Inject

@AndroidEntryPoint
class OnGoingParentFragment : Fragment(R.layout.fragment_on_going_parent) {

    private val binding by viewBinding(FragmentOnGoingParentBinding::bind)
    private var pagerAdapterRef: WeakReference<OnGoingPagerAdapter>? = null

    @Inject
    lateinit var sessionManager: SessionManager
    private val onGoingPagesList: Array<OnGoingScreenModel> by lazy {
        arrayOf(
            OnGoingScreenModel(
                labelOne = getString(R.string.on_going_1st_top_label),
                labelSecond = getString(R.string.on_going_1st_bottom_label),
                imageRes = R.drawable.on_going_1
            ), OnGoingScreenModel(
                labelOne = getString(R.string.on_going_2nd_top_label),
                labelSecond = getString(R.string.on_going_2nd_bottom_label),
                imageRes = R.drawable.on_going_2
            ), OnGoingScreenModel(
                labelOne = getString(R.string.on_going_3rd_top_label),
                labelSecond = getString(R.string.on_going_3rd_bottom_label),
                imageRes = R.drawable.on_going_3
            ), OnGoingScreenModel(
                labelOne = getString(R.string.on_going_4th_top_label),
                labelSecond = getString(R.string.on_going_4th_bottom_label),
                imageRes = R.drawable.lost_phone_home_screen
            )
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupPagerAdapter()
        clickListeners()
    }

    private fun setupPagerAdapter() {
        val adapter = OnGoingPagerAdapter(childFragmentManager, lifecycle)
        pagerAdapterRef = WeakReference(adapter)
        binding?.apply {
            viewPager.adapter = adapter
            viewPager.isUserInputEnabled = true
            viewPager.setupButtonWithPageChange(
                btnContinue,
                nextText = resources.getString(R.string.next),
                finalText = resources.getString(R.string.get_started)
            )
        }
    }

    private fun clickListeners() {
        binding?.apply {
            btnContinue.setupNextButton(
                viewPager, pagerAdapterRef?.get() ?: return, findNavController(),
                sessionManager = sessionManager
            )
        }
    }

    inner class OnGoingPagerAdapter(
        fragmentManager: FragmentManager, lifecycle: Lifecycle
    ) : FragmentStateAdapter(fragmentManager, lifecycle) {

        override fun getItemCount(): Int = onGoingPagesList.size

        override fun createFragment(position: Int): Fragment {
            return OnGoingFragmentItem().apply {
                arguments = Bundle().apply {
                    putParcelable(ON_GOING_DATA_MODEL, onGoingPagesList[position])
                }
            }
        }
    }
}
