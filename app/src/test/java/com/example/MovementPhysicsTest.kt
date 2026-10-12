package com.example

import com.example.cursor.CursorSpeedConfig
import com.example.cursor.Direction
import com.example.cursor.EdgeHandling
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.sqrt

class MovementPhysicsTest {

    @Test
    fun testBaseVelocitySlowNormalFast() {
        assertEquals(3f, CursorSpeedConfig.SLOW.baseSpeedPixels, 0.01f)
        assertEquals(10f, CursorSpeedConfig.NORMAL.baseSpeedPixels, 0.01f)
        assertEquals(20f, CursorSpeedConfig.FAST.baseSpeedPixels, 0.01f)
    }

    @Test
    fun testArrowKeyDirectionMapping() {
        val config = CursorSpeedConfig(baseSpeedPixels = 10f, accelerationEnabled = false)

        // Single directions
        val (upDx, upDy) = config.calculateDelta(setOf(Direction.UP), 0L, false)
        assertEquals(0f, upDx, 0.01f)
        assertEquals(-10f, upDy, 0.01f)

        val (downDx, downDy) = config.calculateDelta(setOf(Direction.DOWN), 0L, false)
        assertEquals(0f, downDx, 0.01f)
        assertEquals(10f, downDy, 0.01f)

        val (leftDx, leftDy) = config.calculateDelta(setOf(Direction.LEFT), 0L, false)
        assertEquals(-10f, leftDx, 0.01f)
        assertEquals(0f, leftDy, 0.01f)

        val (rightDx, rightDy) = config.calculateDelta(setOf(Direction.RIGHT), 0L, false)
        assertEquals(10f, rightDx, 0.01f)
        assertEquals(0f, rightDy, 0.01f)
    }

    @Test
    fun testDiagonalVectorNormalization() {
        val config = CursorSpeedConfig(baseSpeedPixels = 10f, accelerationEnabled = false)

        // Diagonal: UP and RIGHT simultaneously
        val (diagX, diagY) = config.calculateDelta(setOf(Direction.UP, Direction.RIGHT), 0L, false)

        // Total speed length should be exactly 10f (normalized), not 14.14f!
        val length = sqrt(diagX * diagX + diagY * diagY)
        assertEquals(10f, length, 0.01f)
        assertTrue(diagX > 0f)
        assertTrue(diagY < 0f)
    }

    @Test
    fun testAccelerationCurve() {
        val config = CursorSpeedConfig(
            baseSpeedPixels = 10f,
            accelerationEnabled = true,
            maxAccelerationMultiplier = 3.0f,
            accelerationRampMs = 400L
        )

        // Immediate press (< 100ms): base speed multiplier is 1.0x
        val (_, dyInitial) = config.calculateDelta(setOf(Direction.DOWN), 50L, false)
        assertEquals(10f, dyInitial, 0.01f)

        // Full ramp (> 500ms): multiplier reaches 3.0x (30px)
        val (_, dyMax) = config.calculateDelta(setOf(Direction.DOWN), 600L, false)
        assertEquals(30f, dyMax, 0.01f)
    }

    @Test
    fun testSpeedBoostModifier() {
        val config = CursorSpeedConfig(
            baseSpeedPixels = 10f,
            boostMultiplier = 2.5f,
            accelerationEnabled = false
        )

        // Holding Shift / Speed Boost
        val (dxBoost, _) = config.calculateDelta(setOf(Direction.RIGHT), 0L, true)
        assertEquals(25f, dxBoost, 0.01f)
    }

    @Test
    fun testCoordinateBoundsClamping() {
        val config = CursorSpeedConfig(edgeHandling = EdgeHandling.CLAMP)
        val screenWidth = 1280
        val screenHeight = 800

        // Clamping inside screen
        val (x1, y1) = config.clampCoordinates(500f, 400f, screenWidth, screenHeight)
        assertEquals(500f, x1, 0.01f)
        assertEquals(400f, y1, 0.01f)

        // Clamping negative coordinates
        val (xMin, yMin) = config.clampCoordinates(-50f, -100f, screenWidth, screenHeight)
        assertEquals(0f, xMin, 0.01f)
        assertEquals(0f, yMin, 0.01f)

        // Clamping beyond screen bounds
        val (xMax, yMax) = config.clampCoordinates(2000f, 1500f, screenWidth, screenHeight)
        assertEquals(1279f, xMax, 0.01f)
        assertEquals(799f, yMax, 0.01f)
    }

    @Test
    fun testCoordinateWrapAround() {
        val config = CursorSpeedConfig(edgeHandling = EdgeHandling.WRAP)
        val screenWidth = 1280
        val screenHeight = 800

        // Wrap left to right
        val (wrapRight, _) = config.clampCoordinates(-10f, 400f, screenWidth, screenHeight)
        assertEquals(1279f, wrapRight, 0.01f)

        // Wrap right to left
        val (wrapLeft, _) = config.clampCoordinates(1300f, 400f, screenWidth, screenHeight)
        assertEquals(0f, wrapLeft, 0.01f)
    }
}
