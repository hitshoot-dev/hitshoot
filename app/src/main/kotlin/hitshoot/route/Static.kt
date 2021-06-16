package hitshoot.route

import io.vertx.ext.web.Router
import io.vertx.ext.web.handler.StaticHandler

/**
 * Sets up static routes
 * @param router The router to set them up on
 * @since 1.0.0
 */
fun setupStaticRoutes(router: Router) {
	router.route("/*").handler(
			StaticHandler.create()
					.setAllowRootFileSystemAccess(false)
					.setAlwaysAsyncFS(true)
					.setCachingEnabled(true)
					.setDefaultContentEncoding("UTF-8")
					.setDirectoryListing(false)
					.setEnableRangeSupport(true)
	)
}