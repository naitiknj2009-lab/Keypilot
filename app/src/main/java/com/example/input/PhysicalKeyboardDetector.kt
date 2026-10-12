package com.example.input

import android.content.Context
import android.content.res.Configuration
import android.hardware.input.InputManager
import android.view.InputDevice
import android.view.KeyCharacterMap
import android.view.KeyEvent
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class KeyboardDeviceInfo(
    val isConnected: Boolean,
    val name: String,
    val type: String,
    val vendorId: Int,
    val productId: Int,
    val hasDpadKeys: Boolean,
    val isExternal: Boolean
)

/**
 * Detects physical USB OTG, Bluetooth, and dock keyboards using Android InputManager.
 */
class PhysicalKeyboardDetector(context: Context) : InputManager.InputDeviceListener {

    private val appContext = context.applicationContext
    private val inputManager = appContext.getSystemService(Context.INPUT_SERVICE) as? InputManager

    private val _deviceState = MutableStateFlow(scanConnectedKeyboard())
    val deviceState: StateFlow<KeyboardDeviceInfo> = _deviceState.asStateFlow()

    init {
        inputManager?.registerInputDeviceListener(this, null)
    }

    fun refresh() {
        _deviceState.value = scanConnectedKeyboard()
    }

    override fun onInputDeviceAdded(deviceId: Int) {
        refresh()
    }

    override fun onInputDeviceRemoved(deviceId: Int) {
        refresh()
    }

    override fun onInputDeviceChanged(deviceId: Int) {
        refresh()
    }

    /**
     * Scans currently connected input devices for a hardware keyboard.
     */
    private fun scanConnectedKeyboard(): KeyboardDeviceInfo {
        val deviceIds = InputDevice.getDeviceIds()
        var bestMatch: KeyboardDeviceInfo? = null

        for (id in deviceIds) {
            val device = InputDevice.getDevice(id) ?: continue

            // Must have keyboard source and not be purely a virtual on-screen input
            val hasKeyboardSource = (device.sources and InputDevice.SOURCE_KEYBOARD) == InputDevice.SOURCE_KEYBOARD
            val isAlphabetic = device.keyboardType == InputDevice.KEYBOARD_TYPE_ALPHABETIC
            val isNotVirtual = !device.isVirtual

            if (hasKeyboardSource && isAlphabetic && isNotVirtual) {
                val hasDpad = KeyCharacterMap.deviceHasKey(KeyEvent.KEYCODE_DPAD_UP) &&
                        KeyCharacterMap.deviceHasKey(KeyEvent.KEYCODE_DPAD_DOWN)

                val isExternal = device.isExternal
                val typeName = when {
                    device.name.contains("bluetooth", ignoreCase = true) -> "Bluetooth Keyboard"
                    isExternal -> "USB OTG / External Keyboard"
                    else -> "Physical Keyboard / Dock"
                }

                val info = KeyboardDeviceInfo(
                    isConnected = true,
                    name = device.name.ifBlank { "Generic Physical Keyboard" },
                    type = typeName,
                    vendorId = device.vendorId,
                    productId = device.productId,
                    hasDpadKeys = hasDpad,
                    isExternal = isExternal
                )

                // Prefer external over internal
                if (isExternal || bestMatch == null) {
                    bestMatch = info
                }
            }
        }

        // Secondary fallback check via Configuration.hardKeyboardHidden or keyboard == KEYBOARD_QWERTY
        if (bestMatch == null) {
            val config = appContext.resources.configuration
            if (config.keyboard == Configuration.KEYBOARD_QWERTY && config.hardKeyboardHidden != Configuration.HARDKEYBOARDHIDDEN_YES) {
                return KeyboardDeviceInfo(
                    isConnected = true,
                    name = "Physical Hardware Keyboard",
                    type = "Hardware Attached",
                    vendorId = 0,
                    productId = 0,
                    hasDpadKeys = true,
                    isExternal = false
                )
            }

            return KeyboardDeviceInfo(
                isConnected = false,
                name = "No Physical Keyboard Detected",
                type = "Disconnected",
                vendorId = 0,
                productId = 0,
                hasDpadKeys = false,
                isExternal = false
            )
        }

        return bestMatch
    }

    fun unregister() {
        inputManager?.unregisterInputDeviceListener(this)
    }

    companion object {
        @Volatile
        private var instance: PhysicalKeyboardDetector? = null

        fun getInstance(context: Context): PhysicalKeyboardDetector {
            return instance ?: synchronized(this) {
                instance ?: PhysicalKeyboardDetector(context.applicationContext).also { instance = it }
            }
        }
    }
}
