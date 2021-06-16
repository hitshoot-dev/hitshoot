package hitshoot.config

/**
 * Instance configuration
 * @since 1.0.0
 */
class AppConfig {
	/**
	 * Configuration format version
	 * @since 1.0.0
	 */
	var formatVersion = 0

	/**
	 * Application server
	 * @since 1.0.0
	 */
	var server = Server()
	class Server {
		/**
		 * The hostname or IP address to bind to
		 * @since 1.0.0
		 */
		var host = "0.0.0.0"

		/**
		 * The port to bind to
		 * @since 1.0.0
		 */
		var port = 8080

		/**
		 * Whether the server is running behind a reverse proxy, such as Nginx
		 * @since 1.0.0
		 */
		var usingReverseProxy = false

		/**
		 * How many worker threads to have in a pool for CPU-intensive operations
		 * @since 1.0.0
		 */
		var workerThreads = 10

		/**
		 * The User-Agent header to send along with requests to BitChute
		 * @since 1.0.0
		 */
		var userAgent = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/74.0.3729.131 Safari/537.36"

		/**
		 * The amount of API client connections to keep in a pool for connecting to BitChute
		 * @since 1.0.0
		 */
		var connectionPoolSize = 10

		/**
		 * Whether to show error messages on the error page
		 * @since 1.0.0
		 */
		var showErrorMessages = true

		/**
		 * Whether to show full stack traces on the error page
		 * @since 1.0.0
		 */
		var showErrorStackTraces = true
	}

	/**
	 * Application
	 * @since 1.0.0
	 */
	var app = App()
	class App {
		/**
		 * The name that shows up on pages
		 * @since 1.0.0
		 */
		var name = "HitShoot"

		/**
		 * Whether to proxy media via HitShoot rather than linking directly to BitChute
		 * @since 1.0.0
		 */
		var proxyMedia = true

		/**
		 * The contact method to display at the bottom of pages or other locations
		 * @since 1.0.0
		 */
		var contactLink = "<a href=\"mailto:example@example.com\">example@example.com</a>"

		/**
		 * The software project's URL
		 * @since 1.0.0
		 */
		var projectUrl = "https://github.com/hitshoot-dev/hitshoot"
	}

	// This will be used in a future release
//	/**
//	 * PostgreSQL database
//	 * @since 1.0.0
//	 */
//	var db = DB()
//	class DB {
//		/**
//		 * The DB server host
//		 * @since 1.0.0
//		 */
//		var host = "localhost"
//
//		/**
//		 * The DB server port
//		 * @since 1.0.0
//		 */
//		var port = 5432
//
//		/**
//		 * The DB name
//		 * @since 1.0.0
//		 */
//		var name = "hitshoot"
//
//		/**
//		 * The user to authenticate as
//		 * @since 1.0.0
//		 */
//		var user = "someone"
//
//		/**
//		 * The user's password
//		 * @since 1.0.0
//		 */
//		var pass = "TheBestPassword&1337&"
//	}

	/**
	 * Media transcoding
	 * @since 1.0.0
	 */
	var transcoding = Transcoding()
	class Transcoding {
		/**
		 * Whether the option of using media transcoding should be available
		 * @since 1.0.0
		 */
		var enable = false

		/**
		 * Whether to enable transcoding to video
		 * @since 1.0.0
		 */
		var enableVideo = true

		/**
		 * Whether to enable transcoding to audio
		 * @since 1.0.0
		 */
		var enableAudio = true

		/**
		 * Where ffmpeg is located
		 * @since 1.0.0
		 */
		var ffmpegPath = "/usr/bin/ffmpeg"

		/**
		 * The amount of worker threads to use for executing ffmpeg
		 * @since 1.0.0
		 */
		var workerThreads = 5
	}
}