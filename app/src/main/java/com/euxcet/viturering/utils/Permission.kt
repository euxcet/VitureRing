package com.euxcet.viturering.utils

import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.app.ComponentActivity

class Permission {
    companion object {
        fun requestPermissions(activity: ComponentActivity, permissions: List<String>) {
            for (permission in permissions) {
                if (ActivityCompat.checkSelfPermission(activity, permission)
                    != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(activity, arrayOf(permission), 1001)
                }
            }
        }
    }
}