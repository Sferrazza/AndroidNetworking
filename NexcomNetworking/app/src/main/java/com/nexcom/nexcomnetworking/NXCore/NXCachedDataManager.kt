package com.nexcom.nexcomnetworking.NXCore

import android.content.Context
import com.nexcom.NXCore.*
import java.util.*

/**
 * Subclass of [NXDatamanager] that allows data to be cached on disk.
 * Created by danielmeachum on 1/29/18.
 */

open class NXCachedDataManager<T>(val context: Context, network : NXNetwork, rpc : String, val refreshRPC: String, val cacheKey : String? = null, parameters : List<Pair<String,String>>?, method : String = "get") : NXDataManager<T>(network, rpc, parameters, method)
{
    private var fileKey : String

    init {
        fileKey = cacheKey ?: generateFileKey()
    }

    private var cachedValue : CachedValue?
        get() {

            if (context.fileList().contains(fileKey)) {

                val json = context.openFileInput(fileKey).bufferedReader().use { it.readText() }

                return fromJson(json)
            }

            return null
        }
        set(value) {

            if (value != null) {
                context.openFileOutput(fileKey, 0).use { it.write(value.toJson().toByteArray()) }
            } else {
                context.deleteFile(fileKey)
            }

        }

    private fun generateFileKey(): String {

        val parameters = parameters

        var parameterString = ""

        if (parameters != null) {

            parameterString = parameters.map {

                return it.first + ":" + it.second

            }.joinToString("|")
        }

        return "CachedRPC($rpc)RefreshRPC($refreshRPC)Parameters($parameterString).cache"
    }

    override fun sendRequest(completionHandler: (models: List<T>) -> Unit, errorHandler: (error: Error) -> Unit) {

        val cachedValue = cachedValue

        if (cachedValue == null) {

            super.sendRequest(completionHandler, errorHandler)
            return
        }

        val refreshDate = jsonDateFormat.parse(cachedValue.dateString)

        shouldRefresh(refreshDate, responseHandler = { shouldRefresh ->

            if (shouldRefresh) {

                println("Cached value for $fileKey is out of date.")

                super.sendRequest(completionHandler, errorHandler)
            }
            else {
                println("Cached value is valid.")

                val models = parseResponse(cachedValue.valueString, errorHandler)

                completionHandler(models)
            }
        }, errorHandler = errorHandler)
    }

    private fun shouldRefresh(refreshDate : Date, responseHandler : (shouldRefresh : Boolean)->Unit, errorHandler: (error: Error) -> Unit) {

        val dataManager = NXRefreshDateDataManager(network,refreshRPC,parameters)

        dataManager.isDebug = true

        dataManager.sendRequest(completionHandler = { dates ->

            val latestDate = dates.first().refreshDate

            responseHandler( latestDate.after(refreshDate) )

        }, errorHandler = errorHandler)
    }

    /**
     * Caches the raw response.
     */
    override fun handleRawResponse(responseString: String) {

        cachedValue = CachedValue(jsonDateFormat.format(Date()),responseString)
        super.handleRawResponse(responseString)
    }
}

/**
 * Wrapper class of a cached value and its cached date.
 * Used by [NXCachedDataManager]
 */
data class CachedValue(
        val dateString : String,
        val valueString : String
) : NXJsonEncodable