package hitshoot.config.migration

import hitshoot.App.logger
import hitshoot.util.readFileBlocking
import hitshoot.util.writeFileBlocking
import org.yaml.snakeyaml.Yaml
import java.io.File
import java.nio.file.Files
import java.nio.file.StandardCopyOption

private val migrations = arrayOf<ConfigMigration>()

/**
 * Runs all available migrations on the provided configuration file
 * @param path The configuration file path
 * @since 1.0.0
 */
fun runConfigMigrations(path: String) {
	val yaml = Yaml()

	// Read file
	var content = readFileBlocking(path)

	// Parse file
	val map = yaml.load<Map<String, Any>>(content)
	val fmtVer = map["formatVersion"] as Int

	// Run migrations
	for(i in fmtVer until migrations.size) {
		val migration = migrations[i]

		logger.info("Running config migration \"${migration.name()}\"...")
		content = migrations[i].migrate(content)
	}

	// Backup old file
	Files.copy(File(path).toPath(), File("$path.bak").toPath(), StandardCopyOption.REPLACE_EXISTING)
	// Write new file
	writeFileBlocking(path, content)

	logger.info("Migrated old config version to the latest format!")
	logger.info("The old version was saved at $path.bak, so if there are any issues, you can use that to restore the old version")
}