package com.euxcet.viturering.utils

import android.content.Context

class Utils {
    companion object {

        @JvmStatic
        fun dpToPx(context: Context, dp: Int): Int {
            val density = context.resources.displayMetrics.density
            return (dp * density).toInt()
        }
    }
}