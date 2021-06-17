package hitshoot

/**
 * Application constants
 * @since 1.0.0
 */
object Constants {
	/**
	 * The name of the application
	 * @since 1.0.0
	 */
	val APP_NAME = "HitShoot"

	/**
	 * The current application version name
	 * @since 1.0.0
	 */
	val VERSION = "1.0.0"

	/**
	 * The current application version integer (incremental)
	 * @since 1.0.0
	 */
	val VERSION_INT: Int = 0

	/**
	 * The current configuration format version
	 * @since 1.0.0
	 */
	val CONFIG_VERSION: Int = 0

	/**
	 * Headers that are proxied from the client to the server by the media proxy
	 * @since 1.0.0
	 */
	val PROXY_IN_HEADERS: Array<String> = arrayOf(
			"Accept",
			"Accept-Encoding",
			"Accept-Language",
			"Cache-Control",
			"Pragma",
			"User-Agent",
			"Referer",
			"Range"
	)

	/**
	 * Headers that are proxied from the server to the client by the media proxy
	 * @since 1.0.0
	 */
	val PROXY_OUT_HEADERS: Array<String> = arrayOf(
			"Accept-Ranges",
			"Vary",
			"Cache-Control",
			"Content-Length",
			"Content-Type",
			"Content-Range",
			"Date",
			"ETag",
			"Last-Modified"
	)
}