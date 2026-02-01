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

    open val allowedSchemes: Set<String> = emptySet()
    open val allowedHosts: Set<String> = emptySet()

    open val allowedHeaders: Set<String> = emptySet()

    open val allowedMethods: Set<HttpMethod> = emptySet()

    open fun corsHandler(
        hosts: Set<String> = allowedHosts,
        schemes: Set<String> = allowedSchemes,
        headers: Set<String> = allowedHeaders,
        methods: Set<HttpMethod> = allowedMethods
    ): Handler<RoutingContext>? = Handler { ctx ->
        val origin = ctx.request().getHeader("Origin")
        if (origin != null) {
            try {
                val uri = URI(origin)
                // Check the scheme and host
                if (uri.scheme in schemes && uri.host in hosts) {
                    // If the origin is allowed, add it to the response header
                    ctx.response().putHeader("Access-Control-Allow-Origin", origin)
                }
            } catch (_: Exception) {
                // If the URI cannot be parsed, do not add any header.
            }
        }

        // Add the allowed methods to the header:
        val methodsAsString = methods.joinToString(",") { it.name() }
        ctx.response().putHeader("Access-Control-Allow-Methods", methodsAsString)

        // Add the allowed headers to the header:
        val headersAsString = headers.joinToString(",")
        ctx.response().putHeader("Access-Control-Allow-Headers", headersAsString)

        // Set Access-Control-Allow-Credentials to true
        ctx.response().putHeader("Access-Control-Allow-Credentials", "true")

        // If it's a Preflight (OPTIONS) request, end the response immediately:
        if (ctx.request().method() == HttpMethod.OPTIONS) {
            ctx.response().end()
        } else {
            ctx.next()
        }
    }

    open fun corsHandler(): Handler<RoutingContext>? = corsHandler()

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