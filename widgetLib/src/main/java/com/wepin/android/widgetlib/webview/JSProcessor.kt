package com.wepin.android.widgetlib.webview

import android.util.Log
import com.fasterxml.jackson.databind.ObjectMapper
import com.wepin.android.commonlib.error.WepinError
import com.wepin.android.commonlib.types.JSResponse
import com.wepin.android.commonlib.types.LoginOauthAccessTokenRequest
import com.wepin.android.commonlib.types.LoginOauthIdTokenRequest
import com.wepin.android.core.storage.WepinStorageManager
import com.wepin.android.core.utils.convertJsonToLocalStorageData
import com.wepin.android.loginlib.types.LoginOauth2Params
import com.wepin.android.loginlib.types.OauthTokenType
import com.wepin.android.widgetlib.manager.WepinWidgetManager
import com.wepin.android.widgetlib.types.SetEmailBodyData
import org.json.JSONObject
import java.util.concurrent.CompletableFuture

interface Command {
    companion object {
        /**
         * Commands for JS processor
         */
        const val CMD_READY_TO_WIDGET: String = "ready_to_widget"
        const val CMD_GET_SDK_REQUEST: String = "get_sdk_request"
        const val CMD_CLOSE_WEPIN_WIDGET: String = "close_wepin_widget"
        const val CMD_SET_LOCAL_STORAGE: String = "set_local_storage"
        const val CMD_SET_USER_EMAIL: String = "set_user_email"
        const val CMD_GET_CLIPBOARD: String = "get_clipboard"
        const val CMD_GET_LOGIN_INFO: String = "get_login_info"


        /**
         * Commands for get_sdk_request
         */
        const val CMD_REGISTER_USER_EMAIL: String = "register_user_email"
        const val CMD_REGISTER_WEPIN: String = "register_wepin"
        const val CMD_SEND_TRANSACTION_WITHOUT_PROVIDER: String =
            "send_transaction_without_provider"
        const val CMD_RECEIVE_ACCOUNT: String = "receive_account"

        // only for PINPAD
//        const val CMD_SUB_PIN_REGISTER: String = "pin_register"  // only for creating wallet
//        const val CMD_SUB_PIN_AUTH: String = "pin_auth" //
//        const val CMD_SUB_PIN_CHANGE: String = "pin_change"
//        const val CMD_SUB_PIN_OTP: String = "pin_otp"

    }
}

object JSProcessor {
    private val TAG = this.javaClass.name

    fun processRequest(request: String, callback: (response: String) -> Any) {
//        Log.d(TAG, "processRequest : $request")
        try {
            val objectMapper = ObjectMapper()
            // 메시지를 JSONObject로 변환
            val jsonObject = JSONObject(request)
            val headerObject = jsonObject.getJSONObject("header")
            // "body" 객체를 가져옴
            val bodyObject = jsonObject.getJSONObject("body")

            // "command" 값을 가져옴

            val command = bodyObject.getString("command")
            var jsResponse: JSResponse? = null

            when (command) {
                Command.CMD_READY_TO_WIDGET -> {
                    Log.d(TAG, "CMD_READY_TO_WIDGET")
                    val appKey = WepinWidgetManager.getInstance().appKey
                    val appId = WepinWidgetManager.getInstance().appId
                    val domain = WepinWidgetManager.getInstance().packageName
                    val platform = 2  // android sdk platform number
                    val type = WepinWidgetManager.getInstance().sdkType
                    val version = WepinWidgetManager.getInstance().version
                    val attributes = WepinWidgetManager.getInstance().wepinAttributes
                    var storageData = WepinStorageManager.getAllStorage()
                    jsResponse = JSResponse.Builder(
                        headerObject.getString("id"),
                        headerObject.getString("request_from"),
                        command
                    )
                        .setReadyToWidgetData(
                            appKey = appKey!!,
                            appId = appId!!,
                            domain = domain!!,
                            platform = platform,
                            type = type,
                            version = version,
                            localData = storageData ?: {},
                            attributes = attributes!!
                        ).build()
                }

                Command.CMD_GET_SDK_REQUEST -> {
                    Log.d(TAG, "CMD_GET_SDK_REQUEST")
                    jsResponse = JSResponse.Builder(
                        headerObject.getString("id"),
                        headerObject.getString("request_from"),
                        command
                    )
                        .build()
                    jsResponse.body.data =
                        WepinWidgetManager.getInstance().getCurrentWepinRequest() ?: "No request"

                }

                Command.CMD_SET_LOCAL_STORAGE -> {
                    Log.d(TAG, "CMD_SET_LOCAL_STORAGE")
                    try {
                        val data = bodyObject.getJSONObject("parameter").getJSONObject("data")

                        val storageDataMap = mutableMapOf<String, Any>()

                        data.keys().forEach { key ->
                            val storageValue = when (val value = data.get(key)) {
                                is JSONObject -> {
                                    val jsonString = value.toString()
                                    convertJsonToLocalStorageData(jsonString)
                                }
                                //is String -> StorageDataType.StringValue(value)
                                is String -> value
                                is Boolean -> value
                                else -> value //throw IllegalArgumentException("Unsupported data type for key: $key")
                            }
                            storageDataMap[key] = storageValue
                        }

                        WepinStorageManager.setAllStorage(storageDataMap)

                        if (storageDataMap["user_info"] != null && WepinWidgetManager.getInstance()
                                .getResponseWepinUserDeferred() != null
                        ) {
                            WepinWidgetManager.getInstance().getResponseWepinUserDeferred()
                                ?.complete(true)
                        }
                        jsResponse = JSResponse.Builder(
                            headerObject.getString("id"),
                            headerObject.getString("request_from"),
                            command
                        ).build()
                    } catch (e: Exception) {
                        Log.e(TAG, "Error processing JSON data: ${e.message}")
                        throw WepinError.generalUnKnownEx(e.message)
                    }
                }

                Command.CMD_SET_USER_EMAIL -> {
                    Log.d(TAG, "CMD_SET_USER_EMAIL")
                    jsResponse = JSResponse.Builder(
                        headerObject.getString("id"),
                        headerObject.getString("request_from"),
                        command
                    )
                        .build()
                    jsResponse.body.data = SetEmailBodyData(
                        email = WepinWidgetManager.getInstance().getSpecifiedEmail()
                    )
                }

                Command.CMD_GET_CLIPBOARD -> {
                    Log.d(TAG, "CMD_GET_CLIPBOARD")
                    jsResponse = JSResponse.Builder(
                        headerObject.getString("id"),
                        headerObject.getString("request_from"),
                        command
                    ).build()
                    // TODO: 클립보드 라이브러리 연동 후..
//                    jsResponse.body.data = WepinWidgetManager.getInstance().getClipboard()
                }

                Command.CMD_GET_LOGIN_INFO -> {
                    Log.d(TAG, "CMD_GET_LOGIN_INFO")
                    val provider = bodyObject.getJSONObject("parameter").optString("provider")
                    val providerToClientIdMap = WepinWidgetManager.getInstance()
                        .loginProviderInfos.associate { it.provider to it.clientId }

                    val clientId = providerToClientIdMap[provider]

                    if (provider.isEmpty() || clientId.isNullOrEmpty()) {
                        val jsResponse = JSResponse.Builder(
                            headerObject.getString("id"),
                            headerObject.getString("request_from"),
                            command
                        ).build().apply {
                            body.data = mapOf("error" to "Invalid provider or missing clientId")
                        }
                        val responseStr = objectMapper.writeValueAsString(jsResponse)
                        callback(responseStr)
                        return@processRequest
                    }

                    try {
                        WepinWidgetManager.getInstance().loginLib?.loginWithOauthProvider(
                            LoginOauth2Params(provider, clientId)
                        )?.thenCompose { res ->
                            val nextFuture = CompletableFuture<Any>()

                            when (res.type) {
                                OauthTokenType.ID_TOKEN -> {
                                    WepinWidgetManager.getInstance().loginLib?.loginWithIdToken(
                                        LoginOauthIdTokenRequest(idToken = res.token)
                                    )?.thenAccept { loginResult ->
                                        nextFuture.complete(loginResult ?: "failed login")
                                    }?.exceptionally { error ->
                                        val actualError: WepinError =
                                            if (error.cause is WepinError) {
                                                error.cause as WepinError
                                            } else {
                                                WepinError.generalUnKnownEx(
                                                    error.cause?.message ?: error.message
                                                )
                                            }
                                        if (actualError.code == WepinError.REQUIRED_SIGNUP_EMAIL.code) {
                                            Log.d(TAG, "it's required_signup_email")
//                                            jsResponse.body
                                            nextFuture.complete(
                                                mapOf(
                                                    "result" to "no_email",
                                                    "idToken" to res.token,
                                                )
                                            )
                                        } else {
                                            nextFuture.complete(
                                                mapOf(
                                                    "error" to (error.cause?.message
                                                        ?: error.message)
                                                )
                                            )
                                        }
                                        null
                                    }
                                }

                                OauthTokenType.ACCESS_TOKEN -> {
                                    WepinWidgetManager.getInstance().loginLib?.loginWithAccessToken(
                                        LoginOauthAccessTokenRequest(
                                            provider = res.provider,
                                            accessToken = res.token
                                        )
                                    )?.thenAccept { loginResult ->
                                        nextFuture.complete(loginResult ?: "failed login")
                                    }?.exceptionally { error ->
                                        val actualError: WepinError =
                                            if (error.cause is WepinError) {
                                                error.cause as WepinError
                                            } else {
                                                WepinError.generalUnKnownEx(
                                                    error.cause?.message ?: error.message
                                                )
                                            }
                                        if (actualError.code == WepinError.REQUIRED_SIGNUP_EMAIL.code) {
                                            Log.d(TAG, "it's required_signup_email")
//                                            jsResponse.body
                                            nextFuture.complete(
                                                mapOf(
                                                    "result" to "no_email",
                                                    "accessToken" to res.token,
                                                )
                                            )
                                        } else {
                                            nextFuture.complete(
                                                mapOf(
                                                    "error" to (error.cause?.message
                                                        ?: error.message)
                                                )
                                            )
                                        }
                                        null
                                    }
                                }

                                else -> {
                                    nextFuture.complete(mapOf("error" to "Invalid token type"))
                                }
                            }

                            nextFuture
                        }?.thenAccept { finalRes ->
                            val jsResponse = JSResponse.Builder(
                                headerObject.getString("id"),
                                headerObject.getString("request_from"),
                                command
                            ).build().apply {
                                body.data = finalRes
                            }
                            val responseStr = objectMapper.writeValueAsString(jsResponse)
                            callback(responseStr)
                        }?.exceptionally { err ->
                            val jsResponse = JSResponse.Builder(
                                headerObject.getString("id"),
                                headerObject.getString("request_from"),
                                command
                            ).build().apply {
                                body.data = mapOf("error" to (err.cause?.message ?: err.message))
                            }
                            val responseStr = objectMapper.writeValueAsString(jsResponse)
                            callback(responseStr)
                            null
                        }

                    } catch (e: Exception) {
                        val jsResponse = JSResponse.Builder(
                            headerObject.getString("id"),
                            headerObject.getString("request_from"),
                            command
                        ).build().apply {
                            body.data = mapOf("error" to (e.message ?: "Unknown error"))
                        }
                        val responseStr = objectMapper.writeValueAsString(jsResponse)
                        callback(responseStr)
                    }
                }

                Command.CMD_CLOSE_WEPIN_WIDGET -> {
                    Log.d(TAG, "CMD_CLOSE_WEPIN_WIDGET")
                    jsResponse = null
                    handleAllPendingOperations()
                    WepinWidgetManager.getInstance().closeWebview()

                }
                // CMD_GET_SDK_REQUEST 에 요청했던 command에 대한 웹뷰 응답처리
                Command.CMD_REGISTER_WEPIN,
                Command.CMD_SEND_TRANSACTION_WITHOUT_PROVIDER,
                Command.CMD_RECEIVE_ACCOUNT -> {
                    Log.d(TAG, "CMD_REGISTER_WEPIN")
                    jsResponse = JSResponse.Builder(
                        headerObject.getString("id"),
//                        headerObject.getString("request_from"),
                        "wepin_widget",
                        command
                    ).build()
                    WepinWidgetManager.getInstance().getResponseDeferred()!!.complete(request)
                }
            }
            if (jsResponse == null) {
                Log.d(TAG, "JSProcessor Response is null")
                return
            }

            val response = objectMapper.writeValueAsString(jsResponse)
            Log.d(TAG, "JSProcessor Response : $response")

            // JSInterface의 onResponse 메서드를 통해 JavaScript로 응답 전송
            callback(response)

        } catch (e: Exception) {
            e.printStackTrace()
            throw WepinError.generalUnKnownEx(e.message)
        }
    }


    private fun handleAllPendingOperations() {
        val webViewManager = WepinWidgetManager.getInstance()._wepinWebViewManager

        webViewManager?.let { manager ->
            // 1. 로그인 진행 중인 경우 처리
            manager.getResponseWepinUserDeferred()?.let { deferred ->
                if (!deferred.isCompleted) {
                    Log.d(TAG, "Login was in progress when widget closed - treating as incomplete")
                    deferred.complete(false) // 로그인 미완료로 처리
                }
            }

            // 2. Command 응답 대기 중인 경우 처리
            manager.getResponseDeferred()?.let { deferred ->
                if (!deferred.isCompleted) {
                    Log.d(
                        TAG,
                        "Command was in progress when widget closed - treating as user cancel"
                    )
                    try {
                        deferred.completeExceptionally(WepinError.USER_CANCELED)
                    } catch (e: Exception) {
                        Log.w(TAG, "Failed to complete command deferred", e)
                    }
                }
            }

            // 3. 위젯 닫힘 대기 중인 경우 처리
            manager.getResponseWidgetCloseDeferred()?.let { deferred ->
                if (!deferred.isCompleted) {
                    Log.d(TAG, "Widget close was being awaited - completing")
                    deferred.complete(true)
                }
            }
        }
    }
}
