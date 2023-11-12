package co.statu.parsek.model

import co.statu.parsek.model.Result.Companion.encode

class Errors(val errors: Map<String, Error>) : Throwable(), Result {
    override fun encode(extras: Map<String, Any?>): String {
        val response = mutableMapOf<String, Any?>(
            "result" to "errors",
            "errors" to errors.map { it.key to it.value.getErrorCode() }.toMap()
        )

        response.putAll(extras)

        return response.encode()
    }

    override fun getStatusCode() = 400

    override fun getStatusMessage() = ""
}