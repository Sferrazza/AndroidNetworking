package com.nexcom.NXCore

import java.text.SimpleDateFormat
import java.util.*

/**
 * Created by danielmeachum on 1/4/18.
 */

val jsonDateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss",Locale.US)

fun Date.toJsonString(): String {

    return jsonDateFormat.format(this)
}