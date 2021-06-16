package hitshoot.api

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import hitshoot.App.apiClient
import io.vertx.core.http.HttpMethod

/**
 * Search API
 * @since 1.0.0
 */
object Search {
	/**
	 * Types of results to return
	 * @since 1.0.0
	 */
	enum class Kind {
		/**
		 * Videos
		 * @since 1.0.0
		 */
		VIDEO,

		/**
		 * Channels
		 * @since 1.0.0
		 */
		CHANNEL;

		override fun toString() = name.toLowerCase()
	}

	/**
	 * Lengths of videos to return
	 */
	enum class Duration {
		/**
		 * Any duration
		 * @since 1.0.0
		 */
		ANY,

		/**
		 * 0-5 minutes
		 * @since 1.0.0
		 */
		SHORT,

		/**
		 * 5-20 minutes
		 * @since 1.0.0
		 */
		MEDIUM,

		/**
		 * 20+ minutes
		 * @since 1.0.0
		 */
		LONG,

		/**
		 * 45+ minutes
		 * @since 1.0.0
		 */
		FEATURE;

		override fun toString() = if(this == ANY) "" else name.toLowerCase()
	}

	/**
	 * The method to sort results by
	 * @since 1.0.0
	 */
	enum class Sort {
		/**
		 * However BitChute sees fit
		 * @since 1.0.0
		 */
		RELEVANCE,

		/**
		 * Newest first
		 * @since 1.0.0
		 */
		NEW,

		/**
		 * Oldest first
		 * @since 1.0.0
		 */
		OLD;

		override fun toString() = if(this == RELEVANCE) "" else name.toLowerCase()
	}

	@JsonIgnoreProperties("success")
	class SearchResult {
		var url: String = ""
		var count: Int = 0
		var total: Int = 0
		var duration: Double = 0.0

		class Result {
			var kind: String = "video"
			var id: String = ""
			var name: String = ""
			var description: String? = null
			var published: String = ""
			var sensitivity: String = ""
			var views: String = ""
			var duration: String = ""
			var path: String = ""
			var subscribers: String = ""

			class Image {
				var thumbnail: String = ""
				var loading: String = ""
				var action: String = ""
				var error: String = ""
				var sensitivity: String = ""
			}
			var images: Image = Image()
			@JsonProperty("channel_name")
			var channelName: String = ""
			@JsonProperty("channel_path")
			var channelPath: String = ""
			@JsonProperty("creator_name")
			var creatorName: String = ""
			@JsonProperty("creator_path")
			var creatorPath: String = ""
		}
		var results: Array<Result> = arrayOf()
	}

	/**
	 * Performs a search with the specified query and returns the results
	 * @param query The query
	 * @param kind What kind of results to return
	 * @param duration The duration of videos to return (does nothing if kind if CHANNEL)
	 * @param sort How to sort results
	 * @param page The page of results to return (starts at 1)
	 * @return The query results
	 * @since 1.0.0
	 */
	suspend fun search(
			query: String,
			kind: Kind = Kind.VIDEO,
			duration: Duration = Duration.ANY,
			sort: Sort = Sort.RELEVANCE,
			page: Int = 1
	): SearchResult {
		// Perform search
		val json = apiClient.requestJson(HttpMethod.POST, "/api/search/list/", hashMapOf(
				"query" to query,
				"kind" to kind.toString(),
				"sort" to sort.toString(),
				"page" to (page.coerceAtLeast(1)-1).toString()
		).apply {
			if(kind == Kind.VIDEO)
				put("duration", duration.toString())
		})

		// Serialize result
		return json.mapTo(SearchResult::class.java)
	}
}