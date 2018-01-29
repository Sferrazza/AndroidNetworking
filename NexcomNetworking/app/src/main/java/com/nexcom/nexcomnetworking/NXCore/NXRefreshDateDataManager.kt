package com.nexcom.nexcomnetworking.NXCore

import com.nexcom.NXCore.NXDataManager
import com.nexcom.NXCore.NXNetwork

/**
 * Calls a refresh RPC that returns the latest refresh date.
 * Created by danielmeachum on 1/29/18.
 */
class NXRefreshDateDataManager(network : NXNetwork?, rpc: String, parameters : List<Pair<String,String>>?) : NXDataManager<NXRefreshDate>(network,rpc,parameters)
{
    override fun parseResponse(responseString: String, errorHandler: (error: Error) -> Unit): List<NXRefreshDate> {
        return parseJsonTable(responseString)
    }
}