package hitshoot.route

import hitshoot.App.apiClient
import hitshoot.App.vertx
import hitshoot.util.bitchuteLinkPattern
import hitshoot.util.replaceWithFunction
import hitshoot.util.urlToMediaLink
import io.vertx.ext.web.Router
import io.vertx.ext.web.handler.StaticHandler
import io.vertx.kotlin.coroutines.await
import io.vertx.kotlin.coroutines.dispatcher
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

/**
 * Sets up channel RSS routes
 * @param router The router to set them up on
 * @since 1.0.0
 */
@DelicateCoroutinesApi
fun setupRssRoutes(router: Router) {
	router.route("/channel/:channel/rss/").handler { r ->
		GlobalScope.launch(vertx.dispatcher()) {
			try {
				val channel = r.pathParam("channel")

				// Request channel RSS
				val res = apiClient.webClient.getAbs("${apiClient.siteRoot}/feeds/rss/channel/$channel/")
						.followRedirects(true)
						.send()
						.await()

				if(res.statusCode() == 404) {
					r.next()
				} else if(res.statusCode() == 200) {
					// Replace links
					var xml = res.bodyAsString().replaceWithFunction(bitchuteLinkPattern) { m ->
						val str = m.group(0)
						val prefix = if(r.request().headers().contains("Host"))
							"http://"+r.request().getHeader("Host")
						else
							""

						if(str.contains("/embed/")) {
							prefix+"/video/"+str.substring(str.indexOf("/embed/")+7)
						} else if(str.contains("/feeds/rss/channel/")) {
							prefix+"/channel/"+str.substring(str.indexOf("/feeds/rss/channel/")+19)+"rss/"
						} else if(str.contains(".com/channel/")) {
							prefix+"/channel/"+str.substring(str.indexOf(".com/channel/")+13)
						} else {
							prefix+urlToMediaLink(str)
						}
					}

					// Set headers
					r.response().putHeader("Content-Type", "application/rss+xml")

					// Send response
					r.end(xml).await()
				} else {
					r.fail(res.statusCode())
				}
			} catch(e: Throwable) {
				r.fail(e)
			}
		}
	}
}