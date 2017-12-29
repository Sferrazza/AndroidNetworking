package com.nexcom.NXCore

import com.github.kittinunf.fuel.core.FuelError
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type

/**
 * Created by danielmeachum on 12/22/17.
 */

/**
 * An abstract base class that provides the foundation for requesting and parsing data class responses.
 * Note: this class should <b>always</b> be subclassed.
 * When subclassing, you <b>must</b> specify the model data class the json string will be parsed into.
 * When subclassing, you <b>must</b> override the parseResponse function or an assertion will occur.
 * This class encapsulates a network request and therefore requests an RPC, parameters and other parameters to pass into the request.
 * Call sendRequest after creating the data manager subclass.
 *
 * @param manager      Specific network manager to use. If null, NXNetworkManager.evolveJsonManager is used.
 * @param rpc          The remote procedure to be called on the server. Optional as not all aspx pages require an RPC.
 * @param parameters   List of key/value pairs to be passed into the RPC.
 * @param method       Specify http method get/post. Currently only get is supported but this will be updated soon.
 *
 * @type {NXDataManager}
 */
 open class NXDataManager<T>(manager: NXNetworkManager? = null, rpc : String?, parameters: List<Pair<String, String>>? = null, method : String = "get")
 {
     public var manager = manager
     public var rpc = rpc
     public var parameters = parameters
     public var method = method
     public var isDebug = false

     /**
      * Simple constructor for an rpc that takes no arguments (other than environment defaults).
      * @method constructor
      * @param  {String}    rpc          The remote procedure to be called on the server.
      */
     constructor(rpc: String) : this(null,rpc)

     /**
      * Convenience constructor for an rpc and additional parameters. All other values defaulted.
      * @method constructor
      * @param  {[String]}                      rpc          The remote procedure to be called on the server.
      * @param  {List<Pair<String, String>>}    parameters   List of key/value pairs to be passed into the RPC.
      */
     constructor(rpc: String, parameters: List<Pair<String, String>>) : this(null,rpc,parameters)


     /**
      * Sends network request to server. Allows for inline completion and error handling.
      * Call this function when you are ready to submit your request.
      *
      * @param   {(List<T>)->Unit}    completionHandler   Returns parsed models in response.
      * @param   {(Error)->Unit}      errorHandler        Allows for handling of json parse & networking errors.
      */
     open fun sendRequest(completionHandler: (models: List<T>) -> Unit, errorHandler: (error:Error) -> Unit) {

         val request = NXNetworkRequest(rpc, parameters, method)

         request.isDebug = isDebug

         request.send(manager, completionHandler = { s: String ->

             parseResponse(s, completionHandler, errorHandler)

         }, errorHandler = { error: FuelError ->

             val e = Error(error.localizedMessage)

             errorHandler(e)
         })
     }

     /**
      * Called on subclasses when the parsing of json response string into data models is needed.
      * Due to limitations in Kotlin, subclasses are responsible for parsing models.
      * Call inline fun tableFromJson within this subclassed method to parse data models easily.
      * Must call completionHandler or errorHandler to complete request!
      *
      * @param {String}           responseString     Json string to be parsed
      * @param {(List<T>)->Unit}  completionHandler  Completion handler to be called when parsing is complete.
      * @param {(Error)->Unit}    errorHandler       Allows for adding additional error checking.
      */
     open fun parseResponse(responseString : String, completionHandler: (models: List<T>) -> Unit, errorHandler: (error:Error) -> Unit) {

         assert(false)
     }

     /**
      * Helper function for parsing models from json.
      * Available, but not necessary for functionality.
      * @param {String} json Json string to be parsed
      */
     public inline fun <reified U> Gson.classFromJson(json: String) = this.fromJson<U>(json, U::class.java)

     /**
      * Preferred helper function for parsing models from json string. Abstracts GSON library.
      * @param {String} json Json string to be parsed
      */
     public inline fun <reified U> tableFromJson(json: String) = Gson().fromJson<U>(json, U::class.java)

 }
