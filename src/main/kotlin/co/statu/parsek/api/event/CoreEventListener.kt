package co.statu.parsek.api.event

import co.statu.parsek.config.ConfigManager

interface CoreEventListener : ParsekEventListener {
    suspend fun onConfigManagerReady(configManager: ConfigManager)
}