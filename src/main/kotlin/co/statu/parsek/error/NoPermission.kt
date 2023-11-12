package co.statu.parsek.error

import co.statu.parsek.model.Error

class NoPermission(
    statusMessage: String = "",
    extras: Map<String, Any> = mapOf()
) : Error(401, statusMessage, extras)