package hitshoot.route

import hitshoot.App
import hitshoot.controller.HomeController
import hitshoot.controller.SearchController
import hitshoot.util.renderPage
import io.vertx.ext.web.Router
import io.vertx.kotlin.coroutines.dispatcher
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

/**
 * Sets up controller routes
 * @param router The router to set them up on
 * @since 1.0.0
 */
@DelicateCoroutinesApi
fun setupControllerRoutes(router: Router) {
	val homeController = HomeController()
	val searchController = SearchController()

	router.get("/").handler { r ->
		GlobalScope.launch(App.vertx.dispatcher()) {
			try {
				homeController.get(r)
				r.renderPage("home")
			} catch(e: Throwable) {
				r.fail(e)
			}
		}
	}

	router.get("/search").handler { r ->
		GlobalScope.launch(App.vertx.dispatcher()) {
			try {
				searchController.handle(r)
				r.renderPage("search")
			} catch(e: Throwable) {
				r.fail(e)
			}
		}
	}
	router.post("/search").handler { r ->
		GlobalScope.launch(App.vertx.dispatcher()) {
			try {
				searchController.handle(r)
				r.renderPage("search")
			} catch(e: Throwable) {
				r.fail(e)
			}
		}
	}
}