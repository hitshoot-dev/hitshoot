package hitshoot.template

import com.github.jknack.handlebars.*
import com.github.jknack.handlebars.cache.ConcurrentMapTemplateCache
import com.github.jknack.handlebars.io.ClassPathTemplateLoader
import hitshoot.App
import hitshoot.util.urlToMediaLink
import java.io.InputStreamReader
import java.net.URLEncoder
import java.nio.file.FileSystemNotFoundException
import java.nio.file.FileSystems
import java.nio.file.Files
import java.nio.file.Paths
import java.util.concurrent.ConcurrentHashMap


/**
 * Manager for page templates
 */
class TemplateManager {
	/**
	 * All currently loaded templates.
	 * Key: path, value: template content
	 * @since 1.0.0
	 */
	val templates = ConcurrentHashMap<String, Template>()

	// Template file extension
	private val tmplExtension = "hbs"
	// Template root
	private val tmplRoot = "/view/"
	// Template processor
	private val handlebars = Handlebars(ClassPathTemplateLoader().apply {
		prefix = "/view"
		suffix = ".hbs"
		charset = Charsets.UTF_8
	}).with(ConcurrentMapTemplateCache())

	// Lists all files
	private fun listClasspathFiles(root: String): Array<String> {
		val paths = ArrayList<String>()

		val uri = App::class.java.getResource(root)!!.toURI()
		val dirPath = try {
			Paths.get(uri)
		} catch (e: FileSystemNotFoundException) {
			// If this is thrown, then it means that we are running the JAR directly (example: not from an IDE)
			val env = mutableMapOf<String, String>()
			FileSystems.newFileSystem(uri, env).getPath(root)
		}

		// Iterate over paths and find all files
		Files.list(dirPath).forEach {
			if(it.toAbsolutePath().toString().endsWith('/')) {
				paths.addAll(listClasspathFiles(it.toAbsolutePath().toString()))
			} else {
				paths.add(it.toAbsolutePath().toString())
			}
		}

		return paths.toTypedArray()
	}

	/**
	 * Loads all templates to memory
	 * @since 1.0.0
	 */
	fun load() {
		// Register helpers
		handlebars.registerHelper("ifEquals", Helper<Any?> { context, options ->
			if(options.params.isNotEmpty()) {
				if(context.equals(options.params[0])) {
					options.fn()
				} else {
					options.inverse()
				}
			} else {
				options.inverse()
			}
		})
		handlebars.registerHelper("urlencode", Helper<Any?> { context, _ -> URLEncoder.encode(context.toString(), "UTF-8")})
		handlebars.registerHelper("media", Helper<Any?> { context, _ -> urlToMediaLink(context.toString()) })

		// Clear cache
		templates.clear()

		// Get all template files
		val fileList = listClasspathFiles("/view")

		// Read each one
		for(file in fileList) {
			val path = file.substring(tmplRoot.length, file.lastIndex-tmplExtension.length)

			// Read file, process it, and put it in cache map
			val fin = InputStreamReader(App::class.java.getResourceAsStream(file)!!)
			templates[path] = handlebars.compileInline(fin.readText())
			fin.close()
		}
	}

	/**
	 * Renders a template and returns the result
	 * @param path The template name/path
	 * @param data The data to pass to the template
	 * @return The rendered template
	 * @throws TemplateNotFoundException If the provided template name/path does not exist
	 * @throws TemplateRenderException If rendering the template fails
	 * @since 1.0.0
	 */
	fun renderTemplate(path: String, data: Map<String, Any?>): String {
		if(templates.containsKey(path)) {
			try {
				val template = templates[path]!!

				return template.apply(data)
			} catch(e: HandlebarsException) {
				throw TemplateRenderException("Failed to render template \"$path\": ${e::class.java.name}: ${e.message}")
			}
		} else {
			throw TemplateNotFoundException("The template \"$path\" does not exist or is not loaded")
		}
	}
}