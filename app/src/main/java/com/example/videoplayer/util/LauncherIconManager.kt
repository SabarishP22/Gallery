package com.example.videoplayer.util

import android.content.ComponentName
import android.content.Context
import android.content.pm.PackageManager

object LauncherIconManager {

    val BUCKET_COLORS = intArrayOf(
        0xFF00E5FF.toInt(),
        0xFF7C4DFF.toInt(),
        0xFFFF4081.toInt(),
        0xFFFF5252.toInt(),
        0xFFFF9800.toInt(),
        0xFFFFEB3B.toInt(),
        0xFF4CAF50.toInt(),
        0xFF009688.toInt(),
        0xFF2196F3.toInt(),
        0xFF12121A.toInt(),
    )

    private const val DEFAULT_ALIAS = "com.example.videoplayer.launcher.Default"
    private val bucketAliases = Array(10) { index ->
        "com.example.videoplayer.launcher.Bucket$index"
    }

    fun applyDefault(context: Context) {
        setEnabled(context, DEFAULT_ALIAS, true)
        bucketAliases.forEach { setEnabled(context, it, false) }
    }

    fun applyBucket(context: Context, bucketIndex: Int) {
        val index = bucketIndex.coerceIn(0, bucketAliases.lastIndex)
        setEnabled(context, DEFAULT_ALIAS, false)
        bucketAliases.forEachIndexed { i, alias ->
            setEnabled(context, alias, i == index)
        }
    }

    fun applyColor(context: Context, color: Int) {
        applyBucket(context, nearestLauncherBucket(color))
    }

    private fun setEnabled(context: Context, className: String, enabled: Boolean) {
        val state = if (enabled) {
            PackageManager.COMPONENT_ENABLED_STATE_ENABLED
        } else {
            PackageManager.COMPONENT_ENABLED_STATE_DISABLED
        }
        context.packageManager.setComponentEnabledSetting(
            ComponentName(context, className),
            state,
            PackageManager.DONT_KILL_APP,
        )
    }
}
