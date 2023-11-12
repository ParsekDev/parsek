package co.statu.parsek.model

import co.statu.parsek.model.Result.Companion.encode
import co.statu.parsek.util.TextUtil.convertToSnakeCase

abstract class Error(
    private val statusCode: Int = 500,
    private val statusMessage: String = "",
    private val extras: Map<String, Any?> = mapOf()
) : Throwable(), Result {

    override fun encode(extras: Map<String, Any?>): String {
        val response = mutableMapOf<String, Any?>(
            "result" to "error",
            "error" to getErrorCode()
        )

        response.putAll(this.extras)
        response.putAll(extras)

        return response.encode()
    }

    override fun getStatusCode(): Int = statusCode

    override fun getStatusMessage(): String = statusMessage

    fun getErrorCode() = javaClass.simpleName.convertToSnakeCase().uppercase()
}