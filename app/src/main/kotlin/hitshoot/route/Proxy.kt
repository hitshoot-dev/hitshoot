package hitshoot.route

import hitshoot.App.apiClient
import hitshoot.App.configManager
import hitshoot.App.logger
import hitshoot.App.vertx
import hitshoot.Constants
import hitshoot.util.isBitChuteUrl
import io.vertx.ext.web.Router
import io.vertx.ext.web.client.predicate.ResponsePredicate
import io.vertx.ext.web.client.predicate.ResponsePredicateResult
import io.vertx.ext.web.codec.BodyCodec
import io.vertx.kotlin.coroutines.await
import io.vertx.kotlin.coroutines.dispatcher
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.IOException
import java.net.URL
import java.nio.channels.ClosedChannelException

/**
 * Sets up proxy routes
 * @param router The router to set them up on
 * @since 1.0.0
 */
@DelicateCoroutinesApi
fun setupProxyRoutes(router: Router) {
	router.get("/proxy").handler { r ->
		GlobalScope.launch(vertx.dispatcher()) {
			try {
				// Check if proxying is enabled
				if(configManager.config.app.proxyMedia) {
					val params = r.request().params()

					if(params.contains("url")) {
						val url = params["url"]

						try {
							if(isBitChuteUrl(url)) {
								val urlObj = URL(url)

								// Send HTTP request
								apiClient.webClient.getAbs(url)
										.followRedirects(true)
										.apply {
											// Put headers
											putHeader("Host", urlObj.host)
											for(header in Constants.PROXY_IN_HEADERS)
												if(r.request().headers().contains(header))
													putHeader(header, r.request().headers()[header])
										}
										.expect(ResponsePredicate { res ->
											// Validate status
											if(res.statusCode() in 200..299 || res.statusCode() in 300..309) {
												// Set status
												r.response().statusCode = res.statusCode()

												// Validate MIME
												if(res.headers().contains("Content-Type") && (res.headers()["Content-Type"].startsWith("image/") || res.headers()["Content-Type"].startsWith("video/") || res.headers()["Content-Type"].startsWith("audio/"))) {
													// Valid, send correct headers to client
													for(header in Constants.PROXY_OUT_HEADERS)
														if(res.headers().contains(header))
															r.response().putHeader(header, res.headers()[header])

													// Accept
													ResponsePredicateResult.success()
												} else {
													// Send failure
													if(!r.response().ended())
														r.response()
																.setStatusCode(502)
																.end("Cannot proxy ${res.headers()["Content-Type"]} MIME type")

													// Reject
													ResponsePredicateResult.failure("Server sent ${res.headers()["Content-Type"]} MIME type, not an approved type")
												}
											} else {
												// Send failure
												if(!r.response().ended())
													r.response()
															.setStatusCode(502)
															.end("Server sent status ${res.statusCode()}")

												// Reject
												ResponsePredicateResult.failure("Server responded with status code ${res.statusCode()}, not 20X")
											}
										})
										.`as`(BodyCodec.pipe(r.response()))
										.send().await()

								// Always make sure to end the request
								if(!r.response().ended())
									r.response().end()
							} else {
								r.response()
										.putHeader("Content-Type", "text/plain")
										.setStatusCode(400)
										.end("Not a BitChute URL")
							}
						} catch(e: Throwable) {
							if(e !is ClosedChannelException && e !is IOException) {
								logger.error("Error occurred while trying to proxy URL $url:")
								e.printStackTrace()

								if(!r.response().headWritten())
									r.response()
											.statusCode = 500
								if(!r.response().ended())
									r.response().end("Internal server error")
							}
						}
					} else {
						r.response()
								.putHeader("Content-Type", "text/plain")
								.setStatusCode(400)
								.end("No URL provided")
					}
				} else {
					r.response()
							.putHeader("Content-Type", "text/plain")
							.setStatusCode(400)
							.end("Media proxying is disabled on this instance")
				}
			} catch(e: Throwable) {
				r.fail(e)
			}
		}
	}
}