package hitshoot.util

import hitshoot.App
import hitshoot.App.templateManager
import io.vertx.ext.web.RoutingContext
import io.vertx.kotlin.coroutines.await
import java.io.File
import java.io.FileReader
import java.io.FileWriter
import java.net.URLEncoder
import java.util.regex.Matcher
import java.util.regex.Pattern

/**
 * Pattern matching BitChute links
 * @since 1.0.0
 */
val bitchuteLinkPattern = Pattern.compile("https?:\\/\\/([a-zA-Z0-9-]+\\.)?bitchute\\.com(\\/[-a-zA-Z0-9:;,@#%&()~_?\\+=\\/\\\\\\.]*)?")

/**
 * Pattern matching any valid URL
 * @since 1.0.0
 */
val urlPattern: Pattern = Pattern.compile("(?:https?|file|c):(?:\\/{1,3}|\\\\{1})[-a-zA-Z0-9:;,@#%&()~_?\\+=\\/\\\\\\.]*")

/**
 * Returns the directory path where the application resides on disk
 * @return The directory path where the application resides on disk
 * @since 1.0.0
 */
fun appDir(): String {
	val path = File(App::class.java.protectionDomain.codeSource.location.toURI()).path

	return path.substring(0, path.lastIndexOf('/'))
}

/**
 * Reads a file in its entirety to a String and then returns it
 * @param path The file's path
 * @return The file's contents as a String
 * @since 1.0.0
 */
fun readFileBlocking(path: String): String {
	val reader = FileReader(path)
	val str = reader.readText()
	reader.close()
	return str
}

/**
 * Writes a string to a file, overwriting the previous contents if the file already exists
 * @param path The file's path
 * @param content The content to write to the file
 * @since 1.0.0
 */
fun writeFileBlocking(path: String, content: String) {
	val writer = FileWriter(path)
	writer.write(content)
	writer.close()
}

/**
 * Marks the context to signify that the page for this route will not be rendered
 * @return This, to be used fluently
 * @since 1.0.0
 */
fun RoutingContext.noRender(): RoutingContext {
	put("noRender", true)
	return this
}

/**
 * Sets the title of the page that will be rendered for this route
 * @param title The page title
 * @return This, to be used fluently
 * @since 1.0.0
 */
fun RoutingContext.pageTitle(title: String): RoutingContext {
	put("pageTitle", title)
	return this
}

/**
 * Puts an error message in the context for a template to show
 * @param message The error message
 * @return This, to be used fluently
 * @since 1.0.0
 */
fun RoutingContext.errorMessage(message: String): RoutingContext {
	put("errorMessage", message)
	return this
}

/**
 * Renders a page if not disabled by a controller
 * @param template The template's relative path (minus its extension, that is handled automatically)
 * @since 1.0.0
 */
suspend fun RoutingContext.renderPage(template: String) {
	// Make sure noRender isn't true
	if(get<Boolean?>("noRender") != true && !response().ended()) {
		// Set as text/html if not already assigned
		if(!response().headers().contains("Content-Type"))
			response().putHeader("Content-Type", "text/html; charset=utf-8")

		// Render page and write it to response
		end(templateManager.renderTemplate(template, data())).await()
	}
}

/**
 * Converts a map to query parameters
 * @param map The map to convert
 * @return The query parameters string, minus the initial '?'
 * @since 1.0.0
 */
fun mapToQueryParams(map: Map<String, Any?>): String {
	val str = StringBuilder()

	for((key, value) in map.entries)
		str.append("$key=${URLEncoder.encode(value.toString(), "UTF-8")}&")

	return if(str.last() == '&')
		str.toString().substring(0, str.length-1)
	else
		str.toString()
}

/**
 * Puts all required pagination info into context
 * @param currentPage The current page
 * @param totalPages The total amount of pages
 * @param nextPageLink The link to the next page
 * @param lastPageLink The link to the last page
 * @since 1.0.0
 */
fun RoutingContext.putPaginationInfo(currentPage: Int, totalPages: Int, nextPageLink: String?, lastPageLink: String?) {
	put("currentPage", currentPage)
	put("totalPages", totalPages)
	put("nextPage", if(currentPage >= totalPages) null else currentPage+1)
	put("lastPage", if(currentPage <= 0) null else currentPage-1)
	put("nextPageLink", if(currentPage >= totalPages) null else nextPageLink)
	put("lastPageLink", if(currentPage <= 0) null else lastPageLink)
}

/**
 * Returns whether the provided link is a BitChute URL
 * @return Whether the provided link is a BitChute URL
 * @since 1.0.0
 */
fun isBitChuteUrl(url: String) = bitchuteLinkPattern.matcher(url).matches()

/**
 * Returns whether the provided link is a valid URL
 * @return Whether the provided link is a valid URL
 * @since 1.0.0
 */
fun isValidUrl(url: String) = urlPattern.matcher(url).matches()

/**
 * Converts the provided URL to a media link.
 * Returns the original URL if proxying is off, otherwise return the link as a proxied version.
 * @param url The URL to convert
 * @return The media link
 * @since 1.0.0
 */
fun urlToMediaLink(url: String): String {
	var link = url

	// Prepend BitChute if not included
	if(!link.startsWith("https://") && !link.startsWith("http://"))
		link = App.apiClient.siteRoot+link


	// Check if proxy is enabled
	if(App.configManager.config.app.proxyMedia) {
		// Check link against pattern
		link = if(isBitChuteUrl(link)) {
			// Proxy link
			"/proxy?url=${URLEncoder.encode(link, "UTF-8")}"
		} else {
			// Doesn't match, just set link to blank
			""
		}
	}

	// Return processed link
	return link
}

/**
 * Like replace, but replaces matches with a function's return value
 * @param regex The regex to replace
 * @param func The function to use for replacements
 * @return The processed String
 * @since 1.0.0
 */
fun String.replaceWithFunction(regex: Pattern, func: java.util.function.Function<Matcher, String>): String {
	val res = StringBuffer()
	val matcher = regex.matcher(this)
	while(matcher.find())
		matcher.appendReplacement(res, func.apply(matcher))
	matcher.appendTail(res)
	return res.toString()
}