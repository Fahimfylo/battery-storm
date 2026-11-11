package com.ghost.batterystorm.data

import com.ghost.batterystorm.core.AppSettings
import com.ghost.batterystorm.utils.Logger
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File

const val SETTINGS_FILE_NAME = "batterystorm-settings.json"

/**
 * Persists AppSettings to disk using kotlinx.serialization JSON.
 * Settings are stored in the user's application data directory.
 */
class SettingsRepository(private val settingsDir: File) {

    private val settingsFile = File(settingsDir, SETTINGS_FILE_NAME)
    private val serializer = Json { ignoreUnknownKeys = true; prettyPrint = true }

    fun load(): AppSettings {
        return if (settingsFile.exists()) {
            try {
                val json = settingsFile.readText()
                serializer.decodeFromString(AppSettings.serializer(), json)
            } catch (e: Exception) {
                Logger.warn("Failed to load settings, using defaults: ${e.message}")
                AppSettings()
            }
        } else {
            AppSettings()
        }
    }

    fun save(settings: AppSettings) {
        try {
            settingsDir.mkdirs()
            val json = serializer.encodeToString(AppSettings.serializer(), settings)
            settingsFile.writeText(json)
            Logger.debug("Settings saved to ${settingsFile.absolutePath}")
        } catch (e: Exception) {
            Logger.error("Failed to save settings: ${e.message}", e)
        }
    }
}