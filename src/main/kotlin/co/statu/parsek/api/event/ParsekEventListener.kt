package co.statu.parsek.api.event

import co.statu.parsek.api.ParsekEvent
import co.statu.parsek.config.ConfigManager

/**
 * ParsekEventListener is an extension point for listening Parsek related events
 * such as when config manager has been initialized.
 */
interface ParsekEventListener : ParsekEvent {

    suspend fun onConfigManagerReady(configManager: ConfigManager)
}