package com.example.ui.screens

import android.view.KeyEvent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Keyboard
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.input.KeyAction
import com.example.input.KeyBinding
import com.example.input.KeyMappingRepository
import com.example.ui.components.GlassCard
import com.example.ui.components.KeyCapsule
import com.example.ui.components.SectionHeader
import com.example.ui.theme.CyberAmber
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.CyberEmerald
import com.example.ui.theme.CyberRose
import com.example.ui.theme.CyberViolet
import com.example.ui.theme.GlassBorder
import com.example.ui.theme.ObsidianSurface
import com.example.ui.theme.ObsidianSurfaceElevated
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun KeyboardSetupScreen(
    keyRepo: KeyMappingRepository,
    modifier: Modifier = Modifier
) {
    val bindings by keyRepo.bindings.collectAsState()

    var actionBeingRebound by remember { mutableStateOf<KeyAction?>(null) }
    var detectedKeyCodeInRebind by remember { mutableStateOf<Int?>(null) }
    var conflictAction by remember { mutableStateOf<KeyAction?>(null) }

    // Live Key Tester state
    var lastTestedKeyName by remember { mutableStateOf("None (press any key to test)") }
    var lastTestedKeyCode by remember { mutableStateOf(0) }
    var lastTestedAction by remember { mutableStateOf<String?>(null) }

    val testFocusRequester = remember { FocusRequester() }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Visual Directional Pad Overview
        item {
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Visual Directional Controls",
                        color = TextPrimary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(14.dp))

                    // D-Pad Cross Layout
                    val upBinding = bindings[KeyAction.MOVE_UP]?.keyDisplayName ?: "Up"
                    val downBinding = bindings[KeyAction.MOVE_DOWN]?.keyDisplayName ?: "Down"
                    val leftBinding = bindings[KeyAction.MOVE_LEFT]?.keyDisplayName ?: "Left"
                    val rightBinding = bindings[KeyAction.MOVE_RIGHT]?.keyDisplayName ?: "Right"
                    val clickBinding = bindings[KeyAction.PRIMARY_CLICK]?.keyDisplayName ?: "Enter"

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        KeyCapsule(
                            keyLabel = upBinding,
                            isAccent = true,
                            onClick = { actionBeingRebound = KeyAction.MOVE_UP },
                            testTag = "visual_key_up"
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            KeyCapsule(
                                keyLabel = leftBinding,
                                isAccent = true,
                                onClick = { actionBeingRebound = KeyAction.MOVE_LEFT },
                                testTag = "visual_key_left"
                            )
                            KeyCapsule(
                                keyLabel = "Click ($clickBinding)",
                                onClick = { actionBeingRebound = KeyAction.PRIMARY_CLICK },
                                testTag = "visual_key_click"
                            )
                            KeyCapsule(
                                keyLabel = rightBinding,
                                isAccent = true,
                                onClick = { actionBeingRebound = KeyAction.MOVE_RIGHT },
                                testTag = "visual_key_right"
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        KeyCapsule(
                            keyLabel = downBinding,
                            isAccent = true,
                            onClick = { actionBeingRebound = KeyAction.MOVE_DOWN },
                            testTag = "visual_key_down"
                        )
                    }
                }
            }
        }

        // Preset Selector Bar
        item {
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Navigation Layout Presets",
                        color = TextPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        PresetButton(
                            title = "Arrow Keys",
                            subtitle = "Default",
                            onClick = { keyRepo.resetToDefaults() },
                            modifier = Modifier.weight(1f),
                            testTag = "preset_default_button"
                        )
                        PresetButton(
                            title = "WASD",
                            subtitle = "Gamer",
                            onClick = { keyRepo.applyWasdPreset() },
                            modifier = Modifier.weight(1f),
                            testTag = "preset_wasd_button"
                        )
                        PresetButton(
                            title = "IJKL",
                            subtitle = "Right-Hand",
                            onClick = { keyRepo.applyIjklPreset() },
                            modifier = Modifier.weight(1f),
                            testTag = "preset_ijkl_button"
                        )
                        PresetButton(
                            title = "Numpad",
                            subtitle = "8,4,2,6",
                            onClick = { keyRepo.applyNumpadPreset() },
                            modifier = Modifier.weight(1f),
                            testTag = "preset_numpad_button"
                        )
                    }
                }
            }
        }

        // Live Key Detection & Testing Sandbox
        item {
            GlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(testFocusRequester)
                    .focusable()
                    .onKeyEvent { keyEvent ->
                        val code = keyEvent.nativeKeyEvent.keyCode
                        lastTestedKeyCode = code
                        lastTestedKeyName = KeyBinding.getKeyDisplayName(code)
                        val boundAction = keyRepo.getActionForKeyCode(code)
                        lastTestedAction = boundAction?.title ?: "Not Bound"
                        true
                    },
                cornerRadius = 14.dp,
                onClick = { testFocusRequester.requestFocus() },
                testTag = "key_detector_box"
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Keyboard, contentDescription = null, tint = CyberCyan, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Interactive Physical Key Detector", color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        }
                        Text("Tap to focus", color = CyberCyan, fontSize = 11.sp)
                    }
                    Text(
                        text = "Press any key on your USB/BT keyboard to verify immediate hardware capture.",
                        color = TextSecondary,
                        fontSize = 11.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(ObsidianSurfaceElevated)
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Key: $lastTestedKeyName", color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                        Text(
                            text = "Action: ${lastTestedAction ?: "Tap to test"}",
                            color = if (lastTestedAction != null && lastTestedAction != "Not Bound") CyberEmerald else TextMuted,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }

        // Header for list
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                SectionHeader(
                    title = "Configured Key Mappings",
                    subtitle = "Tap any row to reassign to a new key"
                )
            }
        }

        // All Key Action Rows
        items(KeyAction.values()) { action ->
            val binding = bindings[action]
            val keyName = binding?.keyDisplayName ?: KeyBinding.getKeyDisplayName(action.defaultKeyCode)

            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                cornerRadius = 12.dp,
                onClick = {
                    actionBeingRebound = action
                    detectedKeyCodeInRebind = null
                    conflictAction = null
                },
                testTag = "key_row_${action.name}"
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = action.title,
                            color = TextPrimary,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = action.description,
                            color = TextSecondary,
                            fontSize = 11.sp,
                            lineHeight = 15.sp
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    KeyCapsule(
                        keyLabel = keyName,
                        isAccent = action.category == KeyAction.ActionCategory.MOVEMENT || action == KeyAction.TOGGLE_CURSOR_MODE
                    )
                }
            }
        }

        // Reset to Factory Defaults Button
        item {
            OutlinedButton(
                onClick = { keyRepo.resetToDefaults() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("reset_keys_button"),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Refresh, contentDescription = null, tint = CyberRose, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Reset All Key Mappings to Factory Defaults", color = CyberRose, fontSize = 13.sp)
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }

    // Rebind Modal Dialog
    if (actionBeingRebound != null) {
        val targetAction = actionBeingRebound!!
        val dialogFocusRequester = remember { FocusRequester() }

        AlertDialog(
            onDismissRequest = { actionBeingRebound = null },
            title = {
                Text(
                    text = "Rebind \"${targetAction.title}\"",
                    color = TextPrimary,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(dialogFocusRequester)
                        .focusable()
                        .onKeyEvent { event ->
                            val code = event.nativeKeyEvent.keyCode
                            detectedKeyCodeInRebind = code
                            conflictAction = keyRepo.findConflict(targetAction, code)
                            true
                        },
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    LaunchedEffect(Unit) {
                        dialogFocusRequester.requestFocus()
                    }

                    Text(
                        text = "Press any key on your physical keyboard to assign it:",
                        color = TextSecondary,
                        fontSize = 13.sp
                    )

                    val code = detectedKeyCodeInRebind
                    if (code != null) {
                        KeyCapsule(
                            keyLabel = KeyBinding.getKeyDisplayName(code),
                            isAccent = true,
                            modifier = Modifier.padding(8.dp)
                        )

                        if (conflictAction != null) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(CyberAmber.copy(alpha = 0.15f))
                                    .padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(Icons.Default.Warning, contentDescription = null, tint = CyberAmber, modifier = Modifier.size(18.dp))
                                Text(
                                    text = "Conflict: Currently assigned to ${conflictAction?.title}. Saving will reassign this key.",
                                    color = CyberAmber,
                                    fontSize = 11.sp
                                )
                            }
                        }
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(64.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(ObsidianSurfaceElevated)
                                .border(1.dp, GlassBorder, RoundedCornerShape(10.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("Waiting for physical key press...", color = CyberCyan, fontSize = 13.sp)
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val code = detectedKeyCodeInRebind
                        if (code != null) {
                            keyRepo.updateBinding(targetAction, code)
                        }
                        actionBeingRebound = null
                    },
                    enabled = detectedKeyCodeInRebind != null,
                    colors = ButtonDefaults.buttonColors(containerColor = CyberViolet),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.testTag("dialog_confirm_rebind_button")
                ) {
                    Text("Save Binding")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { actionBeingRebound = null },
                    modifier = Modifier.testTag("dialog_cancel_rebind_button")
                ) {
                    Text("Cancel", color = TextSecondary)
                }
            },
            containerColor = ObsidianSurface,
            shape = RoundedCornerShape(16.dp)
        )
    }
}

@Composable
private fun PresetButton(
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    testTag: String? = null
) {
    Box(
        modifier = modifier
            .then(if (testTag != null) Modifier.testTag(testTag) else Modifier)
            .clip(RoundedCornerShape(10.dp))
            .background(ObsidianSurfaceElevated)
            .border(1.dp, GlassBorder, RoundedCornerShape(10.dp))
            .clickable { onClick() }
            .padding(vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(title, color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            Text(subtitle, color = TextSecondary, fontSize = 10.sp)
        }
    }
}
