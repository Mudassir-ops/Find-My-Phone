package com.example.findmyphone.domain

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class OnGoingScreenModel(val labelOne: String?, val labelSecond: String?, val imageRes: Int) :
    Parcelable
