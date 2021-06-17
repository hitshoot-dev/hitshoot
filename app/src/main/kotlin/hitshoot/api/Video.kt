package hitshoot.api

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import hitshoot.App.apiClient
import io.vertx.core.http.HttpMethod
import org.jsoup.Jsoup
import java.util.regex.Pattern

/**
 * Video API
 * @since 1.0.0
 */
object Video {
	@JsonIgnoreProperties("success")
	class VideoResult {
		var html: String = ""
		var title: String = ""
		var canonical: String = ""
		var published: String = ""
		var videoUrl: String = ""
		var descriptionHtml: String = ""
		var hashtags: Array<String> = arrayOf()
		var cfAuth: String = ""
		var cfThread: String = ""

		var channelImage: String = ""
		var channelName: String = ""
		var channelLink: String = ""
		var profileName: String = ""
		var profileLink: String = ""

		class Meta {
			var url: String = ""
			var title: String = ""
			var description: String? = ""
			var type: String = ""
			var image: String = ""
			@JsonProperty("image_secure_url")
			var imageSecureUrl: String = ""
			@JsonProperty("image_width")
			var imageWidth: String = ""
			@JsonProperty("image_height")
			var imageHeight: String = ""
			var oembed: String = ""
			@JsonProperty("display_id")
			var displayId: String = ""
		}
		var meta: Meta = Meta()
	}

	/**
	 * Fetches a video by its ID
	 * @param id The video ID
	 * @return The video result
	 * @since 1.0.0
	 */
	suspend fun fetchVideo(id: String): VideoResult {
		// Fetch video
		val json = apiClient.requestJson(HttpMethod.POST, "/video/$id/")

		// Serialize result
		val res = json.mapTo(VideoResult::class.java)

		// Parse HTML and extract important information
		val html = Jsoup.parse(res.html)
		val vid = html.getElementById("player")
		val src = vid.getElementsByTag("source")[0]
		val full = html.getElementsByClass("full")[0]

		res.videoUrl = src.attr("src")
		res.descriptionHtml = full.html()

		val channelBanner = html.getElementsByClass("channel-banner")[0]
		val chanImg = channelBanner.getElementsByClass("image")[0]
		val chanName = channelBanner.getElementsByClass("name")[0].children()[0]
		val chanOwner = channelBanner.getElementsByClass("owner")[0].children()[0]

		res.channelImage = chanImg.attr("data-src")
		res.channelName = chanName.text()
		res.channelLink = chanName.attr("href")
		res.profileName = chanOwner.text()
		res.profileLink = chanOwner.attr("href")

		val published = html.getElementsByClass("video-publish-date")[0]

		res.published = published.text()

		val hashTags = html.getElementById("video-hashtags").getElementsByTag("a")
		val arr = Array(hashTags.size) { "" }
		for((i, elem) in hashTags.withIndex())
			arr[i] = elem.text()

		res.hashtags = arr

		// Find CommentFreely info
		var cfAuth = res.html.substring(res.html.indexOf("cf_auth: '")+9)
		cfAuth = cfAuth.substring(0, cfAuth.indexOf('\''))
		var cfThread = res.html.substring(res.html.indexOf("cf_thread: '")+11)
		cfThread = cfThread.substring(0, cfThread.indexOf('\''))

		res.cfAuth = cfAuth
		res.cfThread = cfThread

		return res
	}

	@JsonIgnoreProperties("success")
	class CountsResult {
		@JsonProperty("view_count")
		var viewCount: Int = 0
		@JsonProperty("like_count")
		var likeCount: Int = 0
		@JsonProperty("dislike_count")
		var dislikeCount: Int = 0
		@JsonProperty("subscriber_count")
		var subscriberCount: Int = 0
	}

	/**
	 * Fetches various counts for the specified video
	 * @param id The video ID
	 * @return The counts result
	 * @since 1.0.0
	 */
	suspend fun fetchCounts(id: String): CountsResult {
		// Fetch counts
		val json = apiClient.requestJson(HttpMethod.POST, "/video/$id/counts/")

		// Serialize result
		return json.mapTo(CountsResult::class.java)
	}
}