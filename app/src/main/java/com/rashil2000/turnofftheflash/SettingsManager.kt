package com.rashil2000.turnofftheflash

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class SettingsManager(context: Context) {
    private val dataStore = context.dataStore

    companion object {
        val SERVICE_ENABLED_KEY = booleanPreferencesKey("service_enabled")
        val BRIGHTNESS_THRESHOLD_KEY = intPreferencesKey("brightness_threshold")
    }

    val isServiceEnabled: Flow<Boolean> = dataStore.data
        .map { preferences ->
            preferences[SERVICE_ENABLED_KEY] ?: false
        }

    val brightnessThreshold: Flow<Int> = dataStore.data
        .map { preferences ->
            preferences[BRIGHTNESS_THRESHOLD_KEY] ?: 32
        }

    suspend fun setServiceEnabled(isEnabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[SERVICE_ENABLED_KEY] = isEnabled
        }
    }

    suspend fun setBrightnessThreshold(threshold: Int) {
        dataStore.edit { preferences ->
            preferences[BRIGHTNESS_THRESHOLD_KEY] = threshold
        }
    }
}