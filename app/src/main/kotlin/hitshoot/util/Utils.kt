package hitshoot.util

import hitshoot.App
import hitshoot.App.templateManager
import io.vertx.ext.web.RoutingContext
import io.vertx.kotlin.coroutines.await
import java.io.File
import java.io.FileReader
import java.io.FileWriter
import java.net.URLEncoder

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