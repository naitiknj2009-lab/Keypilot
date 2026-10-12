package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.accessibility.AccessibilityManagerHelper
import com.example.cursor.CursorStyle
import com.example.cursor.EdgeHandling
import com.example.settings.AppSettings
import com.example.settings.SettingsRepository
import com.example.settings.ThemeMode
import com.example.ui.components.GlassCard
import com.example.ui.components.SectionHeader
import com.example.ui.theme.CyberAmber
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.CyberEmerald
import com.example.ui.theme.CyberRose
import com.example.ui.theme.CyberViolet
import com.example.ui.theme.GlassBorder
import com.example.ui.theme.ObsidianSurfaceElevated
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun SettingsScreen(
    settingsRepo: SettingsRepository,
    modifier: Modifier = Modifier
) {
    val settings by settingsRepo.settings.collectAsState()
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        // Pointer Velocity & Physics
        SectionHeader(
            title = "Pointer Velocity & Physics",
            subtitle = "Calibrate movement steps and smooth acceleration",
            icon = Icons.Default.Speed
        )

        GlassCard(modifier = Modifier.fillMaxWidth()) {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Movement Speed", color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                    Text(
                        "${settings.customSpeed.toInt()} px / step",
                        color = CyberCyan,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Slider(
                    value = settings.customSpeed,
                    onValueChange = { newVal ->
                        settingsRepo.updateSettings {
                            it.copy(
                                customSpeed = newVal,
                                speedPreset = "custom"
                            )
                        }
                    },
                    valueRange = 2f..36f,
                    colors = SliderDefaults.colors(
                        thumbColor = CyberCyan,
                        activeTrackColor = CyberViolet
                    ),
                    modifier = Modifier.testTag("speed_slider")
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    PresetButton(
                        label = "Slow (3px)",
                        isSelected = settings.speedPreset == "slow",
                        onClick = {
                            settingsRepo.updateSettings { it.copy(speedPreset = "slow", customSpeed = 3f) }
                        },
                        modifier = Modifier.weight(1f)
                    )
                    PresetButton(
                        label = "Normal (10px)",
                        isSelected = settings.speedPreset == "normal",
                        onClick = {
                            settingsRepo.updateSettings { it.copy(speedPreset = "normal", customSpeed = 10f) }
                        },
                        modifier = Modifier.weight(1f)
                    )
                    PresetButton(
                        label = "Fast (20px)",
                        isSelected = settings.speedPreset == "fast",
                        onClick = {
                            settingsRepo.updateSettings { it.copy(speedPreset = "fast", customSpeed = 20f) }
                        },
                        modifier = Modifier.weight(1f)
                    )
                }

                // Acceleration Toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Dynamic Velocity Acceleration", color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                        Text("Smoothly accelerates cursor up to 3x when holding arrow keys", color = TextSecondary, fontSize = 11.sp)
                    }
                    Switch(
                        checked = settings.accelerationEnabled,
                        onCheckedChange = { checked ->
                            settingsRepo.updateSettings { it.copy(accelerationEnabled = checked) }
                        },
                        colors = SwitchDefaults.colors(checkedTrackColor = CyberEmerald),
                        modifier = Modifier.testTag("acceleration_switch")
                    )
                }

                // Edge Handling
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Display Edge Handling", color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                        Text(settings.edgeHandling.description, color = TextSecondary, fontSize = 11.sp)
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        EdgeChip(
                            label = "Clamp",
                            isSelected = settings.edgeHandling == EdgeHandling.CLAMP,
                            onClick = { settingsRepo.updateSettings { it.copy(edgeHandling = EdgeHandling.CLAMP) } }
                        )
                        EdgeChip(
                            label = "Wrap",
                            isSelected = settings.edgeHandling == EdgeHandling.WRAP,
                            onClick = { settingsRepo.updateSettings { it.copy(edgeHandling = EdgeHandling.WRAP) } }
                        )
                    }
                }
            }
        }

        // Pointer Appearance
        SectionHeader(
            title = "Virtual Cursor Appearance",
            subtitle = "Style, dimensions, and theme accents",
            icon = Icons.Default.Palette
        )

        GlassCard(modifier = Modifier.fillMaxWidth()) {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Text("Cursor Pointer Style", color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    for (style in CursorStyle.values()) {
                        StyleChip(
                            label = style.displayName,
                            isSelected = settings.cursorStyle == style,
                            onClick = { settingsRepo.updateSettings { it.copy(cursorStyle = style) } },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                // Size Slider
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Pointer Size", color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                    Text("${settings.cursorSizeDp} dp", color = CyberViolet, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }

                Slider(
                    value = settings.cursorSizeDp.toFloat(),
                    onValueChange = { newVal ->
                        settingsRepo.updateSettings { it.copy(cursorSizeDp = newVal.toInt()) }
                    },
                    valueRange = 18f..56f,
                    colors = SliderDefaults.colors(
                        thumbColor = CyberViolet,
                        activeTrackColor = CyberCyan
                    ),
                    modifier = Modifier.testTag("size_slider")
                )

                // Accent Color Palette
                Text("Glow Accent Color", color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    val colors = listOf(
                        0xFF8B5CF6L to "Violet",
                        0xFF06B6D4L to "Cyan",
                        0xFF10B981L to "Emerald",
                        0xFFF59E0BL to "Amber",
                        0xFFEF4444L to "Rose",
                        0xFFFFFFFFL to "White"
                    )

                    for ((colorHex, _) in colors) {
                        val isSelected = settings.cursorColor == colorHex
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Color(colorHex))
                                .border(
                                    width = if (isSelected) 3.dp else 1.dp,
                                    color = if (isSelected) Color.White else GlassBorder,
                                    shape = CircleShape
                                )
                                .clickable {
                                    settingsRepo.updateSettings { it.copy(cursorColor = colorHex) }
                                }
                        )
                    }
                }
            }
        }

        // Haptic & Theme Preferences
        SectionHeader(
            title = "Feedback & Theme",
            subtitle = "Haptics and display mode",
            icon = Icons.Default.Tune
        )

        GlassCard(modifier = Modifier.fillMaxWidth()) {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Haptic Click Feedback", color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                        Text("Vibrates briefly upon pointer click and mode toggle", color = TextSecondary, fontSize = 11.sp)
                    }
                    Switch(
                        checked = settings.vibrationFeedback,
                        onCheckedChange = { checked ->
                            settingsRepo.updateSettings { it.copy(vibrationFeedback = checked) }
                        },
                        colors = SwitchDefaults.colors(checkedTrackColor = CyberEmerald),
                        modifier = Modifier.testTag("haptic_switch")
                    )
                }

                // Theme Mode Selector
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("App Theme", color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                        Text(settings.themeMode.displayName, color = TextSecondary, fontSize = 11.sp)
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        for (mode in ThemeMode.values()) {
                            EdgeChip(
                                label = when (mode) {
                                    ThemeMode.DARK -> "Dark"
                                    ThemeMode.LIGHT -> "Light"
                                    ThemeMode.SYSTEM -> "Auto"
                                },
                                isSelected = settings.themeMode == mode,
                                onClick = { settingsRepo.updateSettings { it.copy(themeMode = mode) } }
                            )
                        }
                    }
                }
            }
        }

        // Lenovo Tab M10 Troubleshooting & Quick Links
        GlassCard(modifier = Modifier.fillMaxWidth(), cornerRadius = 14.dp) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Lenovo Tab M10 (Android 10) Help", color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                Text(
                    text = "• Arrow keys not moving pointer? Verify Accessibility service is switched ON and toggle F6.\n• Keys typing into text boxes? Press F6 to enter cursor control mode.\n• USB OTG not detected? Check OTG connection in Android tablet settings.\n• Cursor disappearing on rotation? Display bounds are auto-synced on orientation change.",
                    color = TextSecondary,
                    fontSize = 11.sp,
                    lineHeight = 16.sp
                )

                Spacer(modifier = Modifier.height(4.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = { AccessibilityManagerHelper.openAccessibilitySettings(context) },
                        colors = ButtonDefaults.buttonColors(containerColor = CyberViolet),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.height(34.dp)
                    ) {
                        Text("Open Accessibility", fontSize = 11.sp)
                    }
                    OutlinedButton(
                        onClick = { AccessibilityManagerHelper.openKeyboardSettings(context) },
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.height(34.dp)
                    ) {
                        Text("Keyboard Settings", fontSize = 11.sp)
                    }
                }
            }
        }

        // Reset Settings Button
        OutlinedButton(
            onClick = { settingsRepo.resetToDefaults() },
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .testTag("reset_settings_button"),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(Icons.Default.Refresh, contentDescription = null, tint = CyberRose, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Reset All Settings to Factory Defaults", color = CyberRose, fontSize = 13.sp)
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun PresetButton(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(if (isSelected) CyberCyan.copy(alpha = 0.2f) else ObsidianSurfaceElevated)
            .border(1.dp, if (isSelected) CyberCyan else GlassBorder, RoundedCornerShape(8.dp))
            .clickable { onClick() }
            .padding(vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = if (isSelected) Color.White else TextSecondary,
            fontSize = 11.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
        )
    }
}

@Composable
private fun EdgeChip(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(if (isSelected) CyberViolet.copy(alpha = 0.25f) else ObsidianSurfaceElevated)
            .border(1.dp, if (isSelected) CyberViolet else GlassBorder, RoundedCornerShape(6.dp))
            .clickable { onClick() }
            .padding(horizontal = 10.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = if (isSelected) Color.White else TextSecondary,
            fontSize = 11.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
        )
    }
}

@Composable
private fun StyleChip(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(if (isSelected) CyberViolet.copy(alpha = 0.25f) else ObsidianSurfaceElevated)
            .border(1.dp, if (isSelected) CyberViolet else GlassBorder, RoundedCornerShape(8.dp))
            .clickable { onClick() }
            .padding(vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = if (isSelected) Color.White else TextSecondary,
            fontSize = 10.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
        )
    }
}
