package com.example.findmyphone.utils

import android.app.Activity
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.PixelFormat
import android.net.Uri
import android.os.Build
import android.os.Build.VERSION.SDK_INT
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Parcelable
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.fragment.app.FragmentManager
import com.example.findmyphone.R
import com.example.findmyphone.presentation.fragments.home.HomeFragmentFindMyPhone
import com.example.findmyphone.presentation.fragments.settings.SettingsFindMyPhoneFragment
import com.example.findmyphone.utils.all_extension.toast
import com.example.findmyphone.utils.dialogs.ExitDialog
import com.example.findmyphone.utils.dialogs.RateUsDialog
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

inline fun <reified T : Parcelable> Intent.parcelable(key: String): T? = when {
    SDK_INT >= 33 -> getParcelableExtra(key, T::class.java)
    else -> @Suppress("DEPRECATION") getParcelableExtra(key) as? T
}

inline fun <reified T : Parcelable> Bundle.parcelable(key: String): T? = when {
    SDK_INT >= 33 -> getParcelable(key, T::class.java)
    else -> @Suppress("DEPRECATION") getParcelable(key) as? T
}

fun View.setSafeOnClickListener(onSafeClick: (View) -> Unit) {
    val safeClickListener = SafeClickListener {
        onSafeClick(it)
    }
    setOnClickListener(safeClickListener)
}

fun PackageManager?.getPackageInfoCompat(packageName: String, flags: Int = 0): PackageInfo? =
    try {
        if (SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            this?.getPackageInfo(packageName, PackageManager.PackageInfoFlags.of(flags.toLong()))
        } else {
            (this?.getPackageInfo(packageName, flags))
        }
    } catch (e: PackageManager.NameNotFoundException) {
        // Handle the case where the package is not found
        null
    }


fun Activity.moreApps() {
    try {
        this.startActivity(
            Intent(
                Intent.ACTION_VIEW,
                Uri.parse("https://play.google.com/store/apps/details?id=com.parental.control.displaytime.kids.safety")
            )
        )
    } catch (e: Exception) {
        e.printStackTrace()
        toast(this.getString(R.string.no_launcher))
    }
}

fun Activity.privacyPolicyUrl() {
    try {
        this.startActivity(
            Intent(
                Intent.ACTION_VIEW,
                Uri.parse("")
            )
        )
    } catch (e: Exception) {
        e.printStackTrace()
        toast(this.getString(R.string.no_launcher))

    }
}

fun Activity.shareApp() {
    try {
        val sendIntent = Intent(Intent.ACTION_SEND)
        sendIntent.type = "text/plain"
        sendIntent.putExtra(
            Intent.EXTRA_SUBJECT, "ChargingAnimation"
        )
        var shareMessage = "\n Let me recommend you this application\n\n"
        shareMessage = """
             ${shareMessage}https://play.google.com/store/apps/details?id= ${this.packageName}
        """.trimIndent()
        sendIntent.putExtra(Intent.EXTRA_TEXT, shareMessage)
        this.startActivity(Intent.createChooser(sendIntent, "Choose one"))
    } catch (e: java.lang.Exception) {
        e.printStackTrace()
        this.toast("No Launcher")
    }
}

fun Activity.feedBackWithEmail(title: String, message: String, emailId: String) {
    try {
        val emailIntent = Intent(Intent.ACTION_SENDTO)
        emailIntent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK
        emailIntent.data = Uri.parse("mailto:")
        emailIntent.putExtra(Intent.EXTRA_EMAIL, arrayOf(emailId))
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, title)
        emailIntent.putExtra(Intent.EXTRA_TEXT, message)
        this.startActivity(emailIntent)

    } catch (e: java.lang.Exception) {
        e.printStackTrace()
    }
}

fun SettingsFindMyPhoneFragment.showRateDialog(
    fragmentManager: FragmentManager
) {
    if (rateUsDialog == null) {
        rateUsDialog = RateUsDialog {
            rateUsDialog = null
        }
    }
    if (rateUsDialog?.isAdded != true) {
        rateUsDialog?.show(fragmentManager, "RateDialog")
    }
}

fun Context.feedBackWithEmail(title: String, message: String, emailId: String) {
    try {
        val emailIntent = Intent(Intent.ACTION_SENDTO)
        emailIntent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK
        emailIntent.data = Uri.parse("mailto:")
        emailIntent.putExtra(Intent.EXTRA_EMAIL, arrayOf(emailId))
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, title)
        emailIntent.putExtra(Intent.EXTRA_TEXT, message)
        this.startActivity(emailIntent)

    } catch (e: java.lang.Exception) {
        e.printStackTrace()
    }
}

fun HomeFragmentFindMyPhone.showExitDialog(
    fragmentManager: FragmentManager
) {
    if (exitDialog == null) {
        exitDialog = ExitDialog {
            exitDialog = null
        }
    }
    if (exitDialog?.isAdded != true) {
        exitDialog?.show(fragmentManager, "ExitDialog")
    }
}

fun Context.showTimePicker(
    hour: Int = 12,
    minute: Int = 0,
    is24HourView: Boolean = true,
    onTimeSelected: (hour: Int, minute: Int) -> Unit
) {
    val timePicker = TimePickerDialog(this, { _, selectedHour, selectedMinute ->
        onTimeSelected(selectedHour, selectedMinute)
    }, hour, minute, is24HourView)
    timePicker.show()
}

fun isCurrentTimeInRange(startTimeStr: String, endTimeStr: String): Boolean {
    val timeFormatter = SimpleDateFormat("HH:mm", Locale.getDefault())
    val now = Calendar.getInstance()
    val currentTime = timeFormatter.format(now.time)
    val currentDate = timeFormatter.parse(currentTime)
    val startDate = timeFormatter.parse(startTimeStr)
    val endDate = timeFormatter.parse(endTimeStr)
    if (startDate == null || endDate == null || currentDate == null) return false

    return if (startDate.before(endDate)) {
        currentDate.after(startDate) && currentDate.before(endDate)
    } else {
        currentDate.after(startDate) || currentDate.before(endDate)
    }
}

fun showOverlay(context: Context, onShown: () -> Unit) {
    val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
    val params = WindowManager.LayoutParams(
        WindowManager.LayoutParams.WRAP_CONTENT,
        WindowManager.LayoutParams.WRAP_CONTENT,
        if (SDK_INT >= Build.VERSION_CODES.O)
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        else
            WindowManager.LayoutParams.TYPE_PHONE,
        WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS or
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
        PixelFormat.TRANSLUCENT
    )
    params.gravity = Gravity.TOP or Gravity.START

    val overlayView = View(context).apply {
        setBackgroundColor(Color.TRANSPARENT)
        layoutParams = ViewGroup.LayoutParams(1, 1)
        visibility = View.VISIBLE
        post {
            onShown()
        }
    }

    try {
        windowManager.addView(overlayView, params)
    } catch (e: Exception) {
        Log.e("Overlay", "Overlay add failed: ${e.message}")
        onShown()
    }
    Handler(Looper.getMainLooper()).postDelayed({
        try {
            windowManager.removeView(overlayView)
        } catch (e: Exception) {
            Log.e("Overlay", "Overlay remove failed: ${e.message}")
        }
    }, 2000)
}

