package hitshoot.config.migration

/**
 * Interface to be implemented by configuration migrations
 * @since 1.0.0
 */
interface ConfigMigration {
	/**
	 * Which version this migration migrates.
	 * For example, if this migration converts 0 to 1, this method would return 0.
	 * @since 1.0.0
	 */
	fun forVersion(): Int

	/**
	 * The name of this migration
	 * @since 1.0.0
	 */
	fun name(): String

	/**
	 * Runs this migration
	 * @param
	 * @since 1.0.0
	 */
	fun migrate(config: String): String
}