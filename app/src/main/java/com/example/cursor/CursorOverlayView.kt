package com.example.cursor

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PixelFormat
import android.os.Build
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.view.animation.DecelerateInterpolator

/**
 * High-performance, hardware-accelerated floating pointer overlay view rendered on top of
 * all applications via Android WindowManager.
 */
class CursorOverlayView(context: Context) : View(context) {

    private val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as? WindowManager

    private var cursorStyle = CursorStyle.ARROW
    private var cursorSizePx = 32f
    private var cursorColor = 0xFF8B5CF6.toInt()
    private var isButtonHeld = false
    private var showCoordinates = false

    private var currentX = 500f
    private var currentY = 500f
    private var isAttached = false

    // Click ripple animation state
    private var rippleRadius = 0f
    private var rippleAlpha = 0
    private var rippleAnimator: ValueAnimator? = null

    // Drawing paints and paths
    private val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }

    private val strokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 2.5f
    }

    private val glowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 6f
    }

    private val ripplePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 3f
    }

    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        textSize = 22f
        textAlign = Paint.Align.LEFT
    }

    private val badgePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = 0xCC0D1527.toInt()
        style = Paint.Style.FILL
    }

    private val arrowPath = Path()

    val layoutParams: WindowManager.LayoutParams by lazy {
        val windowType = WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY

        WindowManager.LayoutParams(
            (cursorSizePx * 2.5f).toInt(),
            (cursorSizePx * 2.5f).toInt(),
            windowType,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE or
                    WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS or
                    WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                    WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = currentX.toInt()
            y = currentY.toInt()
        }
    }

    init {
        updateArrowPath()
    }

    private fun updateArrowPath() {
        arrowPath.reset()
        val s = cursorSizePx
        // Modern sleek cursor pointer polygon
        arrowPath.moveTo(0f, 0f)
        arrowPath.lineTo(0f, s * 0.95f)
        arrowPath.lineTo(s * 0.28f, s * 0.72f)
        arrowPath.lineTo(s * 0.55f, s * 1.15f)
        arrowPath.lineTo(s * 0.72f, s * 1.05f)
        arrowPath.lineTo(s * 0.45f, s * 0.62f)
        arrowPath.lineTo(s * 0.85f, s * 0.62f)
        arrowPath.close()
    }

    fun updatePosition(x: Float, y: Float) {
        currentX = x
        currentY = y
        if (isAttached && windowManager != null) {
            layoutParams.x = x.toInt()
            layoutParams.y = y.toInt()
            try {
                windowManager.updateViewLayout(this, layoutParams)
            } catch (ignored: Exception) {}
        }
    }

    fun updateVisuals(style: CursorStyle, sizeDp: Int, colorHex: Long, buttonHeld: Boolean) {
        cursorStyle = style
        val density = resources.displayMetrics.density
        cursorSizePx = (sizeDp * density).coerceIn(16f * density, 64f * density)
        cursorColor = colorHex.toInt()
        isButtonHeld = buttonHeld
        updateArrowPath()

        // Update layout params dimensions if needed
        val boxDim = (cursorSizePx * 2.5f).toInt()
        if (layoutParams.width != boxDim) {
            layoutParams.width = boxDim
            layoutParams.height = boxDim
            if (isAttached && windowManager != null) {
                try {
                    windowManager.updateViewLayout(this, layoutParams)
                } catch (ignored: Exception) {}
            }
        }
        postInvalidate()
    }

    fun triggerClickRipple() {
        rippleAnimator?.cancel()
        val maxR = cursorSizePx * 1.2f
        rippleAnimator = ValueAnimator.ofFloat(0f, 1f).apply {
            duration = 320L
            interpolator = DecelerateInterpolator()
            addUpdateListener { anim ->
                val frac = anim.animatedValue as Float
                rippleRadius = maxR * frac
                rippleAlpha = ((1f - frac) * 230).toInt().coerceIn(0, 255)
                postInvalidate()
            }
            start()
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        // Draw click ripple if active
        if (rippleAlpha > 0 && rippleRadius > 0f) {
            ripplePaint.color = cursorColor
            ripplePaint.alpha = rippleAlpha
            canvas.drawCircle(0f, 0f, rippleRadius, ripplePaint)
        }

        when (cursorStyle) {
            CursorStyle.ARROW -> drawArrow(canvas)
            CursorStyle.NEON_RING -> drawNeonRing(canvas)
            CursorStyle.CROSSHAIR -> drawCrosshair(canvas)
            CursorStyle.DOT -> drawDot(canvas)
        }

        // Draw drag / hold indicator
        if (isButtonHeld) {
            val r = cursorSizePx * 0.22f
            fillPaint.color = 0xFFFF5555.toInt() // Red active drag indicator
            canvas.drawCircle(cursorSizePx * 0.7f, cursorSizePx * 0.7f, r, fillPaint)
        }
    }

    private fun drawArrow(canvas: Canvas) {
        // Specular glow layer
        glowPaint.color = cursorColor
        glowPaint.alpha = 80
        canvas.drawPath(arrowPath, glowPaint)

        // Inner solid body
        fillPaint.color = 0xFFFFFFFF.toInt() // White high-contrast center
        canvas.drawPath(arrowPath, fillPaint)

        // Colored rim border
        strokePaint.color = cursorColor
        strokePaint.alpha = 255
        canvas.drawPath(arrowPath, strokePaint)
    }

    private fun drawNeonRing(canvas: Canvas) {
        val r = cursorSizePx * 0.45f
        glowPaint.color = cursorColor
        glowPaint.alpha = 100
        canvas.drawCircle(r, r, r, glowPaint)

        strokePaint.color = cursorColor
        strokePaint.strokeWidth = 3f
        canvas.drawCircle(r, r, r, strokePaint)

        fillPaint.color = Color.WHITE
        canvas.drawCircle(r, r, 3f, fillPaint)
    }

    private fun drawCrosshair(canvas: Canvas) {
        val len = cursorSizePx * 0.6f
        val half = len / 2f
        strokePaint.color = cursorColor
        strokePaint.strokeWidth = 2.5f

        canvas.drawLine(0f, half, len, half, strokePaint)
        canvas.drawLine(half, 0f, half, len, strokePaint)

        strokePaint.strokeWidth = 1.5f
        canvas.drawCircle(half, half, half * 0.6f, strokePaint)
    }

    private fun drawDot(canvas: Canvas) {
        val r = cursorSizePx * 0.3f
        glowPaint.color = cursorColor
        glowPaint.alpha = 110
        canvas.drawCircle(r, r, r, glowPaint)

        fillPaint.color = cursorColor
        canvas.drawCircle(r, r, r * 0.7f, fillPaint)

        fillPaint.color = Color.WHITE
        canvas.drawCircle(r, r, r * 0.3f, fillPaint)
    }

    fun attachToWindow(): Boolean {
        if (isAttached || windowManager == null) return false
        return try {
            windowManager.addView(this, layoutParams)
            isAttached = true
            true
        } catch (e: Exception) {
            isAttached = false
            false
        }
    }

    fun detachFromWindow() {
        if (!isAttached || windowManager == null) return
        try {
            windowManager.removeView(this)
        } catch (ignored: Exception) {}
        isAttached = false
    }

    fun isAttachedToWindowNow(): Boolean = isAttached
}
