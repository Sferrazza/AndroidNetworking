package com.nexcom.NXCore

import com.github.salomonbrys.kotson.registerTypeAdapter
import com.google.gson.*
import org.threeten.bp.LocalDate
import org.threeten.bp.ZonedDateTime


/**
 * Created by danielmeachum on 1/8/18.
 */

open class NXModelParser
{
    val gson: Gson
        get() = GsonBuilder().registerTypeAdapter<ZonedDateTime> {
            serialize {
                JsonPrimitive(it.src.toJsonString())
            }
            deserialize {
                it.json.asString.toZonedDateTime()
            }
        }.registerTypeAdapter<LocalDate> {
            serialize {
                JsonPrimitive(it.src.toJsonString())
            }
            deserialize {
                it.json.asString.toLocalDate()
            }
        }.create()

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
