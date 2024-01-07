package com.suihan74.satena2.utility

import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.os.Build

object PackageManagerCompat {
    @Suppress("DEPRECATION")
    @SuppressLint("QueryPermissionsNeeded")
    fun queryIntentActivities(pm: PackageManager, intent: Intent, flags: Int) : List<ResolveInfo> =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            pm.queryIntentActivities(intent, PackageManager.ResolveInfoFlags.of(flags.toLong()))
        }
        else {
            pm.queryIntentActivities(intent, flags)
        }
}
