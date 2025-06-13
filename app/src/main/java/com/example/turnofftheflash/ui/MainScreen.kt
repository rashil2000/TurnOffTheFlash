package com.example.turnofftheflash.ui

import android.content.Intent
import android.content.pm.PackageManager
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.turnofftheflash.BrightnessService
import com.example.turnofftheflash.SettingsManager
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@Composable
fun MainScreen(
    settingsManager: SettingsManager,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val serviceIntent = remember { Intent(context, BrightnessService::class.java) }

    var hasPermission by remember {
        mutableStateOf(
            context.checkSelfPermission(android.Manifest.permission.WRITE_SECURE_SETTINGS) == PackageManager.PERMISSION_GRANTED
        )
    }

    val isServiceEnabled by settingsManager.isServiceEnabled.collectAsState(initial = false)
    val brightnessThreshold by settingsManager.brightnessThreshold.collectAsState(initial = 32)
    val currentBrightness by BrightnessService.currentBrightness.collectAsState()

    Column(
        modifier = modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        if (hasPermission) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = "Enable Brightness Monitoring")
                Switch(
                    checked = isServiceEnabled,
                    onCheckedChange = { isEnabled ->
                        coroutineScope.launch {
                            settingsManager.setServiceEnabled(isEnabled)
                            if (isEnabled) {
                                context.startForegroundService(Intent(context, BrightnessService::class.java))
                            }
                        }
                    }
                )
            }
            Spacer(modifier = Modifier.height(32.dp))
            Text(text = "Current Brightness: $currentBrightness")
            Spacer(modifier = Modifier.height(16.dp))

            Text(text = "Brightness Threshold: $brightnessThreshold")
            Slider(
                value = brightnessThreshold.toFloat(),
                onValueChange = { value ->
                    coroutineScope.launch {
                        settingsManager.setBrightnessThreshold(value.roundToInt())
                    }
                },
                valueRange = 0f..255f,
                enabled = isServiceEnabled
            )

        } else {
            val packageName = context.packageName
            val adbCommand = "adb shell pm grant $packageName android.permission.WRITE_SECURE_SETTINGS"
            Text(
                text = "Permission not granted. Please grant it using ADB:\n\n$adbCommand",
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = {
                hasPermission =
                    context.checkSelfPermission(android.Manifest.permission.WRITE_SECURE_SETTINGS) == PackageManager.PERMISSION_GRANTED
            }) {
                Text("Check Permission Again")
            }
        }
    }
}