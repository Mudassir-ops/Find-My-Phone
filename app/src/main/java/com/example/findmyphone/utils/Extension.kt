package com.example.findmyphone.utils

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Build.VERSION.SDK_INT
import android.os.Bundle
import android.os.Parcelable
import android.view.View
import com.example.findmyphone.R
import com.example.findmyphone.utils.all_extension.toast

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

fun Activity.feedBackWithEmail(title:String,message:String,emailId:String){
    try {
        val emailIntent = Intent(Intent.ACTION_SENDTO)
        emailIntent.flags  = Intent.FLAG_ACTIVITY_CLEAR_TASK
        emailIntent.data  = Uri.parse("mailto:")
        emailIntent.putExtra(Intent.EXTRA_EMAIL, arrayOf(emailId))
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, title)
        emailIntent.putExtra(Intent.EXTRA_TEXT, message)
        this.startActivity(emailIntent)

    }catch (e:java.lang.Exception){
        e.printStackTrace()
    }
}
