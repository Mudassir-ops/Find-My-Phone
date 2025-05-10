package com.example.findmyphone.presentation.fragments.permission

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.findmyphone.R
import com.example.findmyphone.databinding.FragmentPermissionFindMyPhoneBinding
import com.example.findmyphone.utils.viewBinding

@SuppressLint("InlinedApi")
class PermissionFindMyPhoneFragment : Fragment(R.layout.fragment_permission_find_my_phone) {
    private val binding by viewBinding(FragmentPermissionFindMyPhoneBinding::bind)
    private val permissionViews by lazy {
        mapOf(
            Manifest.permission.CAMERA to binding?.view1,
            Manifest.permission.RECORD_AUDIO to binding?.view2,
            Manifest.permission.POST_NOTIFICATIONS to binding?.view5
        )
    }
    private val overlayView by lazy { binding?.view3 }

    private val requiredPermissions: List<String>
        get() {
            val basePermissions = mutableListOf(
                Manifest.permission.CAMERA,
                Manifest.permission.RECORD_AUDIO
            )
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                basePermissions.add(Manifest.permission.POST_NOTIFICATIONS)
            }
            return basePermissions
        }

    private val permissionsLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { results ->
        results.forEach { (permission, isGranted) ->
            Log.d("Permissions", "$permission granted: $isGranted")
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        onClickListener()
    }

    override fun onResume() {
        super.onResume()
        updateButtonState()
        updatePermissionViews()
    }

    private fun onClickListener() {
        binding?.tvButton?.setOnClickListener {
            requestAllPermissions()
        }
    }

    private fun requestOverlayPermission() {
        if (!Settings.canDrawOverlays(context ?: return)) {
            val intent = Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:${context?.packageName}")
            )
            startActivity(intent)
        }
    }

    private fun requestAllPermissions() {
        val toRequest = requiredPermissions.filter {
            ContextCompat.checkSelfPermission(
                context ?: return,
                it
            ) != PackageManager.PERMISSION_GRANTED
        }
        if (toRequest.isNotEmpty()) {
            permissionsLauncher.launch(toRequest.toTypedArray())
        }
        requestOverlayPermission()
        if (toRequest.isEmpty() && Settings.canDrawOverlays(context ?: return)) {
            if (findNavController().currentDestination?.id == R.id.navigation_permission) {
                findNavController().navigate(R.id.action_navigation_permission_to_navigation_home_fragment)
            }
        }
    }

    private fun enableButton() {
        binding?.tvButton?.alpha = 1f
        binding?.tvButton?.isClickable = true
        binding?.tvButton?.isEnabled = true
    }

    private fun updateButtonState() {
        val allGranted = requiredPermissions.all {
            ContextCompat.checkSelfPermission(
                requireContext(),
                it
            ) == PackageManager.PERMISSION_GRANTED
        } && Settings.canDrawOverlays(context ?: return)
        if (allGranted) {
            enableButton()
        }
    }

    private fun updatePermissionViews() {
        permissionViews.forEach { (permission, view) ->
            val granted = ContextCompat.checkSelfPermission(
                requireContext(),
                permission
            ) == PackageManager.PERMISSION_GRANTED
            view?.setBackgroundResource(
                if (granted) R.drawable.filled_circle_shape else R.drawable.empty_circle_shape
            )
        }
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            binding?.view5?.visibility = View.GONE
            binding?.tvPermissionNotification?.visibility = View.GONE
        }
        val hasOverlayPermission = Settings.canDrawOverlays(requireContext())
        overlayView?.setBackgroundResource(
            if (hasOverlayPermission) R.drawable.filled_circle_shape else R.drawable.empty_circle_shape
        )
    }
}