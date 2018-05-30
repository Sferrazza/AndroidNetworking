package com.nexcom.NXCore

import com.beust.klaxon.*
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*

/**
 * Created by danielmeachum on 1/4/18.
 */

val jsonDateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss",Locale.US)

fun LocalDateTime.toJsonString(): String {

    return DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").format(this)
}

fun LocalDateTime.dateFrom(string: String): LocalDateTime {

    return LocalDateTime.parse(string, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
}

@Target(AnnotationTarget.FIELD)
annotation class NXDate


fun nxJsonParser() = Klaxon()
        .fieldConverter(NXDate::class,object  : Converter {

            override fun canConvert(cls: Class<*>) = cls == LocalDateTime::class.java

            override fun fromJson(jv: JsonValue) =
                    if (jv.string != null) {
                        LocalDateTime.parse(jv.string, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
                    } else {
                        throw KlaxonException("Couldn't parse date: ${jv.string}")
                    }

            override fun toJson(o: Any)
                    = when (o) {
                is LocalDateTime -> o.toJsonString()
                else -> o.toString()
            }
        })