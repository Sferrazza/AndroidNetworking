package com.nexcom.nexcomnetworking.NXCore

import android.content.Context
import android.content.ContextWrapper
import android.net.Uri
import android.util.Log
import com.nexcom.NXCore.*

/**
 * Created by danielmeachum on 1/26/18.
 */

val defaultUri = NXUri("http://","servbyte.nexcomgroup.com","/apps/demo/iOS/aspx/json.aspx")

const val defaultSitetoken = "demo"

val defaultEnvironment = NXNexcomEnvironment(defaultSitetoken, "sessionidNxdm1n12013")



/**
 * Updates the network manager's nexcom environment to a new sessionid.
 *
 * Both [LocalizedNetworkManager] and [LocalizedOnboardingNetworkManager] session ids are updated.
 * These network managers store the sessionid independently under the assumption that one day onboarding could have an independent session id.
 *
 * @param[context] Localized context
 * @param[sessionID] The sessionid to be updated
 *
 * Created by danielmeachum on 1/12/18.
 */
fun updateNetworkManager(context: Context,sessionID: String) {

    val networkManager = LocalizedNetworkManager(context)

    val environment = networkManager.manager.nexcomEnvironment ?: return

    networkManager.manager = NXNetworkManager(networkManager.manager.uri, NXNexcomEnvironment(environment.sitetoken, sessionID))


    val onboardingNetworkManager = LocalizedOnboardingNetworkManager(context)

    val onboardingEnvironment = onboardingNetworkManager.manager.nexcomEnvironment ?: return

    onboardingNetworkManager.manager = NXNetworkManager(onboardingNetworkManager.manager.uri, NXNexcomEnvironment(onboardingEnvironment.sitetoken, sessionID))
}

/**
 * Updates the localized network manager to user particulars.
 *
 * The [User] instance has a company-specific url and sitetoken.
 * Some procedures are required to use localized network parameters.
 * Only [LocalizedNetworkManager] is updated.
 *
 * @param[context] Localized context
 * @param[user] The user instance containing specific network parameters
 */
fun localizeNetworkManager(context : Context, sitetoken : String, url : String) {

    val networkManager = LocalizedNetworkManager(context)

    val sessionID = networkManager.manager.nexcomEnvironment?.sessionid ?: return


    val uri = Uri.parse(url)

    val nxUri = NXUri(uri.scheme + "://",uri.host,uri.path + "/iOS/aspx/json.aspx")

    val environment = NXNexcomEnvironment(sitetoken, sessionID)


    networkManager.manager = NXNetworkManager(nxUri, environment)
}

/**
 * Provides a localized network manager.
 *
 * Localizes and persists all of [NXNetworkManager]'s [NXUri] uri and [NXNexcomEnvironment] environment.
 * Defaults [NXUri] uri to localizedUri.
 * Defaults [NXNexcomEnvironment] nexcomEnvironment to onboardingEnvironment.
 * Supplying a context allows network manager particulars to be customized by using Gradle build variants.
 * This class stores its values in shared preferences as a means of data persistence.
 * Use the manager property to access the localized network manager.
 *
 * @property[manager] This property returns the localized network manager. Setting it will persist values.
 *
 * @param[base] Localized context
 */
open class LocalizedNetworkManager(base : Context) : ContextWrapper(base)
{
    private val preferencesName = "NetworkPreferences"

    private val uriKey = "uri"
    private val environmentKey = "environment"

    private val prefs = getSharedPreferences(preferencesName,0)

    open var manager : NXNetworkManager
        get() {


            val uri : NXUri = fromJson(prefs.getString(uriKey, defaultUri.toJson()))

            var env : NXNexcomEnvironment = fromJson(prefs.getString(environmentKey, defaultEnvironment.toJson()))

            Log.e("Help","Getting environment to " + env?.sitetoken)

            return NXNetworkManager(uri, env)
        }
        set(value) {

            val editor = prefs.edit()

            editor.putString(uriKey, value.uri.toJson())

            val env = value.nexcomEnvironment

            if (env != null) {
                editor.putString(environmentKey, env.toJson())
            }
            else {
                editor.remove(environmentKey)
            }

            Log.e("Help","Setting environment to " + env?.sitetoken)

            editor.commit()
        }
}


/**
 * Provides a localized onboarding network manager.
 *
 * Localizes and persists only [NXNetworkManager]'s [NXUri] uri <b>sessionid</b> property.
 * [NXUri] uri is always defaulted to onboardingUri.
 * [NXNexcomEnvironment] nexcomEnvironment is writable and persists when written to.
 * Defaults [NXNexcomEnvironment] nexcomEnvironment to onboardingEnvironment.
 * Supplying a context allows network manager particulars to be customized by using Gradle build variants.
 * This class stores its values in shared preferences as a means of data persistence.
 * Use the manager property to access the localized network manager.
 *
 * @property[manager] This property returns the localized network manager. Setting it will persist values.
 *
 * @param[base] Localized context
 */
class LocalizedOnboardingNetworkManager(base: Context) : LocalizedNetworkManager(base)
{
    private val preferencesName = "UserNetworkPreferences"

    private val environmentKey = "environment"

    private val prefs = getSharedPreferences(preferencesName,0)

    override var manager : NXNetworkManager
        get() {


            val uri : NXUri = defaultUri

            val env : NXNexcomEnvironment = fromJson(prefs.getString(environmentKey, defaultEnvironment.toJson()))

            return NXNetworkManager(uri, env)
        }
        set(value) {

            val editor = prefs.edit()

            val env = value.nexcomEnvironment

            if (env != null) {
                editor.putString(environmentKey, env.toJson())
            }
            else {
                editor.remove(environmentKey)
            }

            editor.commit()
        }
}




/**
 * Abstract base class for localized data managers.
 *
 * Takes all of [NXDataManager]'s parameters + a localized [Context] context.
 *
 * Use this class when you are not onboarding or referencing the onboarding data manager.
 *
 * Use <i>T</i> to specify the Data Class you will be parsing into.
 *
 * @param[context] Localized context
 * @param[rpc] The remote procedure to call <i>(Optional)</i>
 * @param[parameters] List of parameters to be passed in <i>(Optional)</i>
 * @param[method] Http method to be used. <i>(Optional. Defaults to get)</i>
 *
 */
abstract class LocalizedDataManager<T>(context: Context, rpc : String? = null, parameters : List<Pair<String,String>>? = null, method : String = "get") : NXDataManager<T>(LocalizedNetworkManager(context).manager,rpc, parameters,method)

/**
 * Abstract base class for localized onboarding data managers.
 *
 * Takes all of [NXDataManager]'s parameters + a localized [Context] context.
 *
 * Use this class when you referencing the onboarding data manager and would like to use the localized sessionid.
 *
 * Use <i>T</i> to specify the Data Class you will be parsing into.
 *
 * @param[context] Localized context
 * @param[rpc] The remote procedure to call <i>(Optional)</i>
 * @param[parameters] List of parameters to be passed in <i>(Optional)</i>
 * @param[method] Http method to be used. <i>(Optional. Defaults to get)</i>
 *
 */
abstract class LocalizedOnboardingDataManager<T>(context: Context, rpc : String? = null, parameters : List<Pair<String,String>>? = null, method : String = "get") : NXDataManager<T>(LocalizedOnboardingNetworkManager(context).manager,rpc, parameters,method)