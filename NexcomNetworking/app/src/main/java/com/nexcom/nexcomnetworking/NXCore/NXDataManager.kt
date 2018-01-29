package com.nexcom.NXCore

import com.beust.klaxon.*
import com.github.kittinunf.fuel.core.FuelError

internal val LOG_TAG = "NXDataManager"

/**
 * An abstract base class that provides the foundation for requesting and parsing data class responses.
 * Note: this class should <b>always</b> be subclassed.
 * When subclassing, you <b>must</b> specify the model data class the json string will be parsed into.
 * When subclassing, you <b>must</b> override the parseResponse function or an assertion will occur.
 * This class encapsulates a network request and therefore requests an RPC, parameters and other parameters to pass into the request.
 * Call sendRequest after creating the data network subclass.
 *
 * @param network      Specific network network to use. If null, NXNetwork.evolveJsonManager is used.
 * @param rpc          The remote procedure to be called on the server. Optional as not all aspx pages require an RPC.
 * @param parameters   List of key/value pairs to be passed into the RPC.
 * @param method       Specify http method get/post. Currently only get is supported but this will be updated soon.
 *
 * @type {NXDataManager}
 */
 open class NXDataManager<T>(network: NXNetwork? = null, rpc : String?, parameters: List<Pair<String, String>>? = null, method : String = "get")
 {

     public var network = network
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

         request.send(network, completionHandler = { s: String ->

             handleRawResponse(s)

             completionHandler(parseResponse(s, errorHandler))

         }, errorHandler = { error: FuelError ->

             val e = Error(error.localizedMessage)

             errorHandler(e)
         })
     }

     /**
      * Helper function for subclasses to call when they need to work with the raw string response.
      * Called before [parseResponse] method is called.
      */
     open fun handleRawResponse(responseString: String) {
         //Open for subclasses
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
     open fun parseResponse(responseString : String, errorHandler: (error:Error) -> Unit): List<T> {
         //Should be overridden by subclasses
         return emptyList()
     }

     inline fun <reified T>parseJsonTable(json : String, tableName : String = "Table"): List<T> {

         val root = Parser().parse(StringBuilder(json)) as JsonObject

         val table = root.array<JsonObject>(tableName) ?: throw KlaxonException("Table should not be null")

         return nxJsonParser().parseFromJsonArray(table) ?: throw KlaxonException("Models could not be parsed from json")
     }
 }