package com.example.ui.screens

import android.content.Context
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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Keyboard
import androidx.compose.material.icons.filled.Mouse
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.example.accessibility.KeyPilotAccessibilityService
import com.example.input.PhysicalKeyboardDetector
import com.example.settings.AppSettings
import com.example.settings.SettingsRepository
import com.example.ui.components.GlassCard
import com.example.ui.components.SectionHeader
import com.example.ui.components.StatusBadge
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
fun DashboardScreen(
    settings: AppSettings,
    settingsRepo: SettingsRepository,
    keyboardDetector: PhysicalKeyboardDetector,
    onNavigateToTest: () -> Unit,
    onNavigateToSetup: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToGuide: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val keyboardInfo by keyboardDetector.deviceState.collectAsState()
    val isServiceRunning by KeyPilotAccessibilityService.isServiceRunning.collectAsState()
    val isCursorModeActive by KeyPilotAccessibilityService.isCursorModeActive.collectAsState()

    var isAccessibilityEnabled by remember {
        mutableStateOf(AccessibilityManagerHelper.isAccessibilityServiceEnabled(context))
    }
    var isOverlayGranted by remember {
        mutableStateOf(AccessibilityManagerHelper.isOverlayPermissionGranted(context))
    }

    // Refresh state periodically or on resume
    LaunchedEffect(Unit) {
        keyboardDetector.refresh()
        isAccessibilityEnabled = AccessibilityManagerHelper.isAccessibilityServiceEnabled(context)
        isOverlayGranted = AccessibilityManagerHelper.isOverlayPermissionGranted(context)
    }

    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        // Top Master Toggle Card
        GlassCard(
            modifier = Modifier.fillMaxWidth(),
            cornerRadius = 20.dp,
            testTag = "master_toggle_card"
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Cursor Control Mode",
                                color = TextPrimary,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .clip(CircleShape)
                                    .background(if (isCursorModeActive) CyberEmerald else CyberRose)
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (isCursorModeActive)
                                "ACTIVE — Arrow keys move pointer, Enter clicks. Ordinary typing paused."
                            else
                                "STANDBY — Press F6 or flip switch to activate pointer control.",
                            color = if (isCursorModeActive) CyberEmerald else TextSecondary,
                            fontSize = 13.sp,
                            lineHeight = 18.sp
                        )
                    }

                    Switch(
                        checked = isCursorModeActive,
                        onCheckedChange = { desired ->
                            if (!isAccessibilityEnabled) {
                                AccessibilityManagerHelper.openAccessibilitySettings(context)
                            } else {
                                KeyPilotAccessibilityService.activeServiceInstance?.toggleCursorMode(desired)
                            }
                        },
                        modifier = Modifier.testTag("cursor_mode_switch"),
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = CyberEmerald,
                            uncheckedThumbColor = TextMuted,
                            uncheckedTrackColor = ObsidianSurfaceElevated
                        )
                    )
                }

                if (!isAccessibilityEnabled) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(CyberAmber.copy(alpha = 0.12f))
                            .border(1.dp, CyberAmber.copy(alpha = 0.4f), RoundedCornerShape(10.dp))
                            .padding(12.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(Icons.Default.Warning, contentDescription = null, tint = CyberAmber, modifier = Modifier.size(20.dp))
                            Text(
                                text = "Accessibility service is currently disabled. Tap 'Enable in Settings' below.",
                                color = TextPrimary,
                                fontSize = 12.sp,
                                modifier = Modifier.weight(1f)
                            )
                            Button(
                                onClick = { AccessibilityManagerHelper.openAccessibilitySettings(context) },
                                colors = ButtonDefaults.buttonColors(containerColor = CyberAmber, contentColor = Color.Black),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.testTag("enable_accessibility_button")
                            ) {
                                Text("Enable", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }

        // System Diagnostic Grid
        SectionHeader(
            title = "Hardware & Service Telemetry",
            subtitle = "Live connection diagnostics for Lenovo Tab M10",
            icon = Icons.Default.FlashOn
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Service Status Card
            GlassCard(
                modifier = Modifier.weight(1f),
                cornerRadius = 14.dp,
                onClick = { AccessibilityManagerHelper.openAccessibilitySettings(context) },
                testTag = "accessibility_status_card"
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.TouchApp, contentDescription = null, tint = CyberViolet, modifier = Modifier.size(20.dp))
                        StatusBadge(
                            label = if (isAccessibilityEnabled) "RUNNING" else "OFF",
                            isActive = isAccessibilityEnabled
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Text("Accessibility Engine", color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        if (isAccessibilityEnabled) "Intercepting keys" else "Tap to enable service",
                        color = TextSecondary,
                        fontSize = 11.sp
                    )
                }
            }

            // Keyboard Detection Card
            GlassCard(
                modifier = Modifier.weight(1f),
                cornerRadius = 14.dp,
                onClick = { keyboardDetector.refresh() },
                testTag = "keyboard_status_card"
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Keyboard, contentDescription = null, tint = CyberCyan, modifier = Modifier.size(20.dp))
                        StatusBadge(
                            label = if (keyboardInfo.isConnected) "CONNECTED" else "NOT FOUND",
                            isActive = keyboardInfo.isConnected,
                            activeColor = CyberCyan,
                            inactiveColor = CyberAmber
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = if (keyboardInfo.isConnected) keyboardInfo.name else "Physical Keyboard",
                        color = TextPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = if (keyboardInfo.isConnected) keyboardInfo.type else "Plug in USB OTG or pair BT",
                        color = TextSecondary,
                        fontSize = 11.sp
                    )
                }
            }
        }

        // Quick Movement Speed Switcher
        GlassCard(
            modifier = Modifier.fillMaxWidth(),
            cornerRadius = 16.dp
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.Speed, contentDescription = null, tint = CyberCyan, modifier = Modifier.size(20.dp))
                        Text("Pointer Velocity Preset", color = TextPrimary, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                    }
                    Text(
                        text = "${settings.speedPreset.uppercase()} (${when (settings.speedPreset) {
                            "slow" -> "3 px"
                            "fast" -> "20 px"
                            "custom" -> "${settings.customSpeed.toInt()} px"
                            else -> "10 px"
                        }})",
                        color = CyberCyan,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    SpeedPresetChip(
                        label = "Slow (3px)",
                        isSelected = settings.speedPreset == "slow",
                        onClick = { settingsRepo.updateSettings { it.copy(speedPreset = "slow") } },
                        modifier = Modifier.weight(1f)
                    )
                    SpeedPresetChip(
                        label = "Normal (10px)",
                        isSelected = settings.speedPreset == "normal",
                        onClick = { settingsRepo.updateSettings { it.copy(speedPreset = "normal") } },
                        modifier = Modifier.weight(1f)
                    )
                    SpeedPresetChip(
                        label = "Fast (20px)",
                        isSelected = settings.speedPreset == "fast",
                        onClick = { settingsRepo.updateSettings { it.copy(speedPreset = "fast") } },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // Quick Action Tiles
        SectionHeader(
            title = "Quick Tools & Navigation",
            subtitle = "Test and fine-tune your pointer experience"
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            ActionTile(
                title = "Cursor Test Arena",
                subtitle = "Click targets & test precision",
                icon = Icons.Default.PlayArrow,
                accentColor = CyberEmerald,
                onClick = onNavigateToTest,
                modifier = Modifier.weight(1f),
                testTag = "nav_test_arena_tile"
            )

            ActionTile(
                title = "Keyboard Setup",
                subtitle = "Rebind keys & presets",
                icon = Icons.Default.Keyboard,
                accentColor = CyberViolet,
                onClick = onNavigateToSetup,
                modifier = Modifier.weight(1f),
                testTag = "nav_key_setup_tile"
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            ActionTile(
                title = "Settings & Physics",
                subtitle = "Acceleration, size & theme",
                icon = Icons.Default.Settings,
                accentColor = CyberCyan,
                onClick = onNavigateToSettings,
                modifier = Modifier.weight(1f),
                testTag = "nav_settings_tile"
            )

            ActionTile(
                title = "Setup Guide & FAQ",
                subtitle = "Android 10 Lenovo guide",
                icon = Icons.Default.Info,
                accentColor = CyberAmber,
                onClick = onNavigateToGuide,
                modifier = Modifier.weight(1f),
                testTag = "nav_guide_tile"
            )
        }

        // Platform Transparency Card
        GlassCard(
            modifier = Modifier.fillMaxWidth(),
            cornerRadius = 14.dp
        ) {
            Row(
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Icon(Icons.Default.Info, contentDescription = null, tint = CyberCyan, modifier = Modifier.size(20.dp))
                Column {
                    Text(
                        text = "Native Mouse vs Virtual Overlay Distinction",
                        color = TextPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Android 10 does not allow unprivileged apps to move the operating system's internal hardware mouse pointer without root access. KeyPilot achieves system-wide control by projecting a hardware-accelerated overlay and translating Enter to touch taps via dispatchGesture().",
                        color = TextSecondary,
                        fontSize = 11.sp,
                        lineHeight = 15.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))
    }
}

@Composable
private fun SpeedPresetChip(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val bg = if (isSelected) CyberCyan.copy(alpha = 0.2f) else ObsidianSurfaceElevated
    val border = if (isSelected) CyberCyan else GlassBorder

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(bg)
            .border(1.dp, border, RoundedCornerShape(8.dp))
            .clickable { onClick() }
            .padding(vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = if (isSelected) Color.White else TextSecondary,
            fontSize = 12.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
        )
    }
}

@Composable
private fun ActionTile(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    accentColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    testTag: String? = null
) {
    GlassCard(
        modifier = modifier,
        cornerRadius = 14.dp,
        onClick = onClick,
        testTag = testTag
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(accentColor.copy(alpha = 0.15f))
                    .border(1.dp, accentColor.copy(alpha = 0.35f), RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = accentColor, modifier = Modifier.size(18.dp))
            }
            Text(title, color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            Text(subtitle, color = TextSecondary, fontSize = 11.sp, lineHeight = 14.sp)
        }
    }
}
