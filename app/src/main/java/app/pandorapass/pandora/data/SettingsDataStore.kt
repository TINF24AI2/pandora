package app.pandorapass.pandora.data

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

class SettingsDataStore(private val context: Context) {

    private val isDarkModeKey = booleanPreferencesKey("is_dark_mode")
    private val clipboardTimeoutKey = intPreferencesKey("clipboard_timeout_seconds")
    private val autoLockTimeoutKey = intPreferencesKey("auto_lock_timeout_seconds")

    val isDarkMode: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[isDarkModeKey] ?: false
        }

    val clipboardTimeout: Flow<Int> = context.dataStore.data
        .map { preferences ->
            preferences[clipboardTimeoutKey] ?: 30
        }

    val autoLockTimeout: Flow<Int> = context.dataStore.data
        .map { preferences ->
            preferences[autoLockTimeoutKey] ?: 300
        }

    suspend fun setDarkMode(isDarkMode: Boolean) {
        context.dataStore.edit { settings ->
            settings[isDarkModeKey] = isDarkMode
        }
    }

    suspend fun setClipboardTimeout(timeoutInSeconds: Int) {
        context.dataStore.edit { settings ->
            settings[clipboardTimeoutKey] = timeoutInSeconds
        }
    }

    suspend fun setAutoLockTimeout(timeoutInSeconds: Int) {
        context.dataStore.edit { settings ->
            settings[autoLockTimeoutKey] = timeoutInSeconds
        }
    }
}
