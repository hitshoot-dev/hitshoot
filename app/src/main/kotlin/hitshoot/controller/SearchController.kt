package hitshoot.controller

import hitshoot.api.Search
import hitshoot.util.*
import io.vertx.ext.web.RoutingContext
import kotlin.math.ceil

/**
 * Controller for the search page
 * @since 1.0.0
 */
class SearchController {
	suspend fun handle(r: RoutingContext) {
		val params = r.request().params()

		if(params.contains("query") && params["query"].trim().isNotEmpty()) {
			r.put("showForm", false)

			// Collect info
			val page = try {
				params["page"].toInt()
			} catch(e: Throwable) {
				1
			}.coerceAtLeast(1)
			val query = params["query"].trim()
			val kind = when(params["kind"]) {
				"channel" -> Search.Kind.CHANNEL
				else -> Search.Kind.VIDEO
			}
			val duration = when(params["duration"]) {
				"short" -> Search.Duration.SHORT
				"medium" -> Search.Duration.MEDIUM
				"long" -> Search.Duration.LONG
				"feature" -> Search.Duration.FEATURE
				else -> Search.Duration.ANY
			}
			val sort = when(params["sort"]) {
				"new" -> Search.Sort.NEW
				"old" -> Search.Sort.OLD
				else -> Search.Sort.RELEVANCE
			}

			// Put params in context
			r
					.put("query", query)
					.put("kind", kind.toString())
					.put("duration", duration.toString())
					.put("sort", sort.toString())

			// Validate query
			if(query.length < 3) {
				r.errorMessage("Query is too short, queries need to be at least 3 characters long")
				return
			}

			// Fetch search
			val res = Search.search(query, kind, duration, sort)

			// Pagination info
			val pages = ceil(res.total.toDouble()/res.count).toInt()
			val nextPage = if(page >= pages) null else r.request().path()+'?'+mapToQueryParams(mapOf(
					"query" to query,
					"kind" to kind,
					"duration" to duration,
					"sort" to sort,
					"page" to page+1
			))
			val lastPage = if(page <= 1) null else r.request().path()+'?'+mapToQueryParams(mapOf(
					"query" to query,
					"kind" to kind,
					"duration" to duration,
					"sort" to sort,
					"page" to page-1
			))

			// If page is more than the total pages, go to 404
			if(res.results.isNotEmpty() && page > pages) {
				r.noRender()
				r.next()
				return
			}

			// Page title
			r.pageTitle("\"$query\" - Search")

			// Put context data
			r.putPaginationInfo(page, page, nextPage, lastPage)
			r
					.put("results", res.results.toList())
					.put("totalResults", res.total)
					.put("resultsCount", res.count)
		} else {
			r.pageTitle("Search")
			r.put("showForm", true)
		}
	}
}