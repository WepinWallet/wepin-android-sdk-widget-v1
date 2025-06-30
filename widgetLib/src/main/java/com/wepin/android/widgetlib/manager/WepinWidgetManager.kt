package com.wepin.android.widgetlib.manager

import android.content.Context
import com.wepin.android.commonlib.WepinCommon.Companion.getWepinSdkUrl
import com.wepin.android.commonlib.utils.getVersionMetaDataValue
import com.wepin.android.core.WepinCoreManager
import com.wepin.android.core.network.WepinNetwork
import com.wepin.android.core.session.WepinSessionManager
import com.wepin.android.loginlib.WepinLogin
import com.wepin.android.widgetlib.types.LoginProviderInfo
import com.wepin.android.widgetlib.types.WepinWidgetAttribute
import com.wepin.android.widgetlib.types.WepinWidgetAttributeWithProviders
import com.wepin.android.widgetlib.types.WepinWidgetParams
import com.wepin.android.widgetlib.webview.WepinWebViewManager
import java.util.concurrent.CompletableFuture

internal class WepinWidgetManager {
    private val TAG = this.javaClass.name
    private var _appContext: Context? = null
    var appKey: String? = null
    var appId: String? = null
    var packageName: String? = null
    val version: String = getVersionMetaDataValue()
    lateinit var sdkType: String
    var _wepinWebViewManager: WepinWebViewManager? = null

    var _wepinSessionManager: WepinSessionManager? = null
    var _wepinNetwork: WepinNetwork? = null
    var wepinAttributes: WepinWidgetAttributeWithProviders? = WepinWidgetAttributeWithProviders()
    var loginProviderInfos: List<LoginProviderInfo> = emptyList()
    var loginLib: WepinLogin? = null
    private var _specifiedEmail: String = ""

    companion object {
        @Volatile
        private var instance: WepinWidgetManager? = null

        fun getInstance(): WepinWidgetManager {
            if (instance == null) {
                synchronized(this) {
                    if (instance == null) {
                        instance = WepinWidgetManager()
                    }
                }
            }
            return instance!!
        }

        fun clearInstance() {
            instance = null
        }
    }

    fun getResponseDeferred() = _wepinWebViewManager?.getResponseDeferred()
    fun getResponseWepinUserDeferred() = _wepinWebViewManager?.getResponseWepinUserDeferred()
    fun getResponseWidgetCloseDeferred() = _wepinWebViewManager?.getResponseWidgetCloseDeferred()
    fun getCurrentWepinRequest() = _wepinWebViewManager?.getCurrentWepinRequest()
    fun getSpecifiedEmail() = _specifiedEmail
    fun setSpecifiedEmail(email: String) {
        _specifiedEmail = email
    }

    fun initialize(
        context: Context,
        wepinWidgetParams: WepinWidgetParams,
        attributes: WepinWidgetAttribute,
        platform: String = "android"
    ): CompletableFuture<Boolean> {
        val future = CompletableFuture<Boolean>()
        _appContext = context
        appId = wepinWidgetParams.appId
        appKey = wepinWidgetParams.appKey
        packageName = context.packageName
        sdkType =
            "$platform-sdk" // widget의 경우 "${platform}-sdk' 로 설정 (provider은 "${platform}-provider", pin은 "${platform}-pin")
        wepinAttributes = WepinWidgetAttributeWithProviders(
            defaultLanguage = attributes.defaultLanguage,
            defaultCurrency = attributes.defaultCurrency
        ) //attributes

        // Initialize network and wait for completion
        WepinCoreManager.initialize(
            context = _appContext!!,
            appId = appId!!,
            appKey = appKey!!,
            platformType = platform,
            sdkType = sdkType
        )
            .thenApply {
                _wepinNetwork = WepinCoreManager.getNetwork()
                _wepinSessionManager = WepinCoreManager.getSession()

                val urlInfo = getWepinSdkUrl(appKey!!)
                // Initialize webview manager
                _wepinWebViewManager =
                    WepinWebViewManager(sdkType, urlInfo["wepinWebview"] ?: "")

                future.complete(true)
            }
            .exceptionally { throwable ->
                future.completeExceptionally(throwable)
                null
            }

        return future
    }

    fun setLogin(login: WepinLogin?) {
        loginLib = login
    }

    fun clear() {
        android.util.Log.d("WepinWidgetManager", "FINALIZE CALLED", Throwable("stack trace"))
        _wepinWebViewManager?.closeWidget()
        WepinCoreManager.clear()
        _wepinNetwork = null
        _wepinSessionManager = null
        _wepinWebViewManager = null
        loginLib = null
    }

    fun closeWebview() {
        _wepinWebViewManager?.closeWidget()
    }
}