package hitshoot.controller

import hitshoot.util.pageTitle
import io.vertx.ext.web.RoutingContext

/**
 * Controller for the homepage
 * @since 1.0.0
 */
class HomeController {
	fun get(r: RoutingContext) {
		r.pageTitle("Home")
	}
}