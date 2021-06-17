package hitshoot.controller

import hitshoot.api.Video
import hitshoot.util.noRender
import hitshoot.util.pageTitle
import io.vertx.ext.web.RoutingContext

/**
 * Controller for the video page
 * @since 1.0.0
 */
class VideoController {
	suspend fun get(r: RoutingContext) {
		val id = r.pathParam("id")

		// Fetch video
		val video: Video.VideoResult
		try {
			video = Video.fetchVideo(id)
		} catch(e: Throwable) {
			// Check if the video just doesn't exist
			if(e.message!!.contains("JSON")) {
				r.noRender()
				r.next()
				return
			} else {
				throw e
			}
		}

		// Fetch counts
		val counts = Video.fetchCounts(id)

		// Set page title
		r.pageTitle(video.meta.title)

		// Put render data
		r
				.put("originalUrl", video.canonical)
				.put("descriptionHtml", video.descriptionHtml)
				.put("cfAuth", video.cfAuth)
				.put("cfThread", video.cfThread)
				.put("videoUrl", video.videoUrl)
				.put("published", video.published)
				.put("hashtags", video.hashtags)
				.put("description", video.meta.description)
				.put("id", video.meta.displayId)
				.put("image", video.meta.image)
				.put("imageWidth", video.meta.imageWidth.toInt())
				.put("imageHeight", video.meta.imageHeight.toInt())
				.put("title", video.meta.title)
				.put("viewCount", counts.viewCount)
				.put("likeCount", counts.likeCount)
				.put("dislikeCount", counts.dislikeCount)
				.put("subscriberCount", counts.subscriberCount)
				.put("channelImage", video.channelImage)
				.put("channelName", video.channelImage)
				.put("channelLink", video.channelLink)
				.put("profileName", video.profileName)
				.put("profileLink", video.profileLink)
	}
}