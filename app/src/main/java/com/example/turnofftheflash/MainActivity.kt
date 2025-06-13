package com.example.turnofftheflash

import android.app.UiModeManager
import android.content.ContentResolver
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.turnofftheflash.ui.theme.TurnOffTheFlashTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TurnOffTheFlashTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    DarkModeToggler(
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

@Composable
fun DarkModeToggler(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val contentResolver = context.contentResolver
    val uiModeManager = remember {
        context.getSystemService(Context.UI_MODE_SERVICE) as UiModeManager
    }

    var hasPermission by remember {
        mutableStateOf(
            context.checkSelfPermission(android.Manifest.permission.WRITE_SECURE_SETTINGS) == PackageManager.PERMISSION_GRANTED
        )
    }

    var darkModeState by remember { mutableStateOf(isSystemDarkMode(contentResolver)) }

    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (hasPermission) {
            Text(
                text = "System Dark Mode is ${if (darkModeState) "On" else "Off"}",
                modifier = Modifier.padding(16.dp)
            )
            Button(onClick = {
                toggleSystemDarkMode(contentResolver, uiModeManager)
                darkModeState = isSystemDarkMode(contentResolver)
            }) {
                Text(text = "Toggle Dark Mode")
            }
        } else {
            val packageName = context.packageName
            val adbCommand = "adb shell pm grant $packageName android.permission.WRITE_SECURE_SETTINGS"
            Text(
                text = "Permission not granted. Please grant it using ADB:\n\n$adbCommand",
                modifier = Modifier.padding(16.dp)
            )
            Button(onClick = {
                hasPermission =
                    context.checkSelfPermission(android.Manifest.permission.WRITE_SECURE_SETTINGS) == PackageManager.PERMISSION_GRANTED
                if (hasPermission) {
                    darkModeState = isSystemDarkMode(contentResolver)
                }
            }) {
                Text("Check Permission Again")
            }
        }
    }
}

private const val UI_NIGHT_MODE = "ui_night_mode"

private fun isSystemDarkMode(contentResolver: ContentResolver): Boolean {
    return try {
        Settings.Secure.getInt(contentResolver, UI_NIGHT_MODE) == 2
    } catch (e: Settings.SettingNotFoundException) {
        // Assume light mode if setting is not found
        false
    }
}

private fun toggleSystemDarkMode(contentResolver: ContentResolver, uiModeManager: UiModeManager) {
    val isDarkMode = isSystemDarkMode(contentResolver)
    val newMode = if (isDarkMode) 1 else 2 // 1 for light, 2 for dark

    try {
        Settings.Secure.putInt(contentResolver, UI_NIGHT_MODE, newMode)

        // Cycle car mode to force-sync theme change event
        uiModeManager.enableCarMode(UiModeManager.ENABLE_CAR_MODE_ALLOW_SLEEP)
        uiModeManager.disableCarMode(0x0002) // 0x0002 = UiModeManager.DISABLE_CAR_MODE_ALL_PRIORITIES
    } catch (e: SecurityException) {
        // This will be caught if WRITE_SECURE_SETTINGS is not granted.
        e.printStackTrace()
    }
}

@Preview(showBackground = true)
@Composable
fun DarkModeTogglerPreview() {
    TurnOffTheFlashTheme {
        DarkModeToggler()
    }
}