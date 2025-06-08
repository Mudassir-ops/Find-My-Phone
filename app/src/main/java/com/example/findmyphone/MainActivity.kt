package com.example.findmyphone

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import com.example.findmyphone.databinding.ActivityMainBinding
import com.example.findmyphone.utils.SessionManager
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    @Inject
    lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment_activity_main) as NavHostFragment

        val navController = navHostFragment.navController
        val graph = navController.navInflater.inflate(R.navigation.mobile_navigation)
        val onboardingComplete = checkIfOnboardingCompleted()
        graph.setStartDestination(R.id.action_navigation_ongoing_parent_to_navigation_permission)
        val startDestination = if (onboardingComplete == true) {
            R.id.navigation_home_fragment
        } else {
            R.id.navigation_ongoing_parent
        }
        graph.setStartDestination(startDestination)
        navController.graph = graph
    }

    private fun checkIfOnboardingCompleted(): Boolean? {
        return sessionManager.getOnBoardingDone()
    }

}