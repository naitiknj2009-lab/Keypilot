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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Mouse
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material.icons.filled.Warning
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
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.accessibility.KeyPilotAccessibilityService
import com.example.cursor.MovementEngine
import com.example.settings.AppSettings
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
import com.example.utils.DeviceCompatibilityHelper

@Composable
fun CursorTestScreen(
    settings: AppSettings,
    modifier: Modifier = Modifier
) {
    val service = KeyPilotAccessibilityService.activeServiceInstance
    val isRunning by KeyPilotAccessibilityService.isServiceRunning.collectAsState()
    val isCursorActive by KeyPilotAccessibilityService.isCursorModeActive.collectAsState()

    // Test Target state
    var targetAClicks by remember { mutableIntStateOf(0) }
    var targetBClicks by remember { mutableIntStateOf(0) }
    var bullseyeClicks by remember { mutableIntStateOf(0) }
    var testToggleState by remember { mutableStateOf(false) }
    var sliderValue by remember { mutableStateOf(0.5f) }
    var lastHitTarget by remember { mutableStateOf("None") }

    val scrollState = rememberScrollState()
    val compat = DeviceCompatibilityHelper.getCompatibilityReport()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Native vs Simulated Pointer Status Banner
        GlassCard(
            modifier = Modifier.fillMaxWidth(),
            cornerRadius = 14.dp,
            testTag = "native_support_status_card"
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.Info, contentDescription = null, tint = CyberCyan, modifier = Modifier.size(20.dp))
                        Text("System Pointer Diagnostic", color = TextPrimary, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                    }
                    StatusBadge(
                        label = "VIRTUAL OVERLAY",
                        isActive = true,
                        activeColor = CyberCyan
                    )
                }
                Text(
                    text = "STATUS: Virtual overlay pointer active with accessibility touch gesture click injection. Native OS mouse pointer injection is RESTRICTED by Android 10 security policy without root access.",
                    color = TextSecondary,
                    fontSize = 12.sp,
                    lineHeight = 16.sp
                )
            }
        }

        // Live Telemetry Bar
        GlassCard(modifier = Modifier.fillMaxWidth()) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                SectionHeader(
                    title = "Live Movement & Click Telemetry",
                    subtitle = "Real-time coordinates and dispatch status"
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    TelemetryTile(
                        label = "Service State",
                        value = if (isRunning) "Active" else "Disabled",
                        color = if (isRunning) CyberEmerald else CyberRose,
                        modifier = Modifier.weight(1f)
                    )
                    TelemetryTile(
                        label = "Cursor Mode",
                        value = if (isCursorActive) "ACTIVE (F6)" else "STANDBY",
                        color = if (isCursorActive) CyberEmerald else CyberAmber,
                        modifier = Modifier.weight(1f)
                    )
                    TelemetryTile(
                        label = "Base Velocity",
                        value = when (settings.speedPreset) {
                            "slow" -> "3 px/step"
                            "fast" -> "20 px/step"
                            "custom" -> "${settings.customSpeed.toInt()} px/step"
                            else -> "10 px/step"
                        },
                        color = CyberCyan,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // Interactive Target Testing Arena
        SectionHeader(
            title = "Interactive Target Arena",
            subtitle = "Steer your cursor over these targets and press Enter to click"
        )

        GlassCard(modifier = Modifier.fillMaxWidth()) {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Last Clicked: $lastHitTarget",
                        color = CyberCyan,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                    OutlinedButton(
                        onClick = {
                            targetAClicks = 0
                            targetBClicks = 0
                            bullseyeClicks = 0
                            lastHitTarget = "None"
                        },
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.height(34.dp)
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Reset Counts", fontSize = 11.sp)
                    }
                }

                // Target Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Target A
                    TargetCard(
                        title = "Target Alpha",
                        clicks = targetAClicks,
                        accentColor = CyberViolet,
                        onClick = {
                            targetAClicks++
                            lastHitTarget = "Target Alpha (Total: $targetAClicks)"
                        },
                        modifier = Modifier.weight(1f),
                        testTag = "test_target_alpha"
                    )

                    // Target B
                    TargetCard(
                        title = "Target Beta",
                        clicks = targetBClicks,
                        accentColor = CyberCyan,
                        onClick = {
                            targetBClicks++
                            lastHitTarget = "Target Beta (Total: $targetBClicks)"
                        },
                        modifier = Modifier.weight(1f),
                        testTag = "test_target_beta"
                    )
                }

                // Precision Bullseye Target
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(90.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(CyberEmerald.copy(alpha = 0.12f))
                        .border(1.5.dp, CyberEmerald.copy(alpha = 0.45f), RoundedCornerShape(14.dp))
                        .clickable {
                            bullseyeClicks++
                            lastHitTarget = "Precision Bullseye (Total: $bullseyeClicks)"
                        }
                        .testTag("test_bullseye_target")
                        .padding(12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "CENTER BULLSEYE TARGET",
                            color = CyberEmerald,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = "Clicks Captured: $bullseyeClicks",
                            color = TextPrimary,
                            fontSize = 12.sp
                        )
                    }
                }

                // Interactive Controls (Slider and Switch)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Interactive Slider Test", color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        Slider(
                            value = sliderValue,
                            onValueChange = {
                                sliderValue = it
                                lastHitTarget = "Slider (${(it * 100).toInt()}%)"
                            },
                            colors = SliderDefaults.colors(
                                thumbColor = CyberViolet,
                                activeTrackColor = CyberCyan
                            )
                        )
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Toggle Switch", color = TextPrimary, fontSize = 12.sp)
                        Spacer(modifier = Modifier.height(4.dp))
                        Switch(
                            checked = testToggleState,
                            onCheckedChange = {
                                testToggleState = it
                                lastHitTarget = if (it) "Switch (ON)" else "Switch (OFF)"
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = CyberEmerald
                            )
                        )
                    }
                }
            }
        }

        // Instructions for external apps testing
        GlassCard(modifier = Modifier.fillMaxWidth(), cornerRadius = 14.dp) {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("Testing Across External Apps", color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                Text(
                    text = "1. Press F6 on your keyboard to activate cursor mode.\n2. Switch to Chrome, YouTube, or your Home Screen.\n3. Steer with Arrow Keys across the display.\n4. Press Enter to click any link, icon, or button.\n5. Press Escape for Back, or F6 again to return to normal typing.",
                    color = TextSecondary,
                    fontSize = 12.sp,
                    lineHeight = 17.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))
    }
}

@Composable
private fun TelemetryTile(
    label: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(ObsidianSurfaceElevated)
            .border(1.dp, GlassBorder, RoundedCornerShape(10.dp))
            .padding(10.dp)
    ) {
        Column {
            Text(label, color = TextMuted, fontSize = 10.sp)
            Spacer(modifier = Modifier.height(2.dp))
            Text(value, color = color, fontSize = 13.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun TargetCard(
    title: String,
    clicks: Int,
    accentColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    testTag: String? = null
) {
    Box(
        modifier = modifier
            .then(if (testTag != null) Modifier.testTag(testTag) else Modifier)
            .height(86.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(accentColor.copy(alpha = 0.15f))
            .border(1.dp, accentColor.copy(alpha = 0.45f), RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .padding(12.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(title, color = accentColor, fontSize = 13.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(4.dp))
            Text("Hits: $clicks", color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.ExtraBold)
        }
    }
}
