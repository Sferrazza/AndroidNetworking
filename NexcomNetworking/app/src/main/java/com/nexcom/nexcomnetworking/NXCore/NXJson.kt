package com.nexcom.NXCore

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import java.text.SimpleDateFormat
import java.util.*

/**
 * Created by danielmeachum on 1/8/18.
 */

open class NXModelParser
{
    val gson: Gson
        get() = GsonBuilder().setDateFormat(SimpleDateFormat(jsonDateFormat, Locale.US).toLocalizedPattern()).create()

    /**
     * Parses model into JSON string. Uses Nexcom's compatible date format.
     * @param[model] model to be parsed into JSON
     */
    fun toJson(model : Any) = gson.toJson(model)

    /**
     * Parses JSON string into class.
     * @param {String} json JSON string to be parsed
     */
    inline fun <reified U> fromJson(json : String) = gson.fromJson<U>(json, U::class.java)
}

interface NXJsonEncodable
{
    fun toJson() = NXModelParser().toJson(this)
}

/**
 * Parses JSON string into class.
 * @param {String} json JSON string to be parsed
 */
inline fun <reified U> fromJson(json : String) = NXModelParser().fromJson<U>(json)