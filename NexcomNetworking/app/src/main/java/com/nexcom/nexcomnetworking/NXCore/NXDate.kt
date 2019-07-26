package com.nexcom.NXCore

import com.beust.klaxon.*
import java.text.SimpleDateFormat
import org.threeten.bp.LocalDate
import org.threeten.bp.ZonedDateTime
import org.threeten.bp.format.DateTimeFormatter
import java.util.*
import org.threeten.bp.ZoneId

/**
 * Created by danielmeachum on 1/4/18.
 */

val jsonDateFormat = "yyyy-MM-dd'T'HH:mm:ss"
val jsonDateOnlyFormat = "yyyy-MM-dd"

fun ZonedDateTime.toJsonString(): String {

    return DateTimeFormatter.ofPattern(jsonDateFormat).format(this)
}

fun String.toZonedDateTime(): ZonedDateTime {

    return ZonedDateTime.parse(this, DateTimeFormatter.ofPattern(jsonDateFormat).withZone(ZoneId.systemDefault()))
}

fun LocalDate.toJsonString(): String {

    return DateTimeFormatter.ofPattern(jsonDateOnlyFormat).format(this)
}

fun String.toLocalDate(): LocalDate {

    return LocalDate.parse(this, DateTimeFormatter.ofPattern(jsonDateOnlyFormat))
}

@Target(AnnotationTarget.FIELD)
annotation class NXDate

@Target(AnnotationTarget.FIELD)
annotation class NXDateOnly


fun nxJsonParser() = Klaxon()
        .fieldConverter(NXDate::class,object  : Converter {

            override fun canConvert(cls: Class<*>) = cls == ZonedDateTime::class.java

            override fun fromJson(jv: JsonValue) =
                    if (jv.string != null) {
                        ZonedDateTime.parse(jv.string, DateTimeFormatter.ofPattern(jsonDateFormat).withZone(ZoneId.systemDefault()))
                    } else {
                        throw KlaxonException("Couldn't parse date: ${jv.string}")
                    }

            override fun toJson(value: Any)
                    = when (value) {
                is ZonedDateTime -> value.toJsonString()
                else -> value.toString()
            }
        })
        .fieldConverter(NXDateOnly::class,object  : Converter {

            override fun canConvert(cls: Class<*>) = cls == LocalDate::class.java

            override fun fromJson(jv: JsonValue) =
                    if (jv.string != null) {
                        LocalDate.parse(jv.string, DateTimeFormatter.ofPattern(jsonDateFormat))
                    } else {
                        throw KlaxonException("Couldn't parse date: ${jv.string}")
                    }

            override fun toJson(value: Any)
                    = when (value) {
                is LocalDate -> value.toJsonString()
                else -> value.toString()
            }
        })