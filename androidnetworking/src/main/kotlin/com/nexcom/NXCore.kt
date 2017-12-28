package com.nexcom

import com.github.kittinunf.fuel.core.FuelError
import com.github.kittinunf.fuel.httpGet

/**
 * Created by danielmeachum on 12/21/17.
 */

open class NXNetworkRequest(rpc : String?, parameters: List<Pair<String, String>>? = null, method : String = "get")
{
    var rpc = rpc

    var parameters = parameters

    var method = method

    var isDebug = false


    open fun send(withManager: NXNetworkManager? = null, completionHandler : (String)->Unit, errorHandler : (FuelError)->Unit) {

        var manager = withManager
        if (manager == null) {
            manager = NXNetworkManager.evolveJsonManager
        }

        val initialParameters = parameters ?: listOf()

        var allParameters = initialParameters.toMutableList()

        if (rpc != null) {
            allParameters.add(Pair("rpc",rpc!!))
        }

        var environment= manager.nexcomEnvironment

        if (environment!= null) {

            allParameters.addAll(listOf("sitetoken" to environment.sitetoken, "sessionid" to environment.sessionid))
        }

        val urlString = manager.urlString


        urlString.httpGet(allParameters).responseString { _, response, result ->

            println("Request: " + response.toString())


            val (json, error) = result

            if (json != null) {

                completionHandler(json)
            }
            else if (error != null) {

                println("Error getting json " + error)

                errorHandler(error)
            }
        }
    }
}

data class NXNexcomEnvironment(val sitetoken : String, val sessionid : String)
{

}

public class NXNetworkManager(val scheme : String = "http", val host : String, val path : String)
{
    public var nexcomEnvironment : NXNexcomEnvironment? = null

    companion object {

        var evolveJsonManager = NXNetworkManager("http://", "evolve.nexcomgroup.com", "/apps/demo/iOS/aspx/json.aspx")

    }

    val urlString : String
        get() = scheme + host + path
}
