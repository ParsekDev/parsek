package co.statu.parsek.util

import java.time.Instant
import java.util.*

object DateUtil {
    fun getTodayInMillis(): Long {
        val calendar = Calendar.getInstance()
        calendar[Calendar.HOUR_OF_DAY] = 0
        calendar[Calendar.MINUTE] = 0
        calendar[Calendar.SECOND] = 0
        calendar[Calendar.MILLISECOND] = 0

        return calendar.timeInMillis
    }

    fun convertISO8601ToMillis(iso8601Date: String) = Instant
        .parse(iso8601Date)
        .toEpochMilli()
}