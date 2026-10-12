package com.example.settings

import com.example.cursor.CursorStyle
import com.example.cursor.EdgeHandling

enum class ThemeMode(val displayName: String) {
    SYSTEM("System Default"),
    DARK("Cyber Obsidian (Dark)"),
    LIGHT("Clean Crystal (Light)")
}

data class AppSettings(
    val speedPreset: String = "normal", // "slow", "normal", "fast", "custom"
    val customSpeed: Float = 10f,
    val accelerationEnabled: Boolean = true,
    val edgeHandling: EdgeHandling = EdgeHandling.CLAMP,
    val cursorStyle: CursorStyle = CursorStyle.ARROW,
    val cursorSizeDp: Int = 32,
    val cursorColor: Long = 0xFF8B5CF6, // Cyber Violet
    val cursorTrailEnabled: Boolean = false,
    val vibrationFeedback: Boolean = true,
    val showCoordinateBadge: Boolean = true,
    val themeMode: ThemeMode = ThemeMode.DARK,
    val hasCompletedOnboarding: Boolean = false
)
