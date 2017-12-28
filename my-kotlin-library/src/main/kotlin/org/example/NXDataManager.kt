import com.github.kittinunf.fuel.core.FuelError
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type

/**
 * Created by danielmeachum on 12/22/17.
 */


open class NXDataManager<T>(manager: NXNetworkManager? = null, rpc : String?, parameters: List<Pair<String, String>>? = null, method : String = "get")
{
    public var manager = manager
    public var rpc = rpc
    public var parameters = parameters
    public var method = method

    constructor(rpc: String) : this(null,rpc)

    constructor(rpc: String, parameters: List<Pair<String, String>>) : this(null,rpc,parameters)



    open fun sendRequest(completionHandler: (models: List<T>) -> Unit, errorHandler: (error:Error) -> Unit) {

        val request = NXNetworkRequest(rpc, parameters, method)

        request.send(manager, completionHandler = { s: String ->

            parseResponse(s, completionHandler, errorHandler)

        }, errorHandler = { error: FuelError ->

            val e = Error(error.localizedMessage)

            errorHandler(e)
        })
    }

    open fun parseResponse(responseString : String, completionHandler: (models: List<T>) -> Unit, errorHandler: (error:Error) -> Unit) {

        assert(false)
    }

    public inline fun <reified U> Gson.classFromJson(json: String) = this.fromJson<U>(json, U::class.java)

}

open class ParameterizedTypeReference<T>
{
    inline fun <reified T: Any> typeRef(): ParameterizedTypeReference<T> = object: ParameterizedTypeReference<T>(){}
}
