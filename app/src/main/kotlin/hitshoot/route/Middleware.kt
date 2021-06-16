package hitshoot.route

import hitshoot.App.vertx
import hitshoot.middleware.RenderDataMiddleware
import hitshoot.middleware.SecurityMiddleware
import io.vertx.ext.web.Router
import io.vertx.kotlin.coroutines.dispatcher
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

/**
 * Sets up middleware routes
 * @param router The router to set them up on
 * @since 1.0.0
 */
@OptIn(DelicateCoroutinesApi::class)
fun setupMiddlewareRoutes(router: Router) {
	router.route().handler { r ->
		GlobalScope.launch(vertx.dispatcher()) {
			try {
				SecurityMiddleware().handler(r)
				RenderDataMiddleware().handler(r)
				r.next()
			} catch(e: Throwable) {
				r.fail(e)
			}
		}
	}
}