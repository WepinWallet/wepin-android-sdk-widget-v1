package com.wepin.android.widgetlib.manager

import android.content.Context
import android.util.Log
import com.wepin.android.commonlib.WepinCommon.Companion.getWepinSdkUrl
import com.wepin.android.loginlib.WepinLogin
import com.wepin.android.loginlib.types.WepinLoginOptions
import com.wepin.android.networklib.WepinNetwork
import com.wepin.android.sessionlib.WepinSessionManager
import com.wepin.android.widgetlib.types.LoginProviderInfo
import com.wepin.android.widgetlib.types.WepinWidgetAttribute
import com.wepin.android.widgetlib.types.WepinWidgetAttributeWithProviders
import com.wepin.android.widgetlib.types.WepinWidgetParams
import com.wepin.android.widgetlib.utils.getVersionMetaDataValue
import com.wepin.android.widgetlib.webview.WepinWebViewManager
import java.util.concurrent.CompletableFuture

internal class WepinWidgetManager {
    private val TAG = this.javaClass.name
    private var _appContext: Context? = null
    var appKey: String? = null
    var appId: String? = null
    var packageName: String? = null
    val version: String = getVersionMetaDataValue()
    lateinit var platformType: String
    lateinit var sdkType: String
    var _wepinWebViewManager: WepinWebViewManager? = null

    var _wepinSessionManager: WepinSessionManager? = null
    var _wepinNetwork: WepinNetwork? = null
    var wepinAttributes: WepinWidgetAttributeWithProviders? = WepinWidgetAttributeWithProviders()
    var loginProviderInfos: List<LoginProviderInfo> = emptyList()
    private var _specifiedEmail: String = ""
    var loginLib: WepinLogin? = null

    companion object {
        private var instance: WepinWidgetManager? = null
        fun getInstance(): WepinWidgetManager {
            if (instance == null) {
                instance = WepinWidgetManager()
            }
            return instance!!
        }
    }

    fun getResponseDeferred() = _wepinWebViewManager?.getResponseDeferred()
    fun getResponseWepinUserDeferred() = _wepinWebViewManager?.getResponseWepinUserDeferred()
    fun getCurrentWepinRequest() = _wepinWebViewManager?.getCurrentWepinRequest()
    fun getSpecifiedEmail() = _specifiedEmail
    fun setSpecifiedEmail(email: String) {
        _specifiedEmail = email
    }

    fun initialize(
        context: Context,
        wepinWidgetParams: WepinWidgetParams,
        attributes: WepinWidgetAttribute,
        platform: String? = "android"
    ): CompletableFuture<Boolean> {
        val future = CompletableFuture<Boolean>()
        _appContext = context
        appId = wepinWidgetParams.appId
        appKey = wepinWidgetParams.appKey
        packageName = context.packageName
        platformType = platform ?: "android"
        sdkType =
            "${platformType}-sdk" // widget의 경우 "${platform}-sdk' 로 설정 (provider은 "${platform}-provider", pin은 "${platform}-pin")
        wepinAttributes = WepinWidgetAttributeWithProviders(
            defaultLanguage = attributes.defaultLanguage,
            defaultCurrency = attributes.defaultCurrency
        ) //attributes
        val urlInfo = getWepinSdkUrl(appKey!!)

        // Initialize network and wait for completion
        WepinNetwork.initialize(context, appKey!!, packageName!!, sdkType, version)
            .thenApply { network ->
                _wepinNetwork = network

                // Initialize session manager after network is ready
                WepinSessionManager.initialize()
                _wepinSessionManager = WepinSessionManager.getInstance()

                // Initialize webview manager
                _wepinWebViewManager =
                    WepinWebViewManager(platformType, urlInfo["wepinWebview"] ?: "")

                // Initialize login
                val wepinLoginOptions = WepinLoginOptions(
                    context = _appContext!!,
                    appId = wepinWidgetParams.appId,
                    appKey = wepinWidgetParams.appKey,
                )
                loginLib = WepinLogin(wepinLoginOptions, platformType = platform)
                
                future.complete(true)
            }
            .exceptionally { throwable ->
                future.completeExceptionally(throwable)
                null
            }

        return future
    }

    fun finalize() {
        _wepinSessionManager?.finalize()
        _wepinWebViewManager?.closeWidget()
        _wepinNetwork?.clearAuthToken()
        _wepinNetwork = null
        _wepinSessionManager = null
        _wepinWebViewManager = null
        loginLib?.finalize()
        loginLib = null
    }

    fun closeWebview() {
        _wepinWebViewManager?.closeWidget()
    }
}