package com.example.videoplayer.util

import android.content.ComponentName
import android.content.Context
import android.content.pm.PackageManager

object LauncherIconManager {

    private const val DEFAULT_ALIAS = "com.example.videoplayer.launcher.Default"

    fun applyDefault(context: Context) {
        runCatching {
            val state = PackageManager.COMPONENT_ENABLED_STATE_ENABLED
            context.packageManager.setComponentEnabledSetting(
                ComponentName(context, DEFAULT_ALIAS),
                state,
                PackageManager.DONT_KILL_APP,
            )
        }
    }
}
