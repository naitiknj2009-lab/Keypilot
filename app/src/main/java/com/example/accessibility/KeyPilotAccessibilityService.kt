package com.example.accessibility

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.content.Context
import android.content.res.Configuration
import android.graphics.Point
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.view.KeyEvent
import android.view.WindowManager
import android.view.accessibility.AccessibilityEvent
import com.example.cursor.CursorOverlayView
import com.example.cursor.CursorSpeedConfig
import com.example.cursor.Direction
import com.example.cursor.MovementEngine
import com.example.input.KeyAction
import com.example.input.KeyMappingRepository
import com.example.settings.AppSettings
import com.example.settings.SettingsRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

/**
 * System-wide AccessibilityService that intercepts physical keyboard events and dispatches
 * virtual pointer movement, gestures, and clicks.
 */
class KeyPilotAccessibilityService : AccessibilityService() {

    private val serviceScope = CoroutineScope(Dispatchers.Main.immediate)
    private var settingsJob: Job? = null

    private lateinit var keyMappingRepo: KeyMappingRepository
    private lateinit var settingsRepo: SettingsRepository
    private lateinit var movementEngine: MovementEngine
    private lateinit var gestureDispatcher: GestureDispatcher
    private var overlayView: CursorOverlayView? = null
    private var vibrator: Vibrator? = null

    override fun onCreate() {
        super.onCreate()
        vibrator = getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        keyMappingRepo = KeyMappingRepository.getInstance(this)
        settingsRepo = SettingsRepository.getInstance(this)
        movementEngine = MovementEngine(serviceScope)
        gestureDispatcher = GestureDispatcher(this)
        activeServiceInstance = this
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        _isServiceRunning.value = true

        val info = serviceInfo ?: AccessibilityServiceInfo()
        info.apply {
            eventTypes = AccessibilityEvent.TYPES_ALL_MASK
            feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC
            notificationTimeout = 100
            flags = flags or
                    AccessibilityServiceInfo.FLAG_REQUEST_FILTER_KEY_EVENTS or
                    AccessibilityServiceInfo.FLAG_RETRIEVE_INTERACTIVE_WINDOWS
        }
        serviceInfo = info

        updateDisplayMetrics()
        initOverlay()

        // Sync settings from repository
        settingsJob?.cancel()
        settingsJob = serviceScope.launch {
            settingsRepo.settings.collectLatest { appSettings ->
                applySettings(appSettings)
            }
        }
    }

    private fun initOverlay() {
        if (overlayView == null) {
            overlayView = CursorOverlayView(this)
            movementEngine.onPositionChangedListener = { x, y ->
                overlayView?.updatePosition(x, y)
            }
        }
    }

    private fun updateDisplayMetrics() {
        val wm = getSystemService(Context.WINDOW_SERVICE) as? WindowManager ?: return
        val size = Point()
        @Suppress("DEPRECATION")
        wm.defaultDisplay.getRealSize(size)
        movementEngine.updateScreenDimensions(size.x, size.y)
    }

    private fun applySettings(settings: AppSettings) {
        val speedConfig = when (settings.speedPreset) {
            "slow" -> CursorSpeedConfig.SLOW
            "fast" -> CursorSpeedConfig.FAST
            "custom" -> CursorSpeedConfig(baseSpeedPixels = settings.customSpeed)
            else -> CursorSpeedConfig.NORMAL
        }.copy(
            accelerationEnabled = settings.accelerationEnabled,
            edgeHandling = settings.edgeHandling
        )
        movementEngine.speedConfig = speedConfig

        movementEngine.updateVisualOptions(
            style = settings.cursorStyle,
            sizeDp = settings.cursorSizeDp,
            colorHex = settings.cursorColor,
            trail = settings.cursorTrailEnabled
        )

        overlayView?.updateVisuals(
            style = settings.cursorStyle,
            sizeDp = settings.cursorSizeDp,
            colorHex = settings.cursorColor,
            buttonHeld = movementEngine.cursorState.value.isButtonHeld
        )
    }

    override fun onKeyEvent(event: KeyEvent): Boolean {
        val keyCode = event.keyCode
        val action = keyMappingRepo.getActionForKeyCode(keyCode) ?: return false

        // 1. Toggle Cursor Mode (always handled regardless of whether cursor mode is active)
        if (action == KeyAction.TOGGLE_CURSOR_MODE) {
            if (event.action == KeyEvent.ACTION_UP) {
                toggleCursorMode()
            }
            return true
        }

        // If cursor mode is currently inactive, let key pass through for normal typing
        if (!movementEngine.cursorState.value.isCursorModeActive) {
            return false
        }

        // 2. Handle active cursor controls
        val isDown = event.action == KeyEvent.ACTION_DOWN
        val isUp = event.action == KeyEvent.ACTION_UP

        when (action) {
            KeyAction.MOVE_UP -> {
                if (isDown && event.repeatCount == 0) movementEngine.onDirectionKeyDown(Direction.UP)
                else if (isUp) movementEngine.onDirectionKeyUp(Direction.UP)
                return true
            }
            KeyAction.MOVE_DOWN -> {
                if (isDown && event.repeatCount == 0) movementEngine.onDirectionKeyDown(Direction.DOWN)
                else if (isUp) movementEngine.onDirectionKeyUp(Direction.DOWN)
                return true
            }
            KeyAction.MOVE_LEFT -> {
                if (isDown && event.repeatCount == 0) movementEngine.onDirectionKeyDown(Direction.LEFT)
                else if (isUp) movementEngine.onDirectionKeyUp(Direction.LEFT)
                return true
            }
            KeyAction.MOVE_RIGHT -> {
                if (isDown && event.repeatCount == 0) movementEngine.onDirectionKeyDown(Direction.RIGHT)
                else if (isUp) movementEngine.onDirectionKeyUp(Direction.RIGHT)
                return true
            }
            KeyAction.SPEED_BOOST -> {
                if (isDown) movementEngine.setSpeedBoostActive(true)
                else if (isUp) movementEngine.setSpeedBoostActive(false)
                return true
            }
            KeyAction.PRIMARY_CLICK -> {
                if (isUp) {
                    val state = movementEngine.cursorState.value
                    gestureDispatcher.dispatchTap(state.x, state.y)
                    overlayView?.triggerClickRipple()
                    movementEngine.recordClick(state.x, state.y, "Primary Click")
                    vibrateFeedback(30)
                }
                return true
            }
            KeyAction.SECONDARY_CLICK -> {
                if (isUp) {
                    val state = movementEngine.cursorState.value
                    gestureDispatcher.dispatchLongPress(state.x, state.y)
                    movementEngine.recordClick(state.x, state.y, "Secondary Click")
                    vibrateFeedback(45)
                }
                return true
            }
            KeyAction.TOGGLE_HOLD -> {
                if (isUp) {
                    val newHeld = !movementEngine.cursorState.value.isButtonHeld
                    movementEngine.setButtonHeld(newHeld)
                    overlayView?.updateVisuals(
                        style = movementEngine.cursorState.value.cursorStyle,
                        sizeDp = movementEngine.cursorState.value.cursorSizeDp,
                        colorHex = movementEngine.cursorState.value.cursorColor,
                        buttonHeld = newHeld
                    )
                    vibrateFeedback(if (newHeld) 50 else 25)
                }
                return true
            }
            KeyAction.SCROLL_UP -> {
                if (isUp) {
                    val state = movementEngine.cursorState.value
                    gestureDispatcher.dispatchScroll(state.x, state.y, isScrollUp = true)
                    movementEngine.recordClick(state.x, state.y, "Scroll Up")
                }
                return true
            }
            KeyAction.SCROLL_DOWN -> {
                if (isUp) {
                    val state = movementEngine.cursorState.value
                    gestureDispatcher.dispatchScroll(state.x, state.y, isScrollUp = false)
                    movementEngine.recordClick(state.x, state.y, "Scroll Down")
                }
                return true
            }
            KeyAction.GLOBAL_BACK -> {
                if (isUp) performGlobalAction(GLOBAL_ACTION_BACK)
                return true
            }
            KeyAction.GLOBAL_HOME -> {
                if (isUp) performGlobalAction(GLOBAL_ACTION_HOME)
                return true
            }
            KeyAction.GLOBAL_RECENTS -> {
                if (isUp) performGlobalAction(GLOBAL_ACTION_RECENTS)
                return true
            }
            else -> return false
        }
    }

    fun toggleCursorMode(explicitState: Boolean? = null) {
        val currentState = movementEngine.cursorState.value.isCursorModeActive
        val newState = explicitState ?: !currentState

        movementEngine.setCursorModeActive(newState)
        _isCursorModeActive.value = newState

        if (newState) {
            initOverlay()
            overlayView?.attachToWindow()
            vibrateFeedback(40)
        } else {
            overlayView?.detachFromWindow()
            vibrateFeedback(20)
        }
    }

    private fun vibrateFeedback(durationMs: Long) {
        if (!settingsRepo.settings.value.vibrationFeedback) return
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator?.vibrate(VibrationEffect.createOneShot(durationMs, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(durationMs)
            }
        } catch (ignored: Exception) {}
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        updateDisplayMetrics()
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        // Handled as needed
    }

    override fun onInterrupt() {
        movementEngine.cleanup()
    }

    override fun onDestroy() {
        super.onDestroy()
        activeServiceInstance = null
        _isServiceRunning.value = false
        _isCursorModeActive.value = false
        settingsJob?.cancel()
        overlayView?.detachFromWindow()
        movementEngine.cleanup()
    }

    companion object {
        private val _isServiceRunning = MutableStateFlow(false)
        val isServiceRunning: StateFlow<Boolean> = _isServiceRunning.asStateFlow()

        private val _isCursorModeActive = MutableStateFlow(false)
        val isCursorModeActive: StateFlow<Boolean> = _isCursorModeActive.asStateFlow()

        @Volatile
        var activeServiceInstance: KeyPilotAccessibilityService? = null
            private set
    }
}
