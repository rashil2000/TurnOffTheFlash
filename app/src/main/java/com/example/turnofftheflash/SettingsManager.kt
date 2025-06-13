package com.example.turnofftheflash

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
        val LOWER_BRIGHTNESS_THRESHOLD_KEY = intPreferencesKey("lower_brightness_threshold")
        val UPPER_BRIGHTNESS_THRESHOLD_KEY = intPreferencesKey("upper_brightness_threshold")
    }

    val isServiceEnabled: Flow<Boolean> = dataStore.data
        .map { preferences ->
            preferences[SERVICE_ENABLED_KEY] ?: false
        }

    val lowerBrightnessThreshold: Flow<Int> = dataStore.data
        .map { preferences ->
            preferences[LOWER_BRIGHTNESS_THRESHOLD_KEY] ?: 50
        }

    val upperBrightnessThreshold: Flow<Int> = dataStore.data
        .map { preferences ->
            preferences[UPPER_BRIGHTNESS_THRESHOLD_KEY] ?: 200
        }

    suspend fun setServiceEnabled(isEnabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[SERVICE_ENABLED_KEY] = isEnabled
        }
    }

    suspend fun setLowerBrightnessThreshold(threshold: Int) {
        dataStore.edit { preferences ->
            preferences[LOWER_BRIGHTNESS_THRESHOLD_KEY] = threshold
        }
    }

    suspend fun setUpperBrightnessThreshold(threshold: Int) {
        dataStore.edit { preferences ->
            preferences[UPPER_BRIGHTNESS_THRESHOLD_KEY] = threshold
        }
    }
}