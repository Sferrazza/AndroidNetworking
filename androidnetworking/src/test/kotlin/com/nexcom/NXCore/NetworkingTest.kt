package com.nexcom.NXCore

import com.beust.klaxon.Json
import com.beust.klaxon.JsonObject
import com.beust.klaxon.Parser
import com.beust.klaxon.array
import org.junit.Test
import kotlin.test.assertNotNull

/**
 * Created by danielmeachum on 1/10/18.
 */

data class CustomerSite (
        @Json(name = "dbname") val sitetoken : String,
        @Json(name = "customer_name") val customerName : String,
        @Json(name = "is_customer") val isCustomer : Boolean
)

val environment = NXNexcomEnvironment("company","sessionidNxdm1n12013")

val networkManager = NXNetworkManager("http://","corpus.goanteaterapp.com","/apps/austin/iOS/aspx/json.aspx", environment)

class NetworkingDataManager : NXDataManager<CustomerSite>(networkManager,"genConfirmAccount", listOf("account_num" to "40050965", "emailaddress" to "chris.pham@gonexcom.com"))
{
    override fun parseResponse(responseString: String, errorHandler: (error: Error) -> Unit): List<CustomerSite> {

        return parseJsonTable(responseString)
    }
}

class NetworkingTest
{
    @Test
    fun testNetworkModelParsing() {

        val dataManager = NetworkingDataManager()

        dataManager.sendRequest(completionHandler = { models ->

            for (model in models) {

                println("Customer: " + model)

            }
        }, errorHandler = { error ->

            println("Error :( " + error.localizedMessage)
        })
    }
}

class JsonParsingTest
{
    @Test
    fun parseJson() {

        val dataManager = NetworkingDataManager()

        dataManager.sendRequest(completionHandler = { models ->

            for (model in models) {

                println("Success! Name: " + model)
            }
        }, errorHandler =  { error ->

            println("Error :( " + error.localizedMessage)
        })
    }

    inline fun <reified T : Any>parseJsonTable(json : String, tableName : String = "Table"): List<T>? {

        val root = Parser().parse(StringBuilder(json)) as JsonObject

        val table = root.array<JsonObject>("Table")

        if (table != null) {

            return nxJsonParser().parseFromJsonArray(table)
        }

        return null
    }
}