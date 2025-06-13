package com.rashil2000.turnofftheflash

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import com.rashil2000.turnofftheflash.ui.MainScreen
import com.rashil2000.turnofftheflash.ui.theme.TurnOffTheFlashTheme
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private lateinit var settingsManager: SettingsManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        settingsManager = SettingsManager(this)

        lifecycleScope.launch {
            if (settingsManager.isServiceEnabled.first()) {
                startForegroundService(Intent(this@MainActivity, BrightnessService::class.java))
            }
        }

        enableEdgeToEdge()
        setContent {
            TurnOffTheFlashTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    MainScreen(
                        settingsManager = settingsManager,
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}