package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Keyboard
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Mouse
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
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
import com.example.input.PhysicalKeyboardDetector
import com.example.settings.SettingsRepository
import com.example.ui.components.GlassCard
import com.example.ui.components.SectionHeader
import com.example.ui.components.StatusBadge
import com.example.ui.theme.CyberAmber
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.CyberEmerald
import com.example.ui.theme.CyberViolet
import com.example.ui.theme.GlassBorder
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun OnboardingGuideScreen(
    keyboardDetector: PhysicalKeyboardDetector,
    settingsRepo: SettingsRepository,
    onFinish: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val keyboardInfo by keyboardDetector.deviceState.collectAsState()

    var isAccessibilityEnabled by remember {
        mutableStateOf(AccessibilityManagerHelper.isAccessibilityServiceEnabled(context))
    }
    var isOverlayGranted by remember {
        mutableStateOf(AccessibilityManagerHelper.isOverlayPermissionGranted(context))
    }

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
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        SectionHeader(
            title = "KeyPilot Setup Guide",
            subtitle = "3 simple steps to enable keyboard cursor control on your Lenovo tablet"
        )

        // Step 1: Connect Physical Keyboard
        GuideStepCard(
            stepNumber = "1",
            title = "Connect Physical Keyboard",
            description = "Plug in a USB OTG keyboard or pair a Bluetooth keyboard with your tablet.",
            isComplete = keyboardInfo.isConnected,
            actionButtonText = if (keyboardInfo.isConnected) "Detected: ${keyboardInfo.name}" else "Refresh Detection",
            onAction = { keyboardDetector.refresh() },
            icon = Icons.Default.Keyboard,
            statusBadgeText = if (keyboardInfo.isConnected) "CONNECTED" else "WAITING FOR KEYBOARD",
            testTag = "guide_step_keyboard"
        )

        // Step 2: Enable Accessibility Service
        GuideStepCard(
            stepNumber = "2",
            title = "Enable Accessibility Service",
            description = "KeyPilot requires Android Accessibility Service to intercept Arrow Key presses and dispatch touch taps. No personal data or keystroke passwords are ever logged or transmitted.",
            isComplete = isAccessibilityEnabled,
            actionButtonText = if (isAccessibilityEnabled) "Service Enabled" else "Open Accessibility Settings",
            onAction = { AccessibilityManagerHelper.openAccessibilitySettings(context) },
            icon = Icons.Default.TouchApp,
            statusBadgeText = if (isAccessibilityEnabled) "ENABLED" else "ACTION REQUIRED",
            testTag = "guide_step_accessibility"
        )

        // Step 3: Overlay & System Permissions
        GuideStepCard(
            stepNumber = "3",
            title = "Verify System Overlay Permission",
            description = "Allows KeyPilot to render the high-visibility hardware-accelerated pointer arrow on top of other applications, browsers, and the home screen.",
            isComplete = isOverlayGranted || isAccessibilityEnabled, // Accessibility service itself has permission to render TYPE_ACCESSIBILITY_OVERLAY
            actionButtonText = if (isOverlayGranted) "Overlay Ready" else "Open Overlay Settings",
            onAction = { AccessibilityManagerHelper.openOverlaySettings(context) },
            icon = Icons.Default.Layers,
            statusBadgeText = if (isOverlayGranted || isAccessibilityEnabled) "CONFIGURED" else "OPTIONAL / CONFIGURE",
            testTag = "guide_step_overlay"
        )

        // Summary Card
        GlassCard(modifier = Modifier.fillMaxWidth(), cornerRadius = 16.dp) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Ready to Pilot", color = TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                Text(
                    text = "• Toggle Mode: Press F6 on your keyboard anytime to activate or pause cursor mode.\n• Move: Arrow Keys (Up, Down, Left, Right).\n• Click: Enter (Left click), Backspace (Secondary click), Space (Drag/Hold).\n• Speed Boost: Hold Shift while moving.",
                    color = TextSecondary,
                    fontSize = 12.sp,
                    lineHeight = 17.sp
                )

                Spacer(modifier = Modifier.height(4.dp))

                Button(
                    onClick = {
                        settingsRepo.updateSettings { it.copy(hasCompletedOnboarding = true) }
                        onFinish()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("finish_onboarding_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = CyberEmerald),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Default.Done, contentDescription = null, tint = Color.Black)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Complete Setup & Open Dashboard", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun GuideStepCard(
    stepNumber: String,
    title: String,
    description: String,
    isComplete: Boolean,
    actionButtonText: String,
    onAction: () -> Unit,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    statusBadgeText: String,
    testTag: String? = null
) {
    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        cornerRadius = 14.dp,
        testTag = testTag
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(if (isComplete) CyberEmerald else CyberViolet),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isComplete) {
                            Icon(Icons.Default.Check, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
                        } else {
                            Text(stepNumber, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                    Text(title, color = TextPrimary, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                }

                StatusBadge(
                    label = statusBadgeText,
                    isActive = isComplete,
                    activeColor = CyberEmerald,
                    inactiveColor = CyberAmber
                )
            }

            Text(description, color = TextSecondary, fontSize = 12.sp, lineHeight = 16.sp)

            Button(
                onClick = onAction,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isComplete) CyberEmerald.copy(alpha = 0.2f) else CyberViolet,
                    contentColor = if (isComplete) CyberEmerald else Color.White
                ),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.height(38.dp)
            ) {
                Text(actionButtonText, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}
