package com.rashil2000.turnofftheflash

import android.content.Intent
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.cancel

class BrightnessTileService : TileService() {

    private lateinit var settingsManager: SettingsManager
    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    override fun onCreate() {
        super.onCreate()
        settingsManager = SettingsManager(this)
    }

    override fun onStartListening() {
        super.onStartListening()
        scope.launch {
            settingsManager.isServiceEnabled.collect { isEnabled ->
                updateTile(isEnabled)
            }
        }
    }

    override fun onClick() {
        super.onClick()
        scope.launch {
            val wasEnabled = settingsManager.isServiceEnabled.first()
            val isEnabled = !wasEnabled
            settingsManager.setServiceEnabled(isEnabled)
            if (isEnabled) {
                startForegroundService(Intent(this@BrightnessTileService, BrightnessService::class.java))
            }
            updateTile(isEnabled)
        }
    }

    private fun updateTile(isEnabled: Boolean) {
        qsTile?.let {
            it.state = if (isEnabled) Tile.STATE_ACTIVE else Tile.STATE_INACTIVE
            it.label = if (isEnabled) "Monitoring On" else "Monitoring Off"
            it.updateTile()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        scope.cancel()
    }
}