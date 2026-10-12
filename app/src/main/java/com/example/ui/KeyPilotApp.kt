package com.example.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Keyboard
import androidx.compose.material.icons.filled.Mouse
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.accessibility.AccessibilityManagerHelper
import com.example.accessibility.KeyPilotAccessibilityService
import com.example.input.KeyMappingRepository
import com.example.input.PhysicalKeyboardDetector
import com.example.settings.AppSettings
import com.example.settings.SettingsRepository
import com.example.ui.screens.CursorTestScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.KeyboardSetupScreen
import com.example.ui.screens.OnboardingGuideScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.WelcomeScreen
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.CyberEmerald
import com.example.ui.theme.CyberRose
import com.example.ui.theme.CyberViolet
import com.example.ui.theme.GlassBorder
import com.example.ui.theme.ObsidianBackground
import com.example.ui.theme.ObsidianSurface
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

enum class AppScreen(val title: String, val icon: androidx.compose.ui.graphics.vector.ImageVector) {
    WELCOME("Welcome", Icons.Default.Info),
    DASHBOARD("Dashboard", Icons.Default.Dashboard),
    KEY_SETUP("Key Setup", Icons.Default.Keyboard),
    CURSOR_TEST("Cursor Test", Icons.Default.PlayArrow),
    SETTINGS("Settings", Icons.Default.Settings),
    GUIDE("Setup Guide", Icons.Default.Build)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KeyPilotApp(
    settingsRepo: SettingsRepository,
    keyMappingRepo: KeyMappingRepository,
    keyboardDetector: PhysicalKeyboardDetector,
    modifier: Modifier = Modifier
) {
    val settings by settingsRepo.settings.collectAsState()
    val isCursorActive by KeyPilotAccessibilityService.isCursorModeActive.collectAsState()
    val context = LocalContext.current

    val initialScreen = if (settings.hasCompletedOnboarding) AppScreen.DASHBOARD else AppScreen.WELCOME
    var currentScreen by remember { mutableStateOf(initialScreen) }

    // Tablet landscape / wide screen detection
    val configuration = LocalConfiguration.current
    val isWideScreen = configuration.screenWidthDp >= 720

    // Back handling: If on secondary screen, go back to Dashboard
    BackHandler(enabled = currentScreen != AppScreen.DASHBOARD && currentScreen != AppScreen.WELCOME) {
        currentScreen = AppScreen.DASHBOARD
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            if (currentScreen != AppScreen.WELCOME) {
                TopAppBar(
                    title = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "KeyPilot",
                                color = TextPrimary,
                                fontSize = 19.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            // Mode Status Pill
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(
                                        if (isCursorActive) CyberEmerald.copy(alpha = 0.15f)
                                        else ObsidianSurface
                                    )
                                    .border(
                                        1.dp,
                                        if (isCursorActive) CyberEmerald else GlassBorder,
                                        RoundedCornerShape(12.dp)
                                    )
                                    .padding(horizontal = 8.dp, vertical = 3.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(6.dp)
                                            .clip(CircleShape)
                                            .background(if (isCursorActive) CyberEmerald else TextMuted)
                                    )
                                    Spacer(modifier = Modifier.width(5.dp))
                                    Text(
                                        text = if (isCursorActive) "CURSOR ACTIVE" else "STANDBY (F6)",
                                        color = if (isCursorActive) CyberEmerald else TextSecondary,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    },
                    actions = {
                        IconButton(
                            onClick = {
                                if (AccessibilityManagerHelper.isAccessibilityServiceEnabled(context)) {
                                    KeyPilotAccessibilityService.activeServiceInstance?.toggleCursorMode()
                                } else {
                                    AccessibilityManagerHelper.openAccessibilitySettings(context)
                                }
                            },
                            modifier = Modifier.testTag("topbar_cursor_toggle_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Mouse,
                                contentDescription = "Toggle Cursor Mode",
                                tint = if (isCursorActive) CyberEmerald else CyberViolet
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                )
            }
        },
        bottomBar = {
            if (!isWideScreen && currentScreen != AppScreen.WELCOME) {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = 8.dp
                ) {
                    val navItems = listOf(
                        AppScreen.DASHBOARD,
                        AppScreen.KEY_SETUP,
                        AppScreen.CURSOR_TEST,
                        AppScreen.SETTINGS
                    )
                    navItems.forEach { screen ->
                        NavigationBarItem(
                            selected = currentScreen == screen,
                            onClick = { currentScreen = screen },
                            icon = { Icon(screen.icon, contentDescription = screen.title) },
                            label = { Text(screen.title, fontSize = 11.sp) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = CyberViolet,
                                selectedTextColor = CyberViolet,
                                indicatorColor = CyberViolet.copy(alpha = 0.2f),
                                unselectedIconColor = TextMuted,
                                unselectedTextColor = TextMuted
                            ),
                            modifier = Modifier.testTag("nav_item_${screen.name.lowercase()}")
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Wide Screen Navigation Rail (Lenovo Tab M10 Landscape)
            if (isWideScreen && currentScreen != AppScreen.WELCOME) {
                NavigationRail(
                    containerColor = MaterialTheme.colorScheme.surface,
                    modifier = Modifier.testTag("tablet_nav_rail")
                ) {
                    val navItems = listOf(
                        AppScreen.DASHBOARD,
                        AppScreen.KEY_SETUP,
                        AppScreen.CURSOR_TEST,
                        AppScreen.SETTINGS,
                        AppScreen.GUIDE
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    navItems.forEach { screen ->
                        NavigationRailItem(
                            selected = currentScreen == screen,
                            onClick = { currentScreen = screen },
                            icon = { Icon(screen.icon, contentDescription = screen.title) },
                            label = { Text(screen.title, fontSize = 11.sp) },
                            colors = NavigationRailItemDefaults.colors(
                                selectedIconColor = CyberViolet,
                                selectedTextColor = CyberViolet,
                                indicatorColor = CyberViolet.copy(alpha = 0.2f),
                                unselectedIconColor = TextMuted,
                                unselectedTextColor = TextMuted
                            ),
                            modifier = Modifier.testTag("rail_item_${screen.name.lowercase()}")
                        )
                    }
                    Spacer(modifier = Modifier.weight(1f))
                }
            }

            // Screen Content
            Box(modifier = Modifier.weight(1f)) {
                when (currentScreen) {
                    AppScreen.WELCOME -> {
                        WelcomeScreen(
                            onStartSetup = { currentScreen = AppScreen.GUIDE },
                            onSkipToDashboard = {
                                settingsRepo.updateSettings { it.copy(hasCompletedOnboarding = true) }
                                currentScreen = AppScreen.DASHBOARD
                            }
                        )
                    }
                    AppScreen.DASHBOARD -> {
                        DashboardScreen(
                            settings = settings,
                            settingsRepo = settingsRepo,
                            keyboardDetector = keyboardDetector,
                            onNavigateToTest = { currentScreen = AppScreen.CURSOR_TEST },
                            onNavigateToSetup = { currentScreen = AppScreen.KEY_SETUP },
                            onNavigateToSettings = { currentScreen = AppScreen.SETTINGS },
                            onNavigateToGuide = { currentScreen = AppScreen.GUIDE }
                        )
                    }
                    AppScreen.KEY_SETUP -> {
                        KeyboardSetupScreen(keyRepo = keyMappingRepo)
                    }
                    AppScreen.CURSOR_TEST -> {
                        CursorTestScreen(settings = settings)
                    }
                    AppScreen.SETTINGS -> {
                        SettingsScreen(settingsRepo = settingsRepo)
                    }
                    AppScreen.GUIDE -> {
                        OnboardingGuideScreen(
                            keyboardDetector = keyboardDetector,
                            settingsRepo = settingsRepo,
                            onFinish = { currentScreen = AppScreen.DASHBOARD }
                        )
                    }
                }
            }
        }
    }
}
