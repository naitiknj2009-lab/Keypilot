package com.example.utils

import android.content.Context
import android.graphics.Point
import android.util.DisplayMetrics
import android.view.WindowManager

object DisplayMetricsHelper {

    fun getScreenDimensions(context: Context): Point {
        val wm = context.getSystemService(Context.WINDOW_SERVICE) as? WindowManager
            ?: return Point(1280, 800)
        val point = Point()
        @Suppress("DEPRECATION")
        wm.defaultDisplay.getRealSize(point)
        return point
    }

    fun dpToPx(context: Context, dp: Float): Float {
        return dp * context.resources.displayMetrics.density
    }

    fun pxToDp(context: Context, px: Float): Float {
        return px / context.resources.displayMetrics.density
    }
}
