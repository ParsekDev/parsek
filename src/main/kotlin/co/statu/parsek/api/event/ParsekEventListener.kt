package co.statu.parsek.api.event

import co.statu.parsek.config.ConfigManager
import org.pf4j.ExtensionPoint

/**
 * ParsekEventListener is an extension point for listening Parsek related events
 * such as when config manager has been initialized.
 */
interface ParsekEventListener : ExtensionPoint {

    fun onConfigManagerReady(configManager: ConfigManager)
}