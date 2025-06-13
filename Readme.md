<p align="center">
    <img src="app/src/main/res/mipmap-xxxhdpi/ic_launcher.webp" width="150" />
</p>

<div align="center">
    <h1>Turn Off The Flash</h1>
    <p><strong>Automatically toggle system-wide dark mode based on screen brightness.</strong></p>
</div>

## Features

-   **Automatic Dark Mode:** Automatically enables/disables system-wide dark mode based on the screen brightness.
-   **Customizable Threshold:** Set your own brightness threshold to trigger the dark mode.
-   **Foreground Service:** A persistent foreground service monitors the brightness changes reliably.
-   **Quick Settings Tile:** A convenient tile in the Quick Settings panel to easily enable or disable the brightness monitoring service.
-   **No Root Required:** Does not require root access, but needs a one-time permission grant via ADB.

## Installation

1.  **Install the App:** Download the latest APK from the [Releases](https://github.com/rashil2000/TurnOffTheFlash/releases) page and install it on your device (requires Android 15+).
2.  **Grant Permission:** To allow the app to control the system's dark mode, you need to grant the `WRITE_SECURE_SETTINGS` permission. This is a one-time setup step and can be done via [Android Debug Bridge (ADB)](https://developer.android.com/studio/command-line/adb) with the following command:

    ```shell
    adb shell pm grant com.rashil2000.turnofftheflash android.permission.WRITE_SECURE_SETTINGS
    ```

## How It Works

The app runs a foreground service that listens for changes in the system's screen brightness level. When the brightness level crosses the user-defined threshold, it toggles the system-wide dark mode by changing the value of `Settings.Secure.UI_NIGHT_MODE`.

To apply the theme change immediately, the app uses a clever technique of cycling the Car Mode UI. This forces the system to re-evaluate and apply the theme settings across all applications. It also uses an undocumented flag `0x0002` to prevent going to the home screen when Car Mode is disabled.

```kotlin
uiModeManager.enableCarMode(UiModeManager.ENABLE_CAR_MODE_ALLOW_SLEEP)
uiModeManager.disableCarMode(0x0002) // 0x0002 = UiModeManager.DISABLE_CAR_MODE_ALL_PRIORITIES
```
