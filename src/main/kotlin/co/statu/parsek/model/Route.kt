package co.statu.parsek.model

import io.vertx.core.Handler
import io.vertx.ext.web.RoutingContext
import io.vertx.ext.web.handler.BodyHandler
import io.vertx.ext.web.validation.ValidationHandler
import io.vertx.ext.web.validation.builder.ValidationHandlerBuilder
import io.vertx.json.schema.SchemaRepository

abstract class Route {
    open val order = 1

    abstract val paths: List<Path>

    abstract fun getHandler(): Handler<RoutingContext>

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

    enum class Type {
        THEME_UI,
        PANEL_UI,
        SETUP_UI
    }
}