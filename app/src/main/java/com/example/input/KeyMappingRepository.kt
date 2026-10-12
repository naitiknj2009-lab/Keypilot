package com.example.input

import android.content.Context
import android.content.SharedPreferences
import android.view.KeyEvent
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Manages physical key mappings, conflict resolution, presets, and persistence.
 */
class KeyMappingRepository(context: Context) {

    private val prefs: SharedPreferences =
        context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val _bindings = MutableStateFlow<Map<KeyAction, KeyBinding>>(loadBindings())
    val bindings: StateFlow<Map<KeyAction, KeyBinding>> = _bindings.asStateFlow()

    init {
        // Ensure default values exist in preferences
        if (prefs.all.isEmpty()) {
            resetToDefaults()
        }
    }

    /**
     * Loads bindings from shared preferences, falling back to KeyAction default keyCodes.
     */
    fun loadBindings(): Map<KeyAction, KeyBinding> {
        val map = mutableMapOf<KeyAction, KeyBinding>()
        for (action in KeyAction.values()) {
            val key = PREF_PREFIX + action.name
            val savedKeyCode = prefs.getInt(key, action.defaultKeyCode)
            val isEnabled = prefs.getBoolean(PREF_ENABLED_PREFIX + action.name, true)
            map[action] = KeyBinding(action = action, keyCode = savedKeyCode, isEnabled = isEnabled)
        }
        return map
    }

    /**
     * Finds the action bound to a given keyCode, if enabled.
     */
    fun getActionForKeyCode(keyCode: Int): KeyAction? {
        val current = _bindings.value
        for ((action, binding) in current) {
            if (binding.isEnabled && binding.keyCode == keyCode) {
                return action
            }
        }
        return null
    }

    /**
     * Checks if a keyCode is already assigned to a different action.
     */
    fun findConflict(actionToAssign: KeyAction, keyCode: Int): KeyAction? {
        val current = _bindings.value
        for ((existingAction, binding) in current) {
            if (existingAction != actionToAssign && binding.keyCode == keyCode && binding.isEnabled) {
                return existingAction
            }
        }
        return null
    }

    /**
     * Updates binding for an action.
     */
    fun updateBinding(action: KeyAction, keyCode: Int, isEnabled: Boolean = true) {
        prefs.edit()
            .putInt(PREF_PREFIX + action.name, keyCode)
            .putBoolean(PREF_ENABLED_PREFIX + action.name, isEnabled)
            .apply()

        val updated = _bindings.value.toMutableMap()
        updated[action] = KeyBinding(action, keyCode, isEnabled)
        _bindings.value = updated
    }

    /**
     * Resets all bindings to factory default arrow-key controls.
     */
    fun resetToDefaults() {
        val editor = prefs.edit()
        for (action in KeyAction.values()) {
            editor.putInt(PREF_PREFIX + action.name, action.defaultKeyCode)
            editor.putBoolean(PREF_ENABLED_PREFIX + action.name, true)
        }
        editor.apply()
        _bindings.value = loadBindings()
    }

    /**
     * Applies WASD navigation preset.
     */
    fun applyWasdPreset() {
        updateBinding(KeyAction.MOVE_UP, KeyEvent.KEYCODE_W)
        updateBinding(KeyAction.MOVE_LEFT, KeyEvent.KEYCODE_A)
        updateBinding(KeyAction.MOVE_DOWN, KeyEvent.KEYCODE_S)
        updateBinding(KeyAction.MOVE_RIGHT, KeyEvent.KEYCODE_D)
        updateBinding(KeyAction.PRIMARY_CLICK, KeyEvent.KEYCODE_SPACE)
        updateBinding(KeyAction.SECONDARY_CLICK, KeyEvent.KEYCODE_E)
        updateBinding(KeyAction.TOGGLE_CURSOR_MODE, KeyEvent.KEYCODE_F6)
        updateBinding(KeyAction.SPEED_BOOST, KeyEvent.KEYCODE_SHIFT_LEFT)
    }

    /**
     * Applies IJKL navigation preset.
     */
    fun applyIjklPreset() {
        updateBinding(KeyAction.MOVE_UP, KeyEvent.KEYCODE_I)
        updateBinding(KeyAction.MOVE_LEFT, KeyEvent.KEYCODE_J)
        updateBinding(KeyAction.MOVE_DOWN, KeyEvent.KEYCODE_K)
        updateBinding(KeyAction.MOVE_RIGHT, KeyEvent.KEYCODE_L)
        updateBinding(KeyAction.PRIMARY_CLICK, KeyEvent.KEYCODE_ENTER)
        updateBinding(KeyAction.SECONDARY_CLICK, KeyEvent.KEYCODE_U)
        updateBinding(KeyAction.TOGGLE_CURSOR_MODE, KeyEvent.KEYCODE_F6)
        updateBinding(KeyAction.SPEED_BOOST, KeyEvent.KEYCODE_SHIFT_LEFT)
    }

    /**
     * Applies Numpad navigation preset (8, 4, 2, 6).
     */
    fun applyNumpadPreset() {
        updateBinding(KeyAction.MOVE_UP, KeyEvent.KEYCODE_NUMPAD_8)
        updateBinding(KeyAction.MOVE_LEFT, KeyEvent.KEYCODE_NUMPAD_4)
        updateBinding(KeyAction.MOVE_DOWN, KeyEvent.KEYCODE_NUMPAD_2)
        updateBinding(KeyAction.MOVE_RIGHT, KeyEvent.KEYCODE_NUMPAD_6)
        updateBinding(KeyAction.PRIMARY_CLICK, KeyEvent.KEYCODE_NUMPAD_ENTER)
        updateBinding(KeyAction.SECONDARY_CLICK, KeyEvent.KEYCODE_NUMPAD_DOT)
        updateBinding(KeyAction.TOGGLE_CURSOR_MODE, KeyEvent.KEYCODE_NUMPAD_0)
        updateBinding(KeyAction.SPEED_BOOST, KeyEvent.KEYCODE_NUMPAD_ADD)
    }

    companion object {
        private const val PREFS_NAME = "keypilot_key_mappings"
        private const val PREF_PREFIX = "action_key_"
        private const val PREF_ENABLED_PREFIX = "action_enabled_"

        @Volatile
        private var instance: KeyMappingRepository? = null

        fun getInstance(context: Context): KeyMappingRepository {
            return instance ?: synchronized(this) {
                instance ?: KeyMappingRepository(context.applicationContext).also { instance = it }
            }
        }
    }
}
