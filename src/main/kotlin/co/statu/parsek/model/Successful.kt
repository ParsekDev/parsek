package co.statu.parsek.model

import co.statu.parsek.model.Result.Companion.encode

open class Successful(private val data: Any? = null, private val meta: Map<String, Any?>? = null) : Result {
    override fun encode(extras: Map<String, Any?>): String {
        val response = mutableMapOf<String, Any?>()

        if (data != null) {
            response["data"] = data
        }

        if (meta != null) {
            response["meta"] = meta
        }

        response.putAll(extras)

        return response.encode()
    }

    override fun getStatusCode() = 200

    override fun getStatusMessage() = ""
}