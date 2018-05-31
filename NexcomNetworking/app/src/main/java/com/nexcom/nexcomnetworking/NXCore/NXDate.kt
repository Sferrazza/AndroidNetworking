package com.nexcom.NXCore

import com.beust.klaxon.*
import java.text.SimpleDateFormat
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.*

/**
 * Created by danielmeachum on 1/4/18.
 */

val jsonDateFormat = "yyyy-MM-dd'T'HH:mm:ssZ"

fun ZonedDateTime.toJsonString(): String {

    return DateTimeFormatter.ofPattern(jsonDateFormat).format(this)
}

fun String.toZonedDateTime(): ZonedDateTime {

    return ZonedDateTime.parse(this, DateTimeFormatter.ofPattern(jsonDateFormat))
}

@Target(AnnotationTarget.FIELD)
annotation class NXDate


fun nxJsonParser() = Klaxon()
        .fieldConverter(NXDate::class,object  : Converter {

            override fun canConvert(cls: Class<*>) = cls == ZonedDateTime::class.java

            override fun fromJson(jv: JsonValue) =
                    if (jv.string != null) {
                        ZonedDateTime.parse(jv.string, DateTimeFormatter.ofPattern(jsonDateFormat))
                    } else {
                        throw KlaxonException("Couldn't parse date: ${jv.string}")
                    }

            override fun toJson(value: Any)
                    = when (value) {
                is ZonedDateTime -> value.toJsonString()
                else -> value.toString()
            }
        })