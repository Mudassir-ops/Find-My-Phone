package com.example.findmyphone.presentation.fragments.ongoingscreens

import android.widget.CheckBox
import androidx.appcompat.widget.AppCompatCheckBox
import androidx.navigation.NavController
import androidx.viewpager2.widget.ViewPager2
import com.example.findmyphone.R
import com.google.android.material.button.MaterialButton


fun MaterialButton.setupNextButton(
    viewPager: ViewPager2,
    pagerAdapter: OnGoingParentFragment.OnGoingPagerAdapter,
    navController: NavController
) {
    setOnClickListener {
        val currentItem = viewPager.currentItem
        val lastItemIndex = pagerAdapter.itemCount - 1
        when {
            currentItem < lastItemIndex -> {
                viewPager.currentItem = currentItem + 1
            }

            currentItem == lastItemIndex -> {
                if (navController.currentDestination?.id == R.id.navigation_ongoing_parent) {
                    navController.navigate(R.id.action_navigation_ongoing_parent_to_navigation_permission)
                }
            }
        }
    }
}


fun ViewPager2.setupButtonWithPageChange(
    btn: MaterialButton,
    nextText: String = resources.getString(R.string.next),
    finalText: String = resources.getString(R.string.get_started),
) {
    this.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
        override fun onPageSelected(position: Int) {
            super.onPageSelected(position)
            when (position) {
                0, 1, 2 -> {
                    btn.text = nextText
                }

                3 -> {
                    btn.text = finalText
                }
            }
        }
    })
}
