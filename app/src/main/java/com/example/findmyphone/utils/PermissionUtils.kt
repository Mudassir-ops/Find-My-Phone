package com.example.findmyphone.utils

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.DexterError
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener

/** Permission Util class to manage image permission*/
val permissionArray = when {
    Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU -> listOf(
        android.Manifest.permission.READ_MEDIA_IMAGES
    )

    Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q -> listOf(
        android.Manifest.permission.READ_EXTERNAL_STORAGE
    )

    else -> listOf(
        android.Manifest.permission.READ_EXTERNAL_STORAGE,
        android.Manifest.permission.WRITE_EXTERNAL_STORAGE
    )
}

fun Context.singlePermission(
    permissionString: String,
    onSinglePermissionGranted: () -> Unit,
    onSinglePermissionDenied: (() -> Unit)? = null,
    onSinglePermissionError: ((DexterError) -> Unit)? = null
) {
    Dexter.withContext(this).withPermissions(permissionString)
        .withListener(object : MultiplePermissionsListener {
            override fun onPermissionsChecked(report: MultiplePermissionsReport?) {
                report?.let {
                    if (report.areAllPermissionsGranted()) {
                        onSinglePermissionGranted()
                    } else {
                        if (onSinglePermissionDenied != null) {
                            onSinglePermissionDenied()
                        }
                    }
                    if (report.isAnyPermissionPermanentlyDenied) {
                        onSinglePermissionDenied?.let { it1 -> it1() }
                        showRationalDialogForPermissions()

                    }
                }
            }

            override fun onPermissionRationaleShouldBeShown(
                permissions: MutableList<PermissionRequest>?, token: PermissionToken?
            ) {
                token?.continuePermissionRequest()
            }

        }).withErrorListener {
            onSinglePermissionError?.let { it1 -> it1(it) }
        }.check()
}

fun Context.multiPermission(
    permissionArray: List<String>,
    onMultiPermissionGranted: () -> Unit,
    onMultiPermissionDenied: () -> Unit,
    onMultiPermissionError: (DexterError) -> Unit
) {
    Dexter.withContext(this).withPermissions(permissionArray)
        .withListener(object : MultiplePermissionsListener {
            override fun onPermissionsChecked(report: MultiplePermissionsReport?) {
                report?.let {
                    if (report.areAllPermissionsGranted()) {
                        onMultiPermissionGranted()
                    } else {
                        onMultiPermissionDenied()
                    }
                    if (report.isAnyPermissionPermanentlyDenied) {
                        onMultiPermissionDenied()
                        showRationalDialogForPermissions()

                    }
                }
            }

            override fun onPermissionRationaleShouldBeShown(
                permissions: MutableList<PermissionRequest>?, token: PermissionToken?
            ) {
                token?.continuePermissionRequest()
            }

        }).withErrorListener {
            onMultiPermissionError(it)
        }.check()
}

fun Context.showRationalDialogForPermissions() {
    AlertDialog.Builder(this).setMessage(
        "It looks that you have turned off " + "permissions required for these features. It can be enabled under " + "applications settings"
    ).setPositiveButton("Go To Settings") { _, _ ->
        try {
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
            val uri = Uri.fromParts("package", packageName, null)
            intent.data = uri
            startActivity(intent)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }.setNegativeButton("Cancel") { dialog, _ ->
        dialog.dismiss()
    }.show()
}

fun Context.checkPermissionGranted(permissionArray: List<String>): Boolean {
    val listPermissionsNeeded: ArrayList<String> = ArrayList()
    permissionArray.forEach { string ->
        val perm = ContextCompat.checkSelfPermission(this, string)
        if (perm != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(string)
        }
    }
    return listPermissionsNeeded.isEmpty()
}

fun Context.checkManageOverlayPermission(): Boolean {
    if (!Settings.canDrawOverlays(this)) {
        val intent = Intent(
            Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
            Uri.parse("package:${this.packageName}")
        )
        startActivity(intent)
        return false
    } else {
        return true
    }
}