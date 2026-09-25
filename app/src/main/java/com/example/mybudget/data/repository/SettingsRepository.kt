package com.example.mybudget.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

@Singleton
class SettingsRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val DARK_MODE_KEY = booleanPreferencesKey("dark_mode")
    private val LAST_CLEARED_TIMESTAMP_KEY = longPreferencesKey("last_cleared_timestamp")
    private val ANIMATIONS_ENABLED_KEY = booleanPreferencesKey("animations_enabled")
    private val FONT_SCALE_KEY = androidx.datastore.preferences.core.floatPreferencesKey("font_scale")
    private val SHOW_FLOATING_CALCULATOR_KEY = booleanPreferencesKey("show_floating_calculator")

    val isDarkMode: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[DARK_MODE_KEY] ?: false
        }

    val lastClearedTimestamp: Flow<Long> = context.dataStore.data
        .map { preferences ->
            preferences[LAST_CLEARED_TIMESTAMP_KEY] ?: 0L
        }

    val isAnimationsEnabled: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[ANIMATIONS_ENABLED_KEY] ?: true
        }

    val fontScale: Flow<Float> = context.dataStore.data
        .map { preferences ->
            preferences[FONT_SCALE_KEY] ?: 1.0f
        }

    val showFloatingCalculator: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[SHOW_FLOATING_CALCULATOR_KEY] ?: true // Default is true
        }

    suspend fun setDarkMode(isDarkMode: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[DARK_MODE_KEY] = isDarkMode
        }
    }

    suspend fun setLastClearedTimestamp(timestamp: Long) {
        context.dataStore.edit { preferences ->
            preferences[LAST_CLEARED_TIMESTAMP_KEY] = timestamp
        }
    }

    suspend fun setAnimationsEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[ANIMATIONS_ENABLED_KEY] = enabled
        }
    }

    suspend fun setFontScale(scale: Float) {
        context.dataStore.edit { preferences ->
            preferences[FONT_SCALE_KEY] = scale
        }
    }

    suspend fun setShowFloatingCalculator(show: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[SHOW_FLOATING_CALCULATOR_KEY] = show
        }
    }
}
