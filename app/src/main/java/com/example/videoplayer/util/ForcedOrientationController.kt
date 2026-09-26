package com.example.videoplayer.util

import android.app.Activity
import android.content.pm.ActivityInfo
import android.view.OrientationEventListener

class ForcedOrientationController(private val activity: Activity) {

    private var orientationListener: OrientationEventListener? = null
    private var lastRequestedOrientation: Int = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED

    fun start() {
        if (orientationListener != null) return
        orientationListener = object : OrientationEventListener(activity) {
            override fun onOrientationChanged(orientation: Int) {
                if (orientation == ORIENTATION_UNKNOWN) return
                val target = when {
                    orientation in 45..134 -> ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE
                    orientation in 135..224 -> ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT
                    orientation in 225..314 -> ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
                    else -> ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                }
                if (target != lastRequestedOrientation) {
                    lastRequestedOrientation = target
                    activity.requestedOrientation = target
                }
            }
        }
        if (orientationListener?.canDetectOrientation() == true) {
            orientationListener?.enable()
        } else {
            activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR
        }
    }

    fun stop() {
        orientationListener?.disable()
        orientationListener = null
        lastRequestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
    }
}
