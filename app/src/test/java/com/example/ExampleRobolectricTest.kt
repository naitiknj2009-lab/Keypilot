package com.example

import android.content.Context
import android.view.KeyEvent
import androidx.test.core.app.ApplicationProvider
import com.example.input.KeyAction
import com.example.input.KeyBinding
import com.example.input.KeyMappingRepository
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

    @Test
    fun readStringFromContext() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("KeyPilot", appName)
    }

    @Test
    fun testKeyMappingRepositoryDefaultsAndPresets() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val repo = KeyMappingRepository(context)
        repo.resetToDefaults()

        // Verify arrow keys default mappings
        assertEquals(KeyAction.MOVE_UP, repo.getActionForKeyCode(KeyEvent.KEYCODE_DPAD_UP))
        assertEquals(KeyAction.MOVE_DOWN, repo.getActionForKeyCode(KeyEvent.KEYCODE_DPAD_DOWN))
        assertEquals(KeyAction.MOVE_LEFT, repo.getActionForKeyCode(KeyEvent.KEYCODE_DPAD_LEFT))
        assertEquals(KeyAction.MOVE_RIGHT, repo.getActionForKeyCode(KeyEvent.KEYCODE_DPAD_RIGHT))
        assertEquals(KeyAction.PRIMARY_CLICK, repo.getActionForKeyCode(KeyEvent.KEYCODE_ENTER))
        assertEquals(KeyAction.TOGGLE_CURSOR_MODE, repo.getActionForKeyCode(KeyEvent.KEYCODE_F6))

        // Test conflict detection
        val conflict = repo.findConflict(KeyAction.MOVE_DOWN, KeyEvent.KEYCODE_DPAD_UP)
        assertEquals(KeyAction.MOVE_UP, conflict)

        val noConflict = repo.findConflict(KeyAction.MOVE_UP, KeyEvent.KEYCODE_DPAD_UP)
        assertNull(noConflict)

        // Test WASD Preset
        repo.applyWasdPreset()
        assertEquals(KeyAction.MOVE_UP, repo.getActionForKeyCode(KeyEvent.KEYCODE_W))
        assertEquals(KeyAction.MOVE_LEFT, repo.getActionForKeyCode(KeyEvent.KEYCODE_A))
        assertEquals(KeyAction.MOVE_DOWN, repo.getActionForKeyCode(KeyEvent.KEYCODE_S))
        assertEquals(KeyAction.MOVE_RIGHT, repo.getActionForKeyCode(KeyEvent.KEYCODE_D))
    }

    @Test
    fun testKeyDisplayNames() {
        assertEquals("Arrow Up", KeyBinding.getKeyDisplayName(KeyEvent.KEYCODE_DPAD_UP))
        assertEquals("Arrow Down", KeyBinding.getKeyDisplayName(KeyEvent.KEYCODE_DPAD_DOWN))
        assertEquals("Enter", KeyBinding.getKeyDisplayName(KeyEvent.KEYCODE_ENTER))
        assertEquals("Space", KeyBinding.getKeyDisplayName(KeyEvent.KEYCODE_SPACE))
        assertEquals("F6", KeyBinding.getKeyDisplayName(KeyEvent.KEYCODE_F6))
        assertEquals("Left Shift", KeyBinding.getKeyDisplayName(KeyEvent.KEYCODE_SHIFT_LEFT))
    }
}
