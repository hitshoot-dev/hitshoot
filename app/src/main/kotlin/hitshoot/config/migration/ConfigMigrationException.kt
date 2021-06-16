package hitshoot.config.migration

import hitshoot.config.ConfigException

/**
 * Exception to be thrown when an issue relating to configuration migration occurs
 * @param msg The exception message
 * @since 1.0.0
 */
open class ConfigMigrationException(msg: String): ConfigException(msg)