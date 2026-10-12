package com.example.cursor

import android.os.SystemClock
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

/**
 * High-performance 60fps movement loop that handles simultaneous keys,
 * velocity acceleration, and position updates.
 */
class MovementEngine(
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.Main.immediate)
) {
    private val _cursorState = MutableStateFlow(CursorState())
    val cursorState: StateFlow<CursorState> = _cursorState.asStateFlow()

    var speedConfig: CursorSpeedConfig = CursorSpeedConfig.NORMAL

    var screenWidth: Int = 1280
    var screenHeight: Int = 800

    private val activeDirections = mutableSetOf<Direction>()
    private var holdStartTime: Long = 0L
    private var movementJob: Job? = null
    private var isSpeedBoostActive = false

    // Listener for immediate window manager updates
    var onPositionChangedListener: ((x: Float, y: Float) -> Unit)? = null

    init {
        // Initialize at center of screen
        resetPositionToCenter()
    }

    fun updateScreenDimensions(width: Int, height: Int) {
        if (width <= 0 || height <= 0) return
        val oldW = screenWidth
        val oldH = screenHeight
        screenWidth = width
        screenHeight = height

        // If cursor was clamped or outside new bounds, adjust it smoothly
        val current = _cursorState.value
        val (clampedX, clampedY) = speedConfig.clampCoordinates(current.x, current.y, width, height)
        if (clampedX != current.x || clampedY != current.y) {
            _cursorState.value = current.copy(x = clampedX, y = clampedY)
            onPositionChangedListener?.invoke(clampedX, clampedY)
        }
    }

    fun resetPositionToCenter() {
        val centerX = (screenWidth / 2).toFloat()
        val centerY = (screenHeight / 2).toFloat()
        _cursorState.value = _cursorState.value.copy(x = centerX, y = centerY)
        onPositionChangedListener?.invoke(centerX, centerY)
    }

    fun setCursorModeActive(active: Boolean) {
        if (!active) {
            // Stop any ongoing movement
            activeDirections.clear()
            stopMovementLoop()
        }
        _cursorState.value = _cursorState.value.copy(
            isCursorModeActive = active,
            activeDirections = emptySet()
        )
    }

    fun setSpeedBoostActive(active: Boolean) {
        isSpeedBoostActive = active
        _cursorState.value = _cursorState.value.copy(isSpeedBoostActive = active)
    }

    fun setButtonHeld(held: Boolean) {
        _cursorState.value = _cursorState.value.copy(isButtonHeld = held)
    }

    fun onDirectionKeyDown(direction: Direction) {
        val wasEmpty = activeDirections.isEmpty()
        activeDirections.add(direction)

        if (wasEmpty) {
            holdStartTime = SystemClock.uptimeMillis()
            startMovementLoop()
        }

        _cursorState.value = _cursorState.value.copy(activeDirections = activeDirections.toSet())
    }

    fun onDirectionKeyUp(direction: Direction) {
        activeDirections.remove(direction)
        if (activeDirections.isEmpty()) {
            stopMovementLoop()
        }
        _cursorState.value = _cursorState.value.copy(activeDirections = activeDirections.toSet())
    }

    fun recordClick(x: Float = _cursorState.value.x, y: Float = _cursorState.value.y, eventName: String = "Click") {
        _cursorState.value = _cursorState.value.copy(
            lastClickTime = SystemClock.uptimeMillis(),
            lastClickX = x,
            lastClickY = y,
            lastEventLog = "$eventName at (${x.toInt()}, ${y.toInt()})"
        )
    }

    fun updateVisualOptions(
        style: CursorStyle = _cursorState.value.cursorStyle,
        sizeDp: Int = _cursorState.value.cursorSizeDp,
        colorHex: Long = _cursorState.value.cursorColor,
        trail: Boolean = _cursorState.value.trailEnabled
    ) {
        _cursorState.value = _cursorState.value.copy(
            cursorStyle = style,
            cursorSizeDp = sizeDp,
            cursorColor = colorHex,
            trailEnabled = trail
        )
    }

    private fun startMovementLoop() {
        movementJob?.cancel()
        movementJob = scope.launch {
            while (isActive && activeDirections.isNotEmpty()) {
                val now = SystemClock.uptimeMillis()
                val holdDuration = (now - holdStartTime).coerceAtLeast(0L)

                val (dx, dy) = speedConfig.calculateDelta(
                    directions = activeDirections,
                    holdDurationMs = holdDuration,
                    isSpeedBoostActive = isSpeedBoostActive
                )

                if (dx != 0f || dy != 0f) {
                    val current = _cursorState.value
                    val rawX = current.x + dx
                    val rawY = current.y + dy

                    val (clampedX, clampedY) = speedConfig.clampCoordinates(
                        rawX, rawY, screenWidth, screenHeight
                    )

                    _cursorState.value = current.copy(
                        x = clampedX,
                        y = clampedY,
                        activeDirections = activeDirections.toSet()
                    )

                    onPositionChangedListener?.invoke(clampedX, clampedY)
                }

                // ~60 FPS update interval
                delay(16L)
            }
        }
    }

    private fun stopMovementLoop() {
        movementJob?.cancel()
        movementJob = null
    }

    fun cleanup() {
        stopMovementLoop()
    }
}
