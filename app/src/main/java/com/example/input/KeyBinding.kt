package com.example.input

import android.view.KeyEvent

/**
 * Represents a binding between a physical key code and a cursor action.
 */
data class KeyBinding(
    val action: KeyAction,
    val keyCode: Int,
    val isEnabled: Boolean = true
) {
    /**
     * Friendly human-readable label for the key.
     */
    val keyDisplayName: String
        get() = getKeyDisplayName(keyCode)

    companion object {
        fun getKeyDisplayName(keyCode: Int): String {
            return when (keyCode) {
                KeyEvent.KEYCODE_DPAD_UP -> "Arrow Up"
                KeyEvent.KEYCODE_DPAD_DOWN -> "Arrow Down"
                KeyEvent.KEYCODE_DPAD_LEFT -> "Arrow Left"
                KeyEvent.KEYCODE_DPAD_RIGHT -> "Arrow Right"
                KeyEvent.KEYCODE_ENTER -> "Enter"
                KeyEvent.KEYCODE_DEL -> "Backspace"
                KeyEvent.KEYCODE_FORWARD_DEL -> "Delete"
                KeyEvent.KEYCODE_SPACE -> "Space"
                KeyEvent.KEYCODE_ESCAPE -> "Escape"
                KeyEvent.KEYCODE_F1 -> "F1"
                KeyEvent.KEYCODE_F2 -> "F2"
                KeyEvent.KEYCODE_F3 -> "F3"
                KeyEvent.KEYCODE_F4 -> "F4"
                KeyEvent.KEYCODE_F5 -> "F5"
                KeyEvent.KEYCODE_F6 -> "F6"
                KeyEvent.KEYCODE_F7 -> "F7"
                KeyEvent.KEYCODE_F8 -> "F8"
                KeyEvent.KEYCODE_F9 -> "F9"
                KeyEvent.KEYCODE_F10 -> "F10"
                KeyEvent.KEYCODE_F11 -> "F11"
                KeyEvent.KEYCODE_F12 -> "F12"
                KeyEvent.KEYCODE_SHIFT_LEFT -> "Left Shift"
                KeyEvent.KEYCODE_SHIFT_RIGHT -> "Right Shift"
                KeyEvent.KEYCODE_CTRL_LEFT -> "Left Ctrl"
                KeyEvent.KEYCODE_CTRL_RIGHT -> "Right Ctrl"
                KeyEvent.KEYCODE_ALT_LEFT -> "Left Alt"
                KeyEvent.KEYCODE_ALT_RIGHT -> "Right Alt"
                KeyEvent.KEYCODE_PAGE_UP -> "Page Up"
                KeyEvent.KEYCODE_PAGE_DOWN -> "Page Down"
                KeyEvent.KEYCODE_HOME -> "Home"
                KeyEvent.KEYCODE_MOVE_END -> "End"
                KeyEvent.KEYCODE_INSERT -> "Insert"
                KeyEvent.KEYCODE_TAB -> "Tab"
                KeyEvent.KEYCODE_APP_SWITCH -> "Recent Apps"
                KeyEvent.KEYCODE_W -> "W"
                KeyEvent.KEYCODE_A -> "A"
                KeyEvent.KEYCODE_S -> "S"
                KeyEvent.KEYCODE_D -> "D"
                KeyEvent.KEYCODE_I -> "I"
                KeyEvent.KEYCODE_J -> "J"
                KeyEvent.KEYCODE_K -> "K"
                KeyEvent.KEYCODE_L -> "L"
                else -> {
                    val raw = KeyEvent.keyCodeToString(keyCode)
                    if (raw.startsWith("KEYCODE_")) {
                        raw.substring(8).replace('_', ' ').lowercase().replaceFirstChar { it.uppercase() }
                    } else {
                        "Key ($keyCode)"
                    }
                }
            }
        }
    }
}
