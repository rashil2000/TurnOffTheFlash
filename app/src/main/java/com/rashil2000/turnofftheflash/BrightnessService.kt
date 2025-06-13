package com.rashil2000.turnofftheflash

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.database.ContentObserver
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.provider.Settings
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

class BrightnessService : Service() {

    private val job = SupervisorJob()
    private val scope = CoroutineScope(Dispatchers.IO + job)
    private lateinit var settingsManager: SettingsManager

    private val brightnessObserver by lazy {
        object : ContentObserver(Handler(Looper.getMainLooper())) {
            override fun onChange(selfChange: Boolean) {
                super.onChange(selfChange)
                val currentBrightness = Settings.System.getInt(contentResolver, Settings.System.SCREEN_BRIGHTNESS)
                _currentBrightness.value = currentBrightness
                checkBrightness(currentBrightness)
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        settingsManager = SettingsManager(this)

        settingsManager.isServiceEnabled.onEach { isEnabled ->
            if (!isEnabled) {
                stopSelf()
            }
        }.launchIn(scope)

        contentResolver.registerContentObserver(
            Settings.System.getUriFor(Settings.System.SCREEN_BRIGHTNESS),
            true,
            brightnessObserver
        )
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForeground(NOTIFICATION_ID, createNotification())
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        contentResolver.unregisterContentObserver(brightnessObserver)
        job.cancel()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun checkBrightness(currentBrightness: Int) {
        scope.launch {
            val threshold = settingsManager.brightnessThreshold.first()
            val isDarkMode = DarkModeManager.isSystemDarkMode(this@BrightnessService)

            if (currentBrightness < threshold && !isDarkMode) {
                DarkModeManager.toggleSystemDarkMode(this@BrightnessService)
            } else if (currentBrightness > threshold && isDarkMode) {
                DarkModeManager.toggleSystemDarkMode(this@BrightnessService)
            }
        }
    }

    private fun createNotification(): Notification {
        val channel = NotificationChannel(
            NOTIFICATION_CHANNEL_ID,
            "Brightness Service",
            NotificationManager.IMPORTANCE_DEFAULT
        )
        val notificationManager = getSystemService(NotificationManager::class.java)
        notificationManager.createNotificationChannel(channel)

        return Notification.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setContentTitle("Brightness Monitor")
            .setContentText("Monitoring screen brightness")
            .setSmallIcon(R.mipmap.ic_launcher)
            .build()
    }

    companion object {
        private const val NOTIFICATION_ID = 1
        private const val NOTIFICATION_CHANNEL_ID = "brightness_service_channel"

        private val _currentBrightness = MutableStateFlow(0)
        val currentBrightness = _currentBrightness.asStateFlow()
    }
}