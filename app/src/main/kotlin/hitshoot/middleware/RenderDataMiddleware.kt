package hitshoot.middleware

import hitshoot.App.configManager
import hitshoot.Constants
import io.vertx.ext.web.RoutingContext

/**
 * Middleware that provides basic page render data
 * @since 1.0.0
 */
class RenderDataMiddleware {
	private val config = configManager.config

	fun handler(r: RoutingContext) {
		r
				.put("config", config)
				.put("pageTitle", null)
				.put("appName", Constants.APP_NAME)
				.put("appVersion", Constants.VERSION)
	}
}