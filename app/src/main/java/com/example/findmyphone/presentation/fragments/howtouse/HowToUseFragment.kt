package com.example.findmyphone.presentation.fragments.howtouse

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.findmyphone.R
import com.example.findmyphone.databinding.FragmentHowToUseBinding
import com.example.findmyphone.utils.viewBinding

class HowToUseFragment : Fragment(R.layout.fragment_how_to_use) {
    private val binding by viewBinding(FragmentHowToUseBinding::bind)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        clickListeners()
    }

    private fun clickListeners() {
        binding?.apply {
            headerLayout.apply {
                ivSettings.visibility = View.GONE
                btnBack.visibility = View.VISIBLE
                tvHeaderTitle.text = resources.getString(R.string.how_to_use)
                btnBack.setOnClickListener {
                    findNavController().navigateUp()
                }
            }
        }
    }
}