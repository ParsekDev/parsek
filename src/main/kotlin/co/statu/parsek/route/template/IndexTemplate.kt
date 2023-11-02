package co.statu.parsek.route.template

import co.statu.parsek.annotation.Endpoint
import co.statu.parsek.model.Path
import co.statu.parsek.model.RouteType
import co.statu.parsek.model.Template
import io.vertx.core.Handler
import io.vertx.ext.web.RoutingContext

@Endpoint
class IndexTemplate : Template() {
    private val mHotLinks = mapOf<String, String>()

    override val paths = listOf(Path("/*", RouteType.ROUTE))

    override val order = 999

    override fun getHandler() = Handler<RoutingContext> { context ->
        val response = context.response()
        val normalisedPath = context.normalizedPath()

        if (!mHotLinks[normalisedPath.lowercase()].isNullOrEmpty()) {
            response.putHeader(
                "location",
                mHotLinks[normalisedPath.lowercase()]
            ).setStatusCode(302).end()

            return@Handler
        }

        response.end("Welcome to Parsek!")
    }
}