package com.nexcom.NXCore

import com.github.kittinunf.fuel.core.FuelError
import com.github.kittinunf.fuel.httpGet

/**
 * Created by danielmeachum on 12/21/17.
 */


/**
  * The base class for formatting requests into a Nexcom URI string and receiving its response in primitive form.
  * Use this to perform inline requests. Otherwise, subclass NXDataManager.
  * Set the isDebug property to true for observing the created URL string and its response.
  *
  * @param rpc          The remote procedure to be called on the server. Optional as not all aspx pages require an RPC.
  * @param parameters   List of key/value pairs to be passed into the RPC.
  * @param method       Specify http method get/post. Currently only get is supported but this will be updated soon.
  *
  * @author Daniel Meachum
  *
  * @type {NXNetworkRequest}
  * @see NXDataManager
  *
*/
open class NXNetworkRequest(rpc : String?, parameters: List<Pair<String, String>>? = null, method : String = "get")
{
    var rpc = rpc

    var parameters = parameters

    var method = method

    var isDebug = false

    /**
     * Sends network request to server. Allows for inline completion and error handling.
     * Note: completion handler returns the raw String value of the response.
     * Passing in a network manager allows inline customization of the server, sitetoken, sessionid, etc. If left null (as default), the default manager will be used.
     * In addition to parameters passed in constructor, network manager parameters such as sitetoken, sessionid, rpc, are added into the list of parameters in request.
     *
     * @param withManager         Specific network manager to use. If null, NXNetworkManager.evolveJsonManager is used.
     * @param completionHandler   If request returns a response, completionHandler is called with response as String value.
     * @param errorHandler        Allows for handling of networking errors.
     */
    open fun send(withManager: NXNetworkManager? = null, completionHandler : (String)->Unit, errorHandler : (FuelError)->Unit) {

        var manager = withManager
        if (manager == null) {
            manager = NXNetworkManager.defaultManager
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

            if (isDebug) {
                
                println("URL Request: " + urlString)
                println("Response: " + response.toString())
            }


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

/**
 * Abstract base class for essentials of a Nexcom server envrionment.
 * Used as a property when creating a network manager.
 *
 * @param sitetoken   Point to a particular database.
 * @param sessionid   User's unique session identifier.
 *
 * @type {NXNetworkEnvironment}
 * @see NXNetworkManager
 */
data class NXNexcomEnvironment(val sitetoken : String, val sessionid : String) : NXJsonEncodable
{

}

data class NXUri(val scheme : String = "http", val host : String, val path : String) : NXJsonEncodable
{
    val urlString : String
    get() = scheme + host + path
}

/**
 * Provides specifics to identify a resource file on a server.
 * Evolve json file is preset and defaulted to.
 *
 * @param scheme    URI HTTP scheme to use. Defaults to http as Nexcom servers suck.
 * @param host      URI part host name.
 * @param path      URI part path to resource file.
 * @param nexcomEnvironment Provides an extension data class to specify nexcom environment specifics.
 */
open class NXNetworkManager(open var uri : NXUri, open var nexcomEnvironment: NXNexcomEnvironment? = null)
{

    companion object {

        /**
         * Default json resource file for shared Evolve projects.
         * Note: specific Evolve environments should be configured in individual projects.
         * @type {NXNetworkManager}
         */
        var defaultManager = NXNetworkManager(NXUri("http://", "evolve.nexcomgroup.com", "/apps/demo/iOS/aspx/json.aspx"))

    }

    /**
     * Gets the compiled url string scheme + host + path.
     * @type {String}
     */
    val urlString : String
        get() = uri.urlString
}
