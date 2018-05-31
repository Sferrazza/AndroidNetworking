package com.nexcom.nexcomnetworking.NXCore

import com.nexcom.NXCore.NXNetwork

data class NXNetworkOptions(
        var rpc : String?,
        var parameters: List<Pair<String, String>>?,
        var method : String = "get",
        var isDebug : Boolean = false,
        var debugDescription : String? = null,
        var network: NXNetwork? = null,
        val customDateFormat : String? = null)
{

}