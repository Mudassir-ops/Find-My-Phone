package com.example.findmyphone.utils.event

data class UpdateEvent(
    var count: Int,
    var currentAppCheckIn: Int,
    var totalAppSize: Int,
    var isAllAppCheckFinished: Boolean
)