package com.example.settings

import android.content.Context
import android.content.SharedPreferences
import com.example.cursor.CursorStyle
import com.example.cursor.EdgeHandling
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Manages user preferences and settings persistence.
 */
class SettingsRepository(context: Context) {

    private val prefs: SharedPreferences =
        context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val _settings = MutableStateFlow(loadSettings())
    val settings: StateFlow<AppSettings> = _settings.asStateFlow()

    fun loadSettings(): AppSettings {
        val speedPreset = prefs.getString(KEY_SPEED_PRESET, "normal") ?: "normal"
        val customSpeed = prefs.getFloat(KEY_CUSTOM_SPEED, 10f)
        val accel = prefs.getBoolean(KEY_ACCEL, true)
        val edgeStr = prefs.getString(KEY_EDGE, EdgeHandling.CLAMP.name) ?: EdgeHandling.CLAMP.name
        val edge = try { EdgeHandling.valueOf(edgeStr) } catch (e: Exception) { EdgeHandling.CLAMP }

        val styleStr = prefs.getString(KEY_CURSOR_STYLE, CursorStyle.ARROW.name) ?: CursorStyle.ARROW.name
        val style = try { CursorStyle.valueOf(styleStr) } catch (e: Exception) { CursorStyle.ARROW }

        val sizeDp = prefs.getInt(KEY_CURSOR_SIZE, 32)
        val colorHex = prefs.getLong(KEY_CURSOR_COLOR, 0xFF8B5CF6)
        val trail = prefs.getBoolean(KEY_CURSOR_TRAIL, false)
        val vibe = prefs.getBoolean(KEY_VIBE, true)
        val coords = prefs.getBoolean(KEY_COORDS, true)

        val themeStr = prefs.getString(KEY_THEME, ThemeMode.DARK.name) ?: ThemeMode.DARK.name
        val theme = try { ThemeMode.valueOf(themeStr) } catch (e: Exception) { ThemeMode.DARK }

        val onboarded = prefs.getBoolean(KEY_ONBOARDED, false)

        return AppSettings(
            speedPreset = speedPreset,
            customSpeed = customSpeed,
            accelerationEnabled = accel,
            edgeHandling = edge,
            cursorStyle = style,
            cursorSizeDp = sizeDp,
            cursorColor = colorHex,
            cursorTrailEnabled = trail,
            vibrationFeedback = vibe,
            showCoordinateBadge = coords,
            themeMode = theme,
            hasCompletedOnboarding = onboarded
        )
    }

    fun updateSettings(transform: (AppSettings) -> AppSettings) {
        val current = _settings.value
        val updated = transform(current)
        saveSettings(updated)
        _settings.value = updated
    }

    private fun saveSettings(s: AppSettings) {
        prefs.edit()
            .putString(KEY_SPEED_PRESET, s.speedPreset)
            .putFloat(KEY_CUSTOM_SPEED, s.customSpeed)
            .putBoolean(KEY_ACCEL, s.accelerationEnabled)
            .putString(KEY_EDGE, s.edgeHandling.name)
            .putString(KEY_CURSOR_STYLE, s.cursorStyle.name)
            .putInt(KEY_CURSOR_SIZE, s.cursorSizeDp)
            .putLong(KEY_CURSOR_COLOR, s.cursorColor)
            .putBoolean(KEY_CURSOR_TRAIL, s.cursorTrailEnabled)
            .putBoolean(KEY_VIBE, s.vibrationFeedback)
            .putBoolean(KEY_COORDS, s.showCoordinateBadge)
            .putString(KEY_THEME, s.themeMode.name)
            .putBoolean(KEY_ONBOARDED, s.hasCompletedOnboarding)
            .apply()
    }

    fun resetToDefaults() {
        prefs.edit().clear().apply()
        _settings.value = AppSettings()
    }

    companion object {
        private const val PREFS_NAME = "keypilot_app_settings"
        private const val KEY_SPEED_PRESET = "speed_preset"
        private const val KEY_CUSTOM_SPEED = "custom_speed"
        private const val KEY_ACCEL = "acceleration_enabled"
        private const val KEY_EDGE = "edge_handling"
        private const val KEY_CURSOR_STYLE = "cursor_style"
        private const val KEY_CURSOR_SIZE = "cursor_size"
        private const val KEY_CURSOR_COLOR = "cursor_color"
        private const val KEY_CURSOR_TRAIL = "cursor_trail"
        private const val KEY_VIBE = "vibration_feedback"
        private const val KEY_COORDS = "show_coords"
        private const val KEY_THEME = "theme_mode"
        private const val KEY_ONBOARDED = "has_completed_onboarding"

        @Volatile
        private var instance: SettingsRepository? = null

        fun getInstance(context: Context): SettingsRepository {
            return instance ?: synchronized(this) {
                instance ?: SettingsRepository(context.applicationContext).also { instance = it }
            }
        }
    }
}
