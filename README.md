# KeyPilot — Android Keyboard-to-Mouse Controller

KeyPilot is a native, production-grade Android application that enables users to steer a system-wide virtual mouse pointer and perform click/drag/navigation gestures across all applications, web browsers, and home screen using a physical USB OTG or Bluetooth keyboard.

Designed and optimized specifically for **Android 10 (API 29)** on tablets such as the **Lenovo Tab M10**, KeyPilot operates **100% offline without root access**, third-party helper PCs, or physical mice.

---

## 1. Primary Controls

| Action | Default Hardware Key | Description |
| :--- | :--- | :--- |
| **Move Pointer Up** | `Arrow Up` | Moves cursor upward with smooth acceleration |
| **Move Pointer Down** | `Arrow Down` | Moves cursor downward with smooth acceleration |
| **Move Pointer Left** | `Arrow Left` | Moves cursor left with smooth acceleration |
| **Move Pointer Right** | `Arrow Right` | Moves cursor right with smooth acceleration |
| **Primary Click** | `Enter` | Dispatches simulated touch tap at pointer location (`dispatchGesture`) |
| **Secondary Click** | `Backspace` | Dispatches long-press context click |
| **Hold / Drag** | `Space` | Holds down pointer button for dragging and text selection |
| **Toggle Cursor Mode** | `F6` *(Configurable)* | Instantly toggles between pointer control and ordinary typing |
| **Speed Boost** | `Shift` (Left/Right) | Temporarily accelerates cursor velocity by 2.5× while held |
| **System Back** | `Escape` | Performs system back navigation |
| **System Home** | `Home` | Returns to Android home screen |
| **Recent Apps** | `App Switch` | Opens Android app overview |

*All key bindings are fully customizable via the in-app Key Setup screen.*

---

## 2. System-Wide Cursor Implementation & Platform Analysis

### Android OS Constraint & Transparency Notice
On stock Android without root access or a privileged system signature, third-party applications **cannot** inject raw Linux kernel mouse events (`InputManager.injectInputEvent()` requires the signature-level `INJECT_EVENTS` permission). 

### How KeyPilot Delivers True System-Wide Control:
1. **Physical Key Interception (`canRequestFilterKeyEvents="true"`):**  
   Android's `AccessibilityService.onKeyEvent()` intercepts physical USB OTG and Bluetooth keyboard events system-wide before any other app receives them. When Cursor Mode is active (`F6`), KeyPilot consumes arrow keys and click keys; when inactive, keystrokes pass through untouched for normal typing.
2. **Hardware-Accelerated Floating Cursor Overlay:**  
   KeyPilot renders a responsive, low-latency pointer (`CursorOverlayView`) using Android's `TYPE_ACCESSIBILITY_OVERLAY` with `FLAG_NOT_FOCUSABLE` and `FLAG_NOT_TOUCHABLE`, floating across all 3rd-party apps, browsers, and system windows.
3. **Accessibility Gesture Injection (`dispatchGesture()`):**  
   When the user presses `Enter`, KeyPilot dispatches an official Android touch tap gesture precisely at the virtual cursor's `(x, y)` coordinates via `AccessibilityService.dispatchGesture()`. This interacts with native buttons, links, scrollbars, and elements across any application.

---

## 3. Architecture & Clean Package Structure

```
com.example/
├── MainActivity.kt                       # App entry point & lifecycle coordinator
├── accessibility/
│   ├── KeyPilotAccessibilityService.kt   # System-wide key filter & gesture service
│   ├── AccessibilityManagerHelper.kt     # Permission checks & settings intents
│   └── GestureDispatcher.kt              # dispatchGesture() tap, drag, scroll engine
├── input/
│   ├── KeyAction.kt                      # All configurable cursor actions
│   ├── KeyBinding.kt                     # KeyCode to action mappings & key names
│   ├── KeyMappingRepository.kt           # Persistence & preset management
│   └── PhysicalKeyboardDetector.kt       # Live USB OTG & Bluetooth hardware detection
├── cursor/
│   ├── CursorState.kt                    # Immutable pointer state model
│   ├── CursorSpeedConfig.kt              # Velocity, acceleration curve & bounds clamping
│   ├── MovementEngine.kt                 # 60 FPS simultaneous key movement loop
│   └── CursorOverlayView.kt              # Floating custom Canvas overlay view
├── settings/
│   ├── AppSettings.kt                    # Preferences schema
│   └── SettingsRepository.kt             # DataStore / SharedPreferences engine
├── utils/
│   ├── DisplayMetricsHelper.kt           # Screen density & orientation handling
│   └── DeviceCompatibilityHelper.kt      # Hardware & Android 10 capability report
└── ui/
    ├── KeyPilotApp.kt                    # Adaptive phone/tablet navigation
    ├── components/GlassComponents.kt     # Specular glassmorphic UI components
    ├── theme/                            # Cyber-Chic Obsidian M3 theme
    └── screens/
        ├── WelcomeScreen.kt              # Onboarding hero & device check
        ├── DashboardScreen.kt            # Live telemetry, master switch & quick tools
        ├── KeyboardSetupScreen.kt        # Visual D-Pad, key rebind dialog & presets
        ├── CursorTestScreen.kt           # Interactive target test arena
        ├── SettingsScreen.kt             # Speed sliders, cursor styling & haptics
        └── OnboardingGuideScreen.kt      # 3-step setup walkthrough
```

---

## 4. Building & Installing

### Prerequisites
- Android SDK with API 26+ (Target API 34/36)
- Java 11 or 17
- Gradle 8+

### Build Debug APK
Run the following Gradle command:
```bash
gradle assembleDebug
```
The compiled APK will be generated at:
`app/build/outputs/apk/debug/app-debug.apk`

### Install Directly on Lenovo Tab M10
1. Enable **Developer Options** and **USB Debugging** on the tablet:
   - Go to **Settings > About Tablet > Tap "Build Number" 7 times**.
   - Go to **Settings > System > Developer Options > Enable "USB Debugging"**.
2. Connect the Lenovo Tab M10 to your computer via USB:
   ```bash
   adb install -r app/build/outputs/apk/debug/app-debug.apk
   ```
3. Alternatively, transfer `app-debug.apk` directly to the tablet's Downloads folder via USB flash drive or Google Drive and tap to install.

---

## 5. First-Time Setup on Lenovo Tab M10

1. **Connect Keyboard:** Plug in your USB keyboard via an OTG adapter, or pair a Bluetooth keyboard.
2. **Open KeyPilot:** Launch KeyPilot from the home screen.
3. **Enable Accessibility Service:**
   - Tap **"Enable Accessibility"** in KeyPilot.
   - In Android Settings, navigate to **Installed Services > KeyPilot Cursor Controller**.
   - Toggle to **ON** and accept the system prompt.
4. **Activate Cursor Mode:** Press **`F6`** (or toggle the master switch on the Dashboard).
5. **Steer & Click:** Use the arrow keys to move the pointer anywhere on your tablet screen, and press **`Enter`** to click.

---

## 6. Troubleshooting for Android 10

- **Arrow keys type in text fields instead of moving pointer:**  
  Press `F6` to activate Cursor Mode. When Cursor Mode is active, Arrow keys and Enter are consumed by KeyPilot.
- **USB OTG keyboard not recognized:**  
  Some Lenovo tablets require enabling OTG in **Settings > Connected Devices**. Also ensure the OTG cable supports data and not charge-only.
- **Overlay disappears after screen rotation:**  
  KeyPilot automatically listens to `onConfigurationChanged` and recalculates screen width and height. If the pointer was outside the new screen dimensions, it is clamped inside the visible bounds.
- **Key Conflict:**  
  If two actions share the same key, open **Key Setup** and tap the conflicting action to reassign it or tap **"Reset All Keys to Defaults"**.
