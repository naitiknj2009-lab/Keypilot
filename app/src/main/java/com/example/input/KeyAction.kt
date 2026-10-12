package com.example.input

import android.view.KeyEvent

/**
 * Defines all cursor and system navigation actions that can be mapped to physical keyboard keys.
 */
enum class KeyAction(
    val title: String,
    val description: String,
    val defaultKeyCode: Int,
    val category: ActionCategory,
    val isContinuous: Boolean = false
) {
    MOVE_UP(
        title = "Move Up",
        description = "Moves pointer upward across the display",
        defaultKeyCode = KeyEvent.KEYCODE_DPAD_UP,
        category = ActionCategory.MOVEMENT,
        isContinuous = true
    ),
    MOVE_DOWN(
        title = "Move Down",
        description = "Moves pointer downward across the display",
        defaultKeyCode = KeyEvent.KEYCODE_DPAD_DOWN,
        category = ActionCategory.MOVEMENT,
        isContinuous = true
    ),
    MOVE_LEFT(
        title = "Move Left",
        description = "Moves pointer left across the display",
        defaultKeyCode = KeyEvent.KEYCODE_DPAD_LEFT,
        category = ActionCategory.MOVEMENT,
        isContinuous = true
    ),
    MOVE_RIGHT(
        title = "Move Right",
        description = "Moves pointer right across the display",
        defaultKeyCode = KeyEvent.KEYCODE_DPAD_RIGHT,
        category = ActionCategory.MOVEMENT,
        isContinuous = true
    ),
    PRIMARY_CLICK(
        title = "Primary Click (Left-click)",
        description = "Dispatches a tap gesture at the virtual pointer location",
        defaultKeyCode = KeyEvent.KEYCODE_ENTER,
        category = ActionCategory.CLICK
    ),
    SECONDARY_CLICK(
        title = "Secondary Click (Right-click)",
        description = "Dispatches long-press context click or back gesture where supported",
        defaultKeyCode = KeyEvent.KEYCODE_DEL,
        category = ActionCategory.CLICK
    ),
    TOGGLE_HOLD(
        title = "Hold / Drag Button",
        description = "Holds down pointer button for dragging and selection",
        defaultKeyCode = KeyEvent.KEYCODE_SPACE,
        category = ActionCategory.CLICK
    ),
    TOGGLE_CURSOR_MODE(
        title = "Toggle Cursor Mode",
        description = "Turns keyboard cursor control on/off to allow normal typing",
        defaultKeyCode = KeyEvent.KEYCODE_F6,
        category = ActionCategory.MODE
    ),
    SPEED_BOOST(
        title = "Speed Boost",
        description = "Temporarily accelerates cursor movement while held",
        defaultKeyCode = KeyEvent.KEYCODE_SHIFT_LEFT,
        category = ActionCategory.MODIFIER,
        isContinuous = true
    ),
    SCROLL_UP(
        title = "Scroll Up",
        description = "Dispatches an upward swipe gesture at cursor position",
        defaultKeyCode = KeyEvent.KEYCODE_PAGE_UP,
        category = ActionCategory.NAVIGATION
    ),
    SCROLL_DOWN(
        title = "Scroll Down",
        description = "Dispatches a downward swipe gesture at cursor position",
        defaultKeyCode = KeyEvent.KEYCODE_PAGE_DOWN,
        category = ActionCategory.NAVIGATION
    ),
    GLOBAL_BACK(
        title = "System Back",
        description = "Triggers Android system back action",
        defaultKeyCode = KeyEvent.KEYCODE_ESCAPE,
        category = ActionCategory.NAVIGATION
    ),
    GLOBAL_HOME(
        title = "System Home",
        description = "Triggers Android home screen action",
        defaultKeyCode = KeyEvent.KEYCODE_HOME,
        category = ActionCategory.NAVIGATION
    ),
    GLOBAL_RECENTS(
        title = "Recent Apps",
        description = "Opens the Android app switcher overview",
        defaultKeyCode = KeyEvent.KEYCODE_APP_SWITCH,
        category = ActionCategory.NAVIGATION
    );

    enum class ActionCategory(val displayName: String) {
        MOVEMENT("Movement"),
        CLICK("Click & Drag"),
        MODE("Mode Toggle"),
        MODIFIER("Modifiers"),
        NAVIGATION("System Navigation")
    }
}
