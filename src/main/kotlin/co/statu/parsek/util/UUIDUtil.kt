package co.statu.parsek.util

import co.statu.parsek.error.NotExists
import java.util.*

object UUIDUtil {
    fun validate(uuid: String) {
        try {
            UUID.fromString(uuid)
        } catch (e: IllegalArgumentException) {
            throw NotExists()
        }
    }
}