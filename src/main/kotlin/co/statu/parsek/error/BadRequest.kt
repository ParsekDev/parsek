package co.statu.parsek.error

import co.statu.parsek.model.Error

class BadRequest(
    statusMessage: String = "",
    extras: Map<String, Any> = mapOf()
) : Error(400, statusMessage, extras)