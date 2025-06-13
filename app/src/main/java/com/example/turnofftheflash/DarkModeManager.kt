package com.example.turnofftheflash

import android.app.UiModeManager
import android.content.Context
import android.provider.Settings
import android.util.Log

object DarkModeManager {
    private const val UI_NIGHT_MODE = "ui_night_mode"
    private const val TAG = "DarkModeManager"

    fun isSystemDarkMode(context: Context): Boolean {
        return try {
            Settings.Secure.getInt(context.contentResolver, UI_NIGHT_MODE) == 2
        } catch (e: Settings.SettingNotFoundException) {
            Log.e(TAG, "Could not find UI_NIGHT_MODE setting, assuming light mode.", e)
            // Assume light mode if setting is not found
            false
        }
    }

    fun toggleSystemDarkMode(context: Context) {
        val uiModeManager = context.getSystemService(Context.UI_MODE_SERVICE) as UiModeManager
        val isDarkMode = isSystemDarkMode(context)
        val newMode = if (isDarkMode) 1 else 2 // 1 for light, 2 for dark

        try {
            Settings.Secure.putInt(context.contentResolver, UI_NIGHT_MODE, newMode)

            // Cycle car mode to force-sync theme change event
            uiModeManager.enableCarMode(UiModeManager.ENABLE_CAR_MODE_ALLOW_SLEEP)
            uiModeManager.disableCarMode(0x0002) // 0x0002 = UiModeManager.DISABLE_CAR_MODE_ALL_PRIORITIES
        } catch (e: SecurityException) {
            // This will be caught if WRITE_SECURE_SETTINGS is not granted.
            Log.e(TAG, "Failed to toggle dark mode, do you have WRITE_SECURE_SETTINGS permission?", e)
        }
    }
}