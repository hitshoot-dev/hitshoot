package hitshoot.middleware

import io.vertx.ext.web.RoutingContext

/**
 * Middleware that writes page security headers
 * @since 1.0.0
 */
class SecurityMiddleware {
	fun handler(r: RoutingContext) {
		r.response().putHeader("Content-Security-Policy", "default-src 'self' *.bitchute.com bitchute.com; media-src 'self' *.bitchute.com bitchute.com; img-src 'self' *.bitchute.com bitchute.com; script-src 'self'")
	}
}