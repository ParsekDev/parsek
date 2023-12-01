package co.statu.parsek.error

import co.statu.parsek.model.Error

class PageNotFound(
    statusMessage: String = "",
    extras: Map<String, Any> = mapOf()
) : Error(404, statusMessage, extras)