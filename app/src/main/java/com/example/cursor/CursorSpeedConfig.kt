package com.example.cursor

import kotlin.math.sqrt

/**
 * Handles pointer physics, velocity calculations, acceleration curves, and bounds checking.
 */
data class CursorSpeedConfig(
    val baseSpeedPixels: Float = 10f, // 2-4 (slow), 8-12 (normal), 18-24 (fast)
    val boostMultiplier: Float = 2.5f,
    val accelerationEnabled: Boolean = true,
    val maxAccelerationMultiplier: Float = 3.0f,
    val accelerationRampMs: Long = 400L,
    val edgeHandling: EdgeHandling = EdgeHandling.CLAMP,
    val diagonalMovementEnabled: Boolean = true
) {
    /**
     * Calculates the movement delta for the given directions and key hold duration.
     *
     * @param directions The currently active directions (e.g. UP and RIGHT).
     * @param holdDurationMs Time elapsed since the movement keys began being held.
     * @param isSpeedBoostActive Whether the shift/boost modifier is active.
     * @return Pair of (deltaX, deltaY) in pixels.
     */
    fun calculateDelta(
        directions: Set<Direction>,
        holdDurationMs: Long,
        isSpeedBoostActive: Boolean
    ): Pair<Float, Float> {
        if (directions.isEmpty()) return 0f to 0f

        var vx = 0f
        var vy = 0f

        if (Direction.UP in directions) vy -= 1f
        if (Direction.DOWN in directions) vy += 1f
        if (Direction.LEFT in directions) vx -= 1f
        if (Direction.RIGHT in directions) vx += 1f

        if (vx == 0f && vy == 0f) return 0f to 0f

        // Normalize diagonal vector so diagonal speed does not exceed 1.0
        val length = sqrt(vx * vx + vy * vy)
        val normX = if (diagonalMovementEnabled || (vx == 0f || vy == 0f)) (vx / length) else (if (vx != 0f) vx else 0f)
        val normY = if (diagonalMovementEnabled || (vx == 0f || vy == 0f)) (vy / length) else (if (vy != 0f) vy else 0f)

        // Calculate acceleration multiplier
        var speedMultiplier = 1.0f
        if (accelerationEnabled && holdDurationMs > 100L) {
            val progress = ((holdDurationMs - 100L).toFloat() / accelerationRampMs).coerceIn(0f, 1f)
            // Ease-in quadratic curve
            val easeProgress = progress * progress
            speedMultiplier = 1.0f + (maxAccelerationMultiplier - 1.0f) * easeProgress
        }

        if (isSpeedBoostActive) {
            speedMultiplier *= boostMultiplier
        }

        val step = baseSpeedPixels * speedMultiplier
        return (normX * step) to (normY * step)
    }

    /**
     * Applies coordinate bounds checking according to edgeHandling setting.
     */
    fun clampCoordinates(
        x: Float,
        y: Float,
        screenWidth: Int,
        screenHeight: Int
    ): Pair<Float, Float> {
        val maxX = (screenWidth - 1).coerceAtLeast(0).toFloat()
        val maxY = (screenHeight - 1).coerceAtLeast(0).toFloat()

        return when (edgeHandling) {
            EdgeHandling.CLAMP -> {
                val clampedX = x.coerceIn(0f, maxX)
                val clampedY = y.coerceIn(0f, maxY)
                clampedX to clampedY
            }
            EdgeHandling.WRAP -> {
                var wrappedX = x
                var wrappedY = y
                if (wrappedX < 0f) wrappedX = maxX
                else if (wrappedX > maxX) wrappedX = 0f

                if (wrappedY < 0f) wrappedY = maxY
                else if (wrappedY > maxY) wrappedY = 0f

                wrappedX to wrappedY
            }
        }
    }

    companion object {
        val SLOW = CursorSpeedConfig(baseSpeedPixels = 3f)
        val NORMAL = CursorSpeedConfig(baseSpeedPixels = 10f)
        val FAST = CursorSpeedConfig(baseSpeedPixels = 20f)
    }
}
