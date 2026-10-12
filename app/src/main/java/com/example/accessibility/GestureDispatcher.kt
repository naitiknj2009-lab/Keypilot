package com.example.accessibility

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.graphics.Path
import android.os.Handler
import android.os.Looper

/**
 * Dispatches simulated touch taps, long-presses, and scroll gestures at arbitrary screen coordinates
 * using Android's official AccessibilityService dispatchGesture API.
 */
class GestureDispatcher(private val service: AccessibilityService) {

    private val mainHandler = Handler(Looper.getMainLooper())

    /**
     * Dispatches a single click/tap gesture at (x, y).
     */
    fun dispatchTap(x: Float, y: Float, onComplete: ((Boolean) -> Unit)? = null): Boolean {
        val path = Path().apply {
            moveTo(x, y)
        }
        val stroke = GestureDescription.StrokeDescription(path, 0L, 45L)
        val gesture = GestureDescription.Builder()
            .addStroke(stroke)
            .build()

        return service.dispatchGesture(
            gesture,
            object : AccessibilityService.GestureResultCallback() {
                override fun onCompleted(gestureDescription: GestureDescription?) {
                    onComplete?.invoke(true)
                }

                override fun onCancelled(gestureDescription: GestureDescription?) {
                    onComplete?.invoke(false)
                }
            },
            mainHandler
        )
    }

    /**
     * Dispatches a long-press (context click) gesture at (x, y).
     */
    fun dispatchLongPress(x: Float, y: Float, onComplete: ((Boolean) -> Unit)? = null): Boolean {
        val path = Path().apply {
            moveTo(x, y)
        }
        val stroke = GestureDescription.StrokeDescription(path, 0L, 600L)
        val gesture = GestureDescription.Builder()
            .addStroke(stroke)
            .build()

        return service.dispatchGesture(
            gesture,
            object : AccessibilityService.GestureResultCallback() {
                override fun onCompleted(gestureDescription: GestureDescription?) {
                    onComplete?.invoke(true)
                }

                override fun onCancelled(gestureDescription: GestureDescription?) {
                    onComplete?.invoke(false)
                }
            },
            mainHandler
        )
    }

    /**
     * Dispatches a drag gesture between two points.
     */
    fun dispatchDrag(
        startX: Float,
        startY: Float,
        endX: Float,
        endY: Float,
        durationMs: Long = 350L,
        onComplete: ((Boolean) -> Unit)? = null
    ): Boolean {
        val path = Path().apply {
            moveTo(startX, startY)
            lineTo(endX, endY)
        }
        val stroke = GestureDescription.StrokeDescription(path, 0L, durationMs)
        val gesture = GestureDescription.Builder()
            .addStroke(stroke)
            .build()

        return service.dispatchGesture(
            gesture,
            object : AccessibilityService.GestureResultCallback() {
                override fun onCompleted(gestureDescription: GestureDescription?) {
                    onComplete?.invoke(true)
                }

                override fun onCancelled(gestureDescription: GestureDescription?) {
                    onComplete?.invoke(false)
                }
            },
            mainHandler
        )
    }

    /**
     * Dispatches a vertical scroll swipe gesture at the pointer coordinates.
     */
    fun dispatchScroll(x: Float, y: Float, isScrollUp: Boolean, onComplete: ((Boolean) -> Unit)? = null): Boolean {
        val delta = 280f
        val startY = if (isScrollUp) y + delta else y - delta
        val endY = if (isScrollUp) y - delta else y + delta

        val path = Path().apply {
            moveTo(x, startY)
            lineTo(x, endY)
        }
        val stroke = GestureDescription.StrokeDescription(path, 0L, 200L)
        val gesture = GestureDescription.Builder()
            .addStroke(stroke)
            .build()

        return service.dispatchGesture(
            gesture,
            object : AccessibilityService.GestureResultCallback() {
                override fun onCompleted(gestureDescription: GestureDescription?) {
                    onComplete?.invoke(true)
                }

                override fun onCancelled(gestureDescription: GestureDescription?) {
                    onComplete?.invoke(false)
                }
            },
            mainHandler
        )
    }
}
