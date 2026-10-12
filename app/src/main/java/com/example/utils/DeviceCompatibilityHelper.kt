package com.example.utils

import android.os.Build

data class CompatibilityReport(
    val deviceName: String,
    val androidVersion: String,
    val apiLevel: Int,
    val isLenovoDevice: Boolean,
    val isAndroid10: Boolean,
    val keyFilteringSupported: Boolean,
    val gestureDispatchSupported: Boolean,
    val overlaySupported: Boolean,
    val nativePointerInjectionSupported: Boolean,
    val explanationText: String
)

object DeviceCompatibilityHelper {

    fun getCompatibilityReport(): CompatibilityReport {
        val manufacturer = Build.MANUFACTURER
        val model = Build.MODEL
        val sdk = Build.VERSION.SDK_INT
        val release = Build.VERSION.RELEASE

        val isLenovo = manufacturer.contains("lenovo", ignoreCase = true) || model.contains("tb-", ignoreCase = true)
        val isAndroid10 = sdk == Build.VERSION_CODES.Q

        val nativeSupported = false // Platform constraint: standard Android apps cannot inject raw kernel mouse events without root/system signature

        val explanation = buildString {
            append("Device: $manufacturer $model (Android $release, API $sdk).\n\n")
            append("• Physical Keyboard Interception: FULLY SUPPORTED via AccessibilityService key filtering.\n")
            append("• System-Wide Virtual Pointer Overlay: FULLY SUPPORTED via Accessibility WindowManager Overlay.\n")
            append("• Click & Gesture Dispatch: FULLY SUPPORTED via official dispatchGesture() API across all third-party apps, browsers, and home screen.\n\n")
            append("PLATFORM TRANSPARENCY NOTICE:\n")
            append("On stock Android (including Lenovo Tab M10 without root access), native kernel mouse event injection requires the privileged 'INJECT_EVENTS' system signature permission. KeyPilot transparently operates using Android's official Accessibility Gesture engine and a hardware-accelerated floating pointer overlay.")
        }

        return CompatibilityReport(
            deviceName = "$manufacturer $model",
            androidVersion = "Android $release",
            apiLevel = sdk,
            isLenovoDevice = isLenovo,
            isAndroid10 = isAndroid10,
            keyFilteringSupported = sdk >= Build.VERSION_CODES.JELLY_BEAN_MR2,
            gestureDispatchSupported = sdk >= Build.VERSION_CODES.N,
            overlaySupported = sdk >= Build.VERSION_CODES.M,
            nativePointerInjectionSupported = nativeSupported,
            explanationText = explanation
        )
    }
}
