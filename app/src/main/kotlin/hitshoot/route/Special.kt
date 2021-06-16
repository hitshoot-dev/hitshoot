package hitshoot.route

import hitshoot.App.configManager
import hitshoot.App.logger
import hitshoot.App.vertx
import hitshoot.util.pageTitle
import hitshoot.util.renderPage
import io.vertx.ext.web.Router
import io.vertx.kotlin.coroutines.await
import io.vertx.kotlin.coroutines.dispatcher
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.nio.channels.ClosedChannelException

/**
 * Sets up special routes
 * @param router The router to set them up on
 * @since 1.0.0
 */
@DelicateCoroutinesApi
fun setupSpecialRoutes(router: Router) {
	router.errorHandler(404) { r ->
		GlobalScope.launch(vertx.dispatcher()) {
			try {
				r.response().statusCode = 404

				// Page title
				r.pageTitle("Not Found")

				// Render not found page
				r.put("noRender", false)
				r.renderPage("notfound")
			} catch(e: Throwable) {
				r.fail(e)
			}
		}
	}

	router.errorHandler(500) { r ->
		val conf = configManager.config.server

		val err = r.failure()
		if(err != null) {
			// First check what type of error
			// If it's something having to do with a closed channel, there's no reason to report it, since the client is already gone
			if(err is ClosedChannelException)
				return@errorHandler

			logger.error("Error occurred while serving route ${r.request().method().name()} ${r.request().path()}:")
			r.failure().printStackTrace()
		}

		if(!r.response().ended()) {
			GlobalScope.launch(vertx.dispatcher()) {
				try {
					r.response().statusCode = 500

					// Page title
					r.pageTitle("Internal Error")

					// Put error in render data
					r.put("errorText", if(err == null)
						"Unknown error (status code: ${r.statusCode()})"
					else if(conf.showErrorStackTraces && conf.showErrorMessages)
						err.stackTraceToString()
					else if(conf.showErrorMessages)
						"${err::class.java.name}: ${err.message}"
					else
						err::class.java.name)

					// Render page
					r.put("noRender", false)
					r.renderPage("error")
				} catch(e: Throwable) {
					r.end("Internal error").await()
				}
			}
		}
	}
}