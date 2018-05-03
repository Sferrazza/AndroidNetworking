package com.nexcom.nexcomnetworking.NXCore

import android.content.Context
import android.util.Log
import com.nexcom.NXCore.*
import kotlinx.coroutines.experimental.Deferred
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.async
import org.jetbrains.anko.coroutines.experimental.bg
import org.joda.time.LocalDate
import java.time.LocalDateTime
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

    private fun getCachedValue():CachedValue? {

        if (context.fileList().contains(fileKey)) {

            val json = context.openFileInput(fileKey).bufferedReader().use { it.readText() }

            return fromJson(json)
        }

        return null
    }

    private fun setCachedValue(value: CachedValue?) {

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

        async(UI) {

            val dCachedValue : Deferred<CachedValue?> = bg {

                getCachedValue()
            }

            val cachedValue = dCachedValue.await()

            if (cachedValue == null) {

                if (isDebug) { Log.d(LOG_TAG,"No cached value exists for RPC $rpc. Calling server...") }

                super.sendRequest(completionHandler, errorHandler)

            }
            else {
                val refreshDate = org.joda.time.LocalDateTime(cachedValue.dateString)

                shouldRefresh(refreshDate, responseHandler = { shouldRefresh ->

                    if (shouldRefresh) {

                        if (isDebug) { Log.d(LOG_TAG,"Cached value for $fileKey is out of date.") }

                        super.sendRequest(completionHandler, errorHandler)
                    }
                    else {
                        if (isDebug) { Log.d(LOG_TAG,"Cached value is valid for $fileKey.") }

                        val models = parseResponse(cachedValue.valueString, errorHandler)

                        completionHandler(models)
                    }
                }, errorHandler = errorHandler)
            }
        }
    }

    private fun shouldRefresh(refreshDate : org.joda.time.LocalDateTime, responseHandler : (shouldRefresh : Boolean)->Unit, errorHandler: (error: Error) -> Unit) {

        val dataManager = NXRefreshDateDataManager(network,refreshRPC,parameters)

        dataManager.isDebug = true

        dataManager.sendRequest(completionHandler = { dates ->

            val latestDate = org.joda.time.LocalDateTime(dates.first().refreshDate)

            val isAfter = latestDate.isAfter(refreshDate)

            if (isDebug) {
                Log.d(LOG_TAG,"ServerDate({$latestDate}) is after RefreshDate({$refreshDate}) $isAfter")
            }

            responseHandler( isAfter )

        }, errorHandler = errorHandler)
    }

    /**
     * Caches the raw response.
     */
    override fun handleRawResponse(responseString: String) {

        bg {
            val dateString = org.joda.time.LocalDateTime.now().toString()

            setCachedValue(CachedValue(dateString,responseString))
        }
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