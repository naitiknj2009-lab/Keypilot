package com.example.accessibility

import android.accessibilityservice.AccessibilityServiceInfo
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.view.accessibility.AccessibilityManager

/**
 * Checks system permission status and launches system settings screens.
 */
object AccessibilityManagerHelper {

    /**
     * Checks if KeyPilot's accessibility service is currently enabled by the user in Android settings.
     */
    fun isAccessibilityServiceEnabled(context: Context): Boolean {
        val am = context.getSystemService(Context.ACCESSIBILITY_SERVICE) as? AccessibilityManager
            ?: return false

        val enabledServices = am.getEnabledAccessibilityServiceList(AccessibilityServiceInfo.FEEDBACK_ALL_MASK)
        val expectedPackage = context.packageName

        for (service in enabledServices) {
            val resolveInfo = service.resolveInfo ?: continue
            val serviceInfo = resolveInfo.serviceInfo ?: continue
            if (serviceInfo.packageName == expectedPackage &&
                serviceInfo.name.contains("KeyPilotAccessibilityService")
            ) {
                return true
            }
        }

        // Secondary fallback check via Secure.ENABLED_ACCESSIBILITY_SERVICES
        val settingValue = Settings.Secure.getString(
            context.contentResolver,
            Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
        ) ?: return false

        val expectedComponent = ComponentName(context, KeyPilotAccessibilityService::class.java).flattenToString()
        return settingValue.contains(expectedComponent)
    }

    /**
     * Checks if display over other apps permission is granted.
     */
    fun isOverlayPermissionGranted(context: Context): Boolean {
        return Settings.canDrawOverlays(context)
    }

    /**
     * Opens Android Accessibility settings where the user can enable KeyPilot.
     */
    fun openAccessibilitySettings(context: Context) {
        val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        try {
            context.startActivity(intent)
        } catch (ignored: Exception) {}
    }

    /**
     * Opens Android Overlay (Display over other apps) settings.
     */
    fun openOverlaySettings(context: Context) {
        val intent = Intent(
            Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
            Uri.parse("package:${context.packageName}")
        ).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        try {
            context.startActivity(intent)
        } catch (ignored: Exception) {
            val fallback = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(fallback)
        }
    }

    /**
     * Opens physical keyboard / input settings screen.
     */
    fun openKeyboardSettings(context: Context) {
        val intent = Intent(Settings.ACTION_INPUT_METHOD_SETTINGS).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        try {
            context.startActivity(intent)
        } catch (ignored: Exception) {}
    }
}
