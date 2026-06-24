package com.whatshub.utils

import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.time.temporal.ChronoUnit

object DateTimeUtils {

    fun relativeChatTime(isoTimestamp: String?): String {
        if (isoTimestamp.isNullOrBlank()) return ""
        return runCatching {
            val zone = ZoneId.systemDefault()
            val instant = Instant.parse(isoTimestamp)
            val dateTime = instant.atZone(zone)
            val now = ZonedDateTime.now(zone)

            when {
                dateTime.toLocalDate() == now.toLocalDate() ->
                    dateTime.format(DateTimeFormatter.ofPattern("HH:mm"))
                ChronoUnit.DAYS.between(dateTime.toLocalDate(), now.toLocalDate()) < 7 ->
                    dateTime.format(DateTimeFormatter.ofPattern("EEEE"))
                else ->
                    dateTime.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT))
            }
        }.getOrDefault("")
    }
}
