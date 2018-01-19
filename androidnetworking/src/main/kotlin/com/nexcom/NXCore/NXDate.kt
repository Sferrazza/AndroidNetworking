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

fun Date.toJsonString(): String {

    return jsonDateFormat.format(this)
}

@Target(AnnotationTarget.FIELD)
annotation class NXDate


fun nxJsonParser() = Klaxon()
        .fieldConverter(NXDate::class, object  : Converter<Date> {

            override fun fromJson(jv: JsonValue): Date{

                if (jv.string != null){

                    return jsonDateFormat.parse(jv.string)
                }
                else {
                    throw KlaxonException("Couldn't parse date " + jv.string)
                }
            }

            override fun toJson(value: Date): String? {

                return value.toJsonString()
            }
        })