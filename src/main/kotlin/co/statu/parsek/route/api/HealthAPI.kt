package co.statu.parsek.route.api

import co.statu.parsek.annotation.Endpoint
import co.statu.parsek.model.*
import io.vertx.ext.web.RoutingContext
import io.vertx.ext.web.validation.ValidationHandler
import io.vertx.json.schema.SchemaRepository

@Endpoint
class HealthAPI : Api() {
    override val paths = listOf(Path("/health", RouteType.GET))

    override fun getValidationHandler(schemaRepository: SchemaRepository): ValidationHandler? = null

    override suspend fun handle(context: RoutingContext): Result {
        return Successful()
    }
}