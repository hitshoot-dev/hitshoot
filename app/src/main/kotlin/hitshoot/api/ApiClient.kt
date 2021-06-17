package hitshoot.api

import hitshoot.App.configManager
import hitshoot.App.logger
import hitshoot.App.vertx
import io.netty.handler.codec.http.cookie.Cookie
import io.vertx.core.Future
import io.vertx.core.MultiMap
import io.vertx.core.buffer.Buffer
import io.vertx.core.http.HttpMethod
import io.vertx.core.impl.NoStackTraceThrowable
import io.vertx.core.json.JsonObject
import io.vertx.ext.web.client.*
import io.vertx.kotlin.coroutines.await
import java.net.URLEncoder

/**
 * Stateful API client that keeps track of cookies and other important information
 * @since 1.0.0
 */
class ApiClient {
	private var client: WebClient? = null

	/**
	 * The underlying WebClient used to make requests
	 * @since 1.0.0
	 */
	val webClient: WebClient
		get() = client!!

	private var clientSession: WebClientSession? = null

	/**
	 * The underlying WebClientSession used to make requests and keep track of cookies
	 * @since 1.0.0
	 */
	val webSession: WebClientSession
		get() = clientSession!!

	/**
	 * The site host where requests should be sent
	 * @since 1.0.0
	 */
	val host = "www.bitchute.com"

	/**
	 * The site root where requests should be sent
	 * @since 1.0.0
	 */
	val siteRoot = "https://www.bitchute.com"

	/**
	 * The API root where API requests should be sent
	 * @since 1.0.0
	 */
	val apiRoot = "$siteRoot/api"

	/**
	 * Initializes the API client
	 * @since 1.0.0
	 */
	suspend fun init() {
		// Create web client
		client = WebClient.create(vertx, WebClientOptions()
				.setUserAgent(configManager.config.server.userAgent)
				.setUserAgentEnabled(true)
				.setFollowRedirects(true)
				.setDefaultHost(host)
				.setDefaultPort(443)
				.setSsl(true)
				.setMaxPoolSize(configManager.config.server.connectionPoolSize)
				.setMaxRedirects(5)
				.setTrustAll(true)
				.setUseAlpn(true))

		// Create session client
		clientSession = WebClientSession.create(client)
		webSession
				.addHeader("Accept", "*/*")
				.addHeader("Accept-Language", "en-US,en;q=0.5")

		// Setup session
		refreshSession()

		logger.info("Current CSRF token is: "+getCSRFToken())
	}

	/**
	 * Refreshes the session by clearing cookies and sending a request to the site root to get required credentials again
	 * @since 1.0.0
	 */
	suspend fun refreshSession() {
		clearCookies()

		// Request a random page to obtain the CSRF token necessary to perform API requests
		webSession.getAbs("$siteRoot/${Math.random()}/").send().await()

		if(getCSRFToken() == null)
			logger.warn("Failed to obtain a CSRF token, requests will fail!")
	}

	/**
	 * Returns all cookies currently associated with the web client (for BitChute)
	 * @return All cookies
	 * @since 1.0.0
	 */
	fun cookies(): Iterable<Cookie> {
		return webSession.cookieStore().get(true, host, "/")
	}

	/**
	 * Deletes all current cookies associated with the web client (for BitChute)
	 * @since 1.0.0
	 */
	fun clearCookies() {
		cookies().forEach {
			webSession.cookieStore().remove(it)
		}
	}

	/**
	 * Returns the current CSRF token, or null if it isn't present
	 * @return The current CSRF token
	 * @since 1.0.0
	 */
	fun getCSRFToken(): String? {
		val cookies = cookies()

		for(cookie in cookies)
			if(cookie.name() == "csrftoken")
				return cookie.value()

		return null
	}

	/**
	 * Performs a request to the provided relative path and method, and returns the result.
	 * Attempts to retry request if it encounters 403 Forbidden by refreshing the session and firing the request again.
	 * If the retry fails, the request fails entirely.
	 * @param method The HTTP method
	 * @param path The relative path to request (or full URL if it starts with http:// or https://)
	 * @param data The data to send, form encoded body for POST, query params for everything else
	 * @param retry Whether to retry if met with 403 Forbidden (only retries once)
	 * @return The HTTP response (even if not 2XX status)
	 * @throws NoStackTraceThrowable If the server returns 403 Forbidden even after retry
	 * @since 1.0.0
	 */
	suspend fun request(method: HttpMethod, path: String, data: Map<String, String> = HashMap(), retry: Boolean = true): HttpResponse<Buffer> {
		val url = if(path.startsWith("http://") || path.startsWith("https://"))
			path
		else
			siteRoot+path

		fun send(): Future<HttpResponse<Buffer>> {
			val req = webSession.requestAbs(method, url)

			// Add cookies
			val cookieStr = StringBuilder()
			cookies().forEach {
				cookieStr.append("${it.name()}=${URLEncoder.encode(it.value(), "UTF-8")}; ")
			}
			val cutCookie = cookieStr.endsWith(' ')
			req.headers().add("Cookie", if(cutCookie)
				cookieStr.toString().substring(0, cookieStr.length-2)
			else
				cookieStr.toString())

			// Add required headers
			req.headers()
					.add("Origin", siteRoot)
					.add("Referer", url)
					.add("X-Requested-With", "XMLHttpRequest")

			if(method != HttpMethod.POST) {
				req.queryParams()
						.addAll(data)
						.add("csrfmiddlewaretoken", getCSRFToken().orEmpty())
				req.headers()
						.add("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8")
			}

			return if(method == HttpMethod.POST)
				req.sendForm(MultiMap.caseInsensitiveMultiMap()
						.addAll(data)
						.add("csrfmiddlewaretoken", getCSRFToken().orEmpty()))
			else
				req.send()
		}

		// Send request
		var res = send().await()

		// Retry request if got 403 Forbidden
		if(retry && res.statusCode() == 403) {
			logger.info("API request $method $path was forbidden (${res.statusCode()} ${res.statusMessage()}), refreshing CSRF token and trying again...")
			refreshSession()
			res = send().await()
		}

		// Check if 403 Forbidden
		if(res.statusCode() == 403) {
			// Try to determine if this is the result of Cloudflare blocking it (fuck cloudflare)
			val body = res.bodyAsString()

			if(body.contains("CAPTCHA") && body.contains("Cloudflare"))
				throw NoStackTraceThrowable("Server returned ${res.statusCode()} ${res.statusMessage()}. This seems to be the result of Cloudflare blocking automated traffic. Try again in a few hours.")
			else
				throw NoStackTraceThrowable("Server returned ${res.statusCode()} ${res.statusMessage()}")
		}

		// Send response
		return res
	}

	/**
	 * Performs a request to the provided relative path and method, and returns the result as a JsonObject.
	 * Attempts to retry request if it encounters 403 Forbidden by refreshing the session and firing the request again.
	 * Fails if response type is not JSON, or if "success" JSON field is false.
	 * @param method The HTTP method
	 * @param path The relative path to request (or full URL if it starts with http:// or https://)
	 * @param data The data to send, form encoded body for POST, query params for everything else
	 * @param retry Whether to retry if met with 403 Forbidden (only retries once)
	 * @return The HTTP response (even if not 2XX status)
	 * @throws NoStackTraceThrowable If response type is not JSON, or if the "success" JSON field is false
	 * @since 1.0.0
	 */
	suspend fun requestJson(method: HttpMethod, path: String, data: Map<String, String> = HashMap(), retry: Boolean = true): JsonObject {
		val res = request(method, path, data, retry)

		// Check for JSON content type
		if(res.headers().contains("Content-Type") && (res.headers()["Content-Type"] == "application/json" || res.headers()["Content-Type"] == "text/json")) {
			// Serialize JSON
			val json = res.bodyAsJsonObject()

			if(json.getBoolean("success"))
				return json
			else
				throw NoStackTraceThrowable("API response's \"success\" field was false:\n"+json.encodePrettily())
		} else {
			throw NoStackTraceThrowable("Expected JSON response, got non-JSON (${res.headers()["Content-Type"]})")
		}
	}
}