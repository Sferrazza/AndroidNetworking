package com.nexcom.NXCore

import com.beust.klaxon.*
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*

/**
 * Created by danielmeachum on 1/4/18.
 */

val jsonDateFormat = "yyyy-MM-dd'T'HH:mm:ssZ"

fun LocalDateTime.toJsonString(): String {

    return DateTimeFormatter.ofPattern(jsonDateFormat).format(this)
}

fun String.toLocalDateTime(): LocalDateTime {

    return LocalDateTime.parse(this, DateTimeFormatter.ofPattern(jsonDateFormat))
}

@Target(AnnotationTarget.FIELD)
annotation class NXDate


fun nxJsonParser() = Klaxon()
        .fieldConverter(NXDate::class,object  : Converter {

            override fun canConvert(cls: Class<*>) = cls == LocalDateTime::class.java

            override fun fromJson(jv: JsonValue) =
                    if (jv.string != null) {
                        LocalDateTime.parse(jv.string, DateTimeFormatter.ofPattern(jsonDateFormat))
                    } else {
                        throw KlaxonException("Couldn't parse date: ${jv.string}")
                    }

            override fun toJson(value: Any)
                    = when (value) {
                is LocalDateTime -> value.toJsonString()
                else -> value.toString()
            }
        })