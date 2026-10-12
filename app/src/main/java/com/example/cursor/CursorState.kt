package com.example.cursor

enum class Direction {
    UP, DOWN, LEFT, RIGHT
}

enum class CursorStyle(val displayName: String) {
    ARROW("Cyber Arrow"),
    NEON_RING("Neon Ring"),
    CROSSHAIR("Precision Crosshair"),
    DOT("Minimal Dot")
}

enum class EdgeHandling(val displayName: String, val description: String) {
    CLAMP("Clamp to Edge", "Stops cursor at screen boundaries"),
    WRAP("Wrap Around", "Cursor wraps around to the opposite side of the screen")
}

/**
 * Immutable state snapshot of the virtual cursor.
 */
data class CursorState(
    val x: Float = 500f,
    val y: Float = 500f,
    val isCursorModeActive: Boolean = false,
    val isSpeedBoostActive: Boolean = false,
    val isButtonHeld: Boolean = false,
    val activeDirections: Set<Direction> = emptySet(),
    val lastClickTime: Long = 0L,
    val lastClickX: Float = -1f,
    val lastClickY: Float = -1f,
    val clickRippleAnimFraction: Float = 0f,
    val cursorStyle: CursorStyle = CursorStyle.ARROW,
    val cursorSizeDp: Int = 30,
    val cursorColor: Long = 0xFF8B5CF6, // Neon Violet
    val trailEnabled: Boolean = false,
    val lastEventLog: String = "Ready"
)
