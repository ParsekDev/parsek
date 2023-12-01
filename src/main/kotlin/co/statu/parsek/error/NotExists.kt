package co.statu.parsek.error

import co.statu.parsek.model.Error

class NotExists(
    statusMessage: String = "",
    extras: Map<String, Any> = mapOf()
) : Error(404, statusMessage, extras)