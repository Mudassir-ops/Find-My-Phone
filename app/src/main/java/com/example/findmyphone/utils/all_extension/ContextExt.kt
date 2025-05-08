package com.example.findmyphone.utils.all_extension

import android.app.Activity
import android.content.Context
import android.os.SystemClock
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog

fun Context.showToast(message: String) {
    Toast.makeText(this, message, Toast.LENGTH_LONG).show()
}

fun Context.showGenericAlertDialog(message: String) {
    AlertDialog.Builder(this).apply {
        setMessage(message)
        setPositiveButton("Ok") { dialog, _ ->
            dialog.dismiss()
        }
    }.show()
}

private var toast: Toast? = null
fun Activity.toast(message: String) {
    try {
        if (this.isDestroyed || this.isFinishing) return
        if (toast != null) {
            toast?.cancel()
        }
        toast = Toast.makeText(this, message, Toast.LENGTH_SHORT)
        if (this.isDestroyed || this.isFinishing) return
        toast?.show()

    } catch (e: Exception) {
        e.printStackTrace()
    }
}

var mLastClickTime: Long = 0
fun View.clickListener(action: (view: View) -> Unit) {
    this.setOnClickListener {
        if (SystemClock.elapsedRealtime() - mLastClickTime < 1000) {
            return@setOnClickListener
        }
        mLastClickTime = SystemClock.elapsedRealtime()
        action(it)
    }
}

