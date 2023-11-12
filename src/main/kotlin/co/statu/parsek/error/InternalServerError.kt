package co.statu.parsek.error

import co.statu.parsek.model.Error

class InternalServerError(
    statusMessage: String = "",
    extras: Map<String, Any> = mapOf()
) : Error(statusMessage = statusMessage, extras = extras)