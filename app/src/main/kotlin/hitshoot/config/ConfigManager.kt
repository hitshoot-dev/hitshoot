package hitshoot.config

import hitshoot.App
import hitshoot.App.logger
import hitshoot.Constants
import hitshoot.config.migration.runConfigMigrations
import hitshoot.util.readFileBlocking
import org.yaml.snakeyaml.Yaml
import org.yaml.snakeyaml.constructor.Constructor
import java.io.*
import kotlin.system.exitProcess

/**
 * Application configuration manager
 * @since 1.0.0
 */
class ConfigManager {
	// Config instance
	private var cfg = AppConfig()

	/**
	 * The current loaded configuration
	 * @since 1.0.0
	 */
	val config: AppConfig
		get() = cfg

	/**
	 * Writes the default config to the provided path
	 * @param path The path to write the config to
	 * @since 1.0.0
	 */
	fun writeDefaultConfig(path: String) {
		// Open input stream
		val fin = App::class.java.getResourceAsStream("/config.yml") as InputStream
		// Open write stream
		val fout = FileOutputStream(path)

		// Copy
		val buf = ByteArray(1024)
		var len: Int
		while(fin.read(buf).also { len = it } > -1)
			fout.write(buf, 0, len)

		// Close streams
		fin.close()
		fout.close()
	}

	/**
	 * Initializes the application configuration with the specified file path
	 * @param path The path to the configuration file to read from (or create if it doesn't exist)
	 * @since 1.0.0
	 */
	fun initialize(path: String) {
		val file = File(path)

		// Check if file is a directory
		if(file.isDirectory) {
			throw ConfigException("Config file path cannot be a directory")
		} else if(!file.exists()) {
			logger.info("File \"$path\" does not exist, creating new config file at that location...")

			// Write default config if one doesn't exist
			writeDefaultConfig(path)
		}

		// Read and parse config
		val ymlParser = Yaml()
		logger.info("Reading config file...")
		val cfgYml = ymlParser.load<Map<String, Any?>>(readFileBlocking(path))

		// Check for formatVersion
		if(cfgYml.containsKey("formatVersion") && cfgYml["formatVersion"] is Int) {
			val fmtVer = cfgYml["formatVersion"] as Int

			// If using a previous version, run migrations
			if(fmtVer < Constants.CONFIG_VERSION) {
				runConfigMigrations(path)
			} else if(fmtVer > Constants.CONFIG_VERSION) {
				logger.warn("formatVersion on config is higher than the current application version ($fmtVer > ${Constants.CONFIG_VERSION}), it was made for a newer version of ${Constants.APP_NAME}")
				logger.warn("There will probably be errors!")
			}

			// Read and parse the file again, this time serializing it to the config class
			val ymlToObject = Yaml(Constructor(AppConfig::class.java))
			cfg = ymlToObject.load(readFileBlocking(path))
		} else {
			logger.error("The field \"formatVersion\" is missing or invalid in the config!")
			logger.error("If it was deleted and you have not updated ${Constants.APP_NAME}, you can place the following line at the top of the config:")
			logger.error("formatVersion: ${Constants.CONFIG_VERSION}")
			logger.error("If the program was updated, try to remember which formatVersion was present when the file was created, and replace the number in the previous line with that")
			logger.error("If all else fails, you can discard the config file and rewrite it by specifying --recreate-config as a command line argument")
			exitProcess(1)
		}
		// TODO Read and parse file, do migrations
		// TODO Also add the cmdline-specified config values
	}
}