package co.statu.parsek.config.migration

import co.statu.parsek.PluginManager
import co.statu.parsek.annotation.Migration
import co.statu.parsek.config.ConfigMigration
import com.typesafe.config.ConfigFactory
import com.typesafe.config.ConfigRenderOptions
import io.vertx.core.json.JsonObject
import org.slf4j.Logger
import java.io.File

@Migration
class ConfigMigration2To3(private val pluginManager: PluginManager, private val logger: Logger) :
    ConfigMigration(2, 3, "Split plugins section into specific folders") {
    override fun migrate(config: JsonObject) {
        val plugins = config.getJsonObject("plugins") ?: JsonObject()

        plugins.map.forEach { plugin ->
            val pluginId = plugin.key
            val pluginConfig = JsonObject.mapFrom(plugin.value as LinkedHashMap<*, *>)

            createPluginConfigFile(pluginId, pluginConfig)

            logger.info("Migrated \"${pluginId}\" plugin config to specific folder.")
        }

        config.remove("plugins")
    }

    private fun createPluginConfigFile(pluginId: String, config: JsonObject) {
        val renderOptions = ConfigRenderOptions
            .defaults()
            .setJson(false)           // false: HOCON, true: JSON
            .setOriginComments(false) // true: add comment showing the origin of a value
            .setComments(true)        // true: keep original comment
            .setFormatted(true)

        val parsedConfig = ConfigFactory.parseString(config.toString())

        val pluginsFolder = pluginManager.pluginsRoot.toAbsolutePath().toString()
        val pluginDataDir = File(System.getProperty("parsek.pluginDataDir", pluginsFolder)).absolutePath

        val configFilePath = File(pluginDataDir, "$pluginId${File.separator}config.conf").absolutePath

        val configFile = File(configFilePath)

        if (configFile.parentFile != null && !configFile.parentFile.exists()) {
            configFile.parentFile.mkdirs()
        }

        configFile.writeText(parsedConfig.root().render(renderOptions))
    }
}