package co.statu.parsek.model

import io.vertx.core.Handler
import io.vertx.core.http.HttpMethod
import io.vertx.ext.web.RoutingContext
import io.vertx.ext.web.handler.BodyHandler
import io.vertx.ext.web.validation.ValidationHandler
import io.vertx.ext.web.validation.builder.ValidationHandlerBuilder
import io.vertx.json.schema.SchemaRepository
import java.net.URI

abstract class Route {
    open val order = 1

    abstract val paths: List<Path>

    abstract fun getHandler(): Handler<RoutingContext>

    open val allowedSchemes = setOf("http", "https")
    open val allowedHosts = setOf("localhost", "127.0.0.1", "0.0.0.0")

    open val allowedHeaders = setOf(
        "x-requested-with",
        "Access-Control-Allow-Origin",
        "origin",
        "Content-Type",
        "accept",
        "X-PINGARUNER",
        "x-csrf-token"
    )

    open val allowedMethods = setOf<HttpMethod>(
        HttpMethod.GET,
        HttpMethod.POST,
        HttpMethod.OPTIONS,
        HttpMethod.DELETE,
        HttpMethod.PATCH,
        HttpMethod.PUT
    )

    open fun corsHandler(): Handler<RoutingContext>? = Handler { ctx ->
        val origin = ctx.request().getHeader("Origin")
        if (origin != null) {
            try {
                val uri = URI(origin)
                // Check the scheme and host
                if (uri.scheme in allowedSchemes && uri.host in allowedHosts) {
                    // If the origin is allowed, add it to the response header
                    ctx.response().putHeader("Access-Control-Allow-Origin", "*")
                }
            } catch (_: Exception) {
                // If the URI cannot be parsed, do not add any header.
            }
        }

        // Add the allowed methods to the header:
        val methodsAsString = allowedMethods.joinToString(",") { it.name() }
        ctx.response().putHeader("Access-Control-Allow-Methods", methodsAsString)

        // Add the allowed headers to the header:
        val headersAsString = allowedHeaders.joinToString(",")
        ctx.response().putHeader("Access-Control-Allow-Headers", headersAsString)

        // If it's a Preflight (OPTIONS) request, end the response immediately:
        if (ctx.request().method() == HttpMethod.OPTIONS) {
            ctx.response().end()
        } else {
            ctx.next()
        }
    }

    open fun bodyHandler(): Handler<RoutingContext>? = BodyHandler.create()

    open fun getValidationHandler(schemaRepository: SchemaRepository): ValidationHandler? =
        ValidationHandlerBuilder.create(schemaRepository).build()

    open fun getFailureHandler(): Handler<RoutingContext> = Handler { request ->
        val response = request.response()

        if (response.ended()) {
            return@Handler
        }

        response.end()
    }
}