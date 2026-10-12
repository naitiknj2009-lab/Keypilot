package com.example

import android.os.Bundle
import android.view.KeyEvent
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.example.accessibility.AccessibilityManagerHelper
import com.example.input.KeyMappingRepository
import com.example.input.PhysicalKeyboardDetector
import com.example.settings.SettingsRepository
import com.example.ui.KeyPilotApp
import com.example.ui.theme.KeyPilotTheme

class MainActivity : ComponentActivity() {

    private lateinit var settingsRepo: SettingsRepository
    private lateinit var keyMappingRepo: KeyMappingRepository
    private lateinit var keyboardDetector: PhysicalKeyboardDetector

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        settingsRepo = SettingsRepository.getInstance(this)
        keyMappingRepo = KeyMappingRepository.getInstance(this)
        keyboardDetector = PhysicalKeyboardDetector.getInstance(this)

        setContent {
            val settings by settingsRepo.settings.collectAsState()

            KeyPilotTheme(themeMode = settings.themeMode) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    KeyPilotApp(
                        settingsRepo = settingsRepo,
                        keyMappingRepo = keyMappingRepo,
                        keyboardDetector = keyboardDetector
                    )
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        keyboardDetector.refresh()
    }

    override fun onDestroy() {
        super.onDestroy()
        keyboardDetector.unregister()
    }
}
