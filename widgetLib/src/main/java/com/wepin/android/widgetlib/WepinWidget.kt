package com.wepin.android.widgetlib

import android.app.Activity
import android.content.Context
import android.util.Log
import com.wepin.android.commonlib.error.WepinError
import com.wepin.android.commonlib.types.WepinLifeCycle
import com.wepin.android.commonlib.types.WepinLoginStatus
import com.wepin.android.commonlib.types.WepinUser
import com.wepin.android.core.types.wepin.GetAccountListRequest
import com.wepin.android.core.types.wepin.GetNFTListRequest
import com.wepin.android.core.types.wepin.ITermsAccepted
import com.wepin.android.core.types.wepin.RegisterRequest
import com.wepin.android.core.types.wepin.UpdateTermsAcceptedRequest
import com.wepin.android.loginlib.WepinLogin
import com.wepin.android.loginlib.types.WepinLoginOptions
import com.wepin.android.widgetlib.manager.WepinWidgetManager
import com.wepin.android.widgetlib.types.LoginProviderInfo
import com.wepin.android.widgetlib.types.WepinAccount
import com.wepin.android.widgetlib.types.WepinAccountBalanceInfo
import com.wepin.android.widgetlib.types.WepinNFT
import com.wepin.android.widgetlib.types.WepinReceiveResponse
import com.wepin.android.widgetlib.types.WepinSendResponse
import com.wepin.android.widgetlib.types.WepinTxData
import com.wepin.android.widgetlib.types.WepinWidgetAttribute
import com.wepin.android.widgetlib.types.WepinWidgetParams
import com.wepin.android.widgetlib.utils.filterAccountBalance
import com.wepin.android.widgetlib.utils.filterAccountList
import com.wepin.android.widgetlib.utils.filterNft
import com.wepin.android.widgetlib.utils.handleJsonResult
import com.wepin.android.widgetlib.utils.normalizeAmount
import com.wepin.android.widgetlib.webview.Command
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.json.JSONObject
import java.util.concurrent.CompletableFuture
import java.util.concurrent.CompletionException
import java.util.concurrent.Executors

class WepinWidget(wepinWidgetParams: WepinWidgetParams, platformType: String? = "android") {
    private val TAG = this.javaClass.name
    private var _appContext: Context? = wepinWidgetParams.context
    private var _isInitialized: Boolean = false
    private var _platformType: String = platformType ?: "android"
    private lateinit var _wepinWidgetManager: WepinWidgetManager
    private var _wepinWidgetParams: WepinWidgetParams = wepinWidgetParams

    var login: WepinLogin? = null
        private set // 외부에서 변경 불가능하게 설정

    init {
        if (_appContext == null) {
            throw IllegalArgumentException("Context is required")
        }
        if (wepinWidgetParams.appId.isEmpty()) {
            throw IllegalArgumentException("AppId is required")
        }
        if (wepinWidgetParams.appKey.isEmpty()) {
            throw IllegalArgumentException("AppKey is required")
        }

        // Initialize login
        val wepinLoginOptions = WepinLoginOptions(
            context = _appContext!!,
            appId = wepinWidgetParams.appId,
            appKey = wepinWidgetParams.appKey,
        )
        login = WepinLogin(wepinLoginOptions)
    }

    fun initialize(attributes: WepinWidgetAttribute): CompletableFuture<Boolean> {
        Log.i(TAG, "initialize")
        val completableFuture = CompletableFuture<Boolean>()
        if (_isInitialized) {
            completableFuture.completeExceptionally(WepinError.ALREADY_INITIALIZED_ERROR)
            return completableFuture
        }
//        _wepinWidgetManager.wepinAttributes = attributes
        _wepinWidgetManager = WepinWidgetManager.getInstance()
        _wepinWidgetManager.initialize(_appContext!!, _wepinWidgetParams, attributes, _platformType)
            .thenCompose {
                _wepinWidgetManager._wepinNetwork?.let { network ->

                    network.getAppInfo().thenApply { infoResponse ->
                        if (!infoResponse) {
                            completableFuture.complete(false)
                            _isInitialized = false

                            return@thenApply false
                        }

                        login?.init()?.thenApply {
                            _wepinWidgetManager.setLogin(login)
                            _wepinWidgetManager._wepinSessionManager?.checkLoginStatusAndGetLifeCycle()
                                ?.thenApply { }
                        }

                        _isInitialized = true
                        completableFuture.complete(true)
                        true
                    }?.exceptionally { error ->
                        _isInitialized = false
                        completableFuture.completeExceptionally(error)
                        null
                    }
                }
            }?.exceptionally { error ->
                _isInitialized = false
                completableFuture.completeExceptionally(error)
            }

        return completableFuture
    }

    fun isInitialized(): Boolean {
        Log.i(TAG, "isInitialized")
        return _isInitialized
    }

    fun changeLanguage(language: String, currency: String? = null) {
        Log.i(TAG, "changeLanguage")
        if (!_isInitialized) {
            throw WepinError.NOT_INITIALIZED_ERROR
        }
        _wepinWidgetManager.wepinAttributes?.defaultLanguage = language
        if (currency != null)
            _wepinWidgetManager.wepinAttributes?.defaultCurrency = currency
        return
    }

    fun getStatus(): CompletableFuture<WepinLifeCycle>? {
        Log.i(TAG, "getStatus")
        val completableFuture = CompletableFuture<WepinLifeCycle>()
        if (!_isInitialized) {
            completableFuture.complete(WepinLifeCycle.NOT_INITIALIZED)
            return completableFuture
        }
        return _wepinWidgetManager._wepinSessionManager?.checkLoginStatusAndGetLifeCycle()
    }

    fun openWidget(context: Context): CompletableFuture<Boolean> {
        Log.i(TAG, "openWidget")
        val completableFuture = CompletableFuture<Boolean>()
        if (!_isInitialized) {
            completableFuture.completeExceptionally(WepinError.NOT_INITIALIZED_ERROR)
            return completableFuture
        }
        context as? Activity ?: run {
            completableFuture.completeExceptionally(WepinError.NOT_ACTIVITY)
            return completableFuture
        }
        getStatus()?.thenApply {
            if (it != WepinLifeCycle.LOGIN) {
                completableFuture.completeExceptionally(WepinError.INVALID_LOGIN_SESSION)
            } else {
                _wepinWidgetManager._wepinWebViewManager?.openWidget(context)
                completableFuture.complete(true)
            }
        }
        return completableFuture
    }

    fun closeWidget() {
        Log.i(TAG, "closeWidget")
        val completableFuture = CompletableFuture<Boolean>()
        if (!_isInitialized) {
            completableFuture.completeExceptionally(WepinError.NOT_INITIALIZED_ERROR)
            return
        }
        _wepinWidgetManager._wepinWebViewManager?.closeWidget()
        return
    }

    fun finalize(): Boolean {
        if (!_isInitialized) {
            throw WepinError.NOT_INITIALIZED_ERROR
        }
        login?.finalize()
        _wepinWidgetManager.clear()
        WepinWidgetManager.clearInstance()
        _isInitialized = false
        return true
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    fun loginWithUI(
        context: Context,
        loginProviders: List<LoginProviderInfo>,
        email: String? = null
    ): CompletableFuture<WepinUser> {
        Log.i(TAG, "loginWithUI")
        val completableFuture = CompletableFuture<WepinUser>()
        _wepinWidgetManager._wepinWebViewManager?.resetResponseWepinUserDeferred()
        if (!_isInitialized) {
            completableFuture.completeExceptionally(WepinError.NOT_INITIALIZED_ERROR)
            return completableFuture
        }

        context as? Activity ?: run {
            completableFuture.completeExceptionally(WepinError.NOT_ACTIVITY)
            return completableFuture
        }

        getStatus()?.thenApply {
            if (it == WepinLifeCycle.LOGIN) {
                completableFuture.complete(_wepinWidgetManager._wepinSessionManager!!.getWepinUser())
            } else {
                if (email != null) {
                    _wepinWidgetManager.setSpecifiedEmail(email)
                } else {
                    _wepinWidgetManager.setSpecifiedEmail("")
                }
                if (loginProviders.isNotEmpty()) {
                    _wepinWidgetManager.wepinAttributes?.loginProviders =
                        loginProviders.map { it.provider }
                    _wepinWidgetManager.loginProviderInfos = loginProviders
                } else {
                    _wepinWidgetManager.wepinAttributes?.loginProviders = emptyList()
                    _wepinWidgetManager.loginProviderInfos = emptyList()
                }
                _wepinWidgetManager._wepinWebViewManager!!.openWidget(context)

                _wepinWidgetManager._wepinWebViewManager?.getResponseWepinUserDeferred()
                    ?.invokeOnCompletion { throwable ->
                        if (throwable != null) {
                            completableFuture.completeExceptionally(throwable)
                        } else {
                            try {
                                val result =
                                    _wepinWidgetManager._wepinWebViewManager?.getResponseWepinUserDeferred()
                                        ?.getCompleted()
//                                Log.d(TAG, "_responseDeferred result : $result")
                                if (result == true) {
                                    closeWidget()
                                    completableFuture.complete(_wepinWidgetManager._wepinSessionManager!!.getWepinUser())
                                } else
                                    completableFuture.completeExceptionally(WepinError.FAILED_LOGIN)
                            } catch (e: Exception) {
//                                Log.e(TAG, "Exception occurred: ${e.message}")
                                completableFuture.completeExceptionally(e)
                            }
                        }
                    }
            }
        }
        return completableFuture
    }

    fun register(context: Context): CompletableFuture<WepinUser> {
        Log.i(TAG, "register")
        val completableFuture = CompletableFuture<WepinUser>()

        if (!_isInitialized) {
            completableFuture.completeExceptionally(WepinError.NOT_INITIALIZED_ERROR)
            return completableFuture
        }

        context as? Activity ?: run {
            completableFuture.completeExceptionally(WepinError.NOT_ACTIVITY)
            return completableFuture
        }

        getStatus()?.thenApply { status ->
            if (status != WepinLifeCycle.LOGIN_BEFORE_REGISTER) {
                completableFuture.completeExceptionally(WepinError.generalIncorrectLifeCycle("The LifeCycle is not loginBeforeRegister"))
                return@thenApply
            }

            val userInfo = _wepinWidgetManager._wepinSessionManager?.getWepinUser()
            if (userInfo == null) {
                completableFuture.completeExceptionally(WepinError.generalIncorrectLifeCycle("The userInfo is null"))
                return@thenApply
            }

            val userStatus = userInfo.userStatus
            if (
                userStatus?.loginStatus == WepinLoginStatus.REGISTER_REQUIRED && userStatus.pinRequired != true) {
                val userId = userInfo.userInfo?.userId
                val walletId = userInfo.walletId

                if (userId == null || walletId == null) {
                    completableFuture.completeExceptionally(WepinError.FAILED_REGISTER)
                    return@thenApply
                }

                _wepinWidgetManager._wepinNetwork?.register(
                    RegisterRequest(
                        appId = _wepinWidgetManager.appId!!,
                        userId = userId,
                        loginStatus = userStatus.loginStatus.value,
                        walletId = walletId
                    )
                )?.thenCompose {
                    _wepinWidgetManager._wepinNetwork!!.updateTermsAccepted(
                        userId,
                        UpdateTermsAcceptedRequest(
                            termsAccepted = ITermsAccepted(
                                termsOfService = true,
                                privacyPolicy = true
                            )
                        )
                    )
                }?.thenCompose {
                    _wepinWidgetManager._wepinSessionManager?.checkLoginStatusAndGetLifeCycle()
                }?.thenApply {
                    completableFuture.complete(_wepinWidgetManager._wepinSessionManager!!.getWepinUser())
                }?.exceptionally {
                    completableFuture.completeExceptionally(it)
                    null
                }
            } else {
                val parameter = mapOf(
                    "loginStatus" to userStatus?.loginStatus,
                    "pinRequired" to userStatus?.pinRequired
                )

                _wepinWidgetManager._wepinWebViewManager?.openWidgetWithCommand(
                    context,
                    Command.CMD_REGISTER_WEPIN,
                    parameter
                )
                    ?.thenCompose { result ->
                        _wepinWidgetManager._wepinSessionManager?.checkLoginStatusAndGetLifeCycle()
                            ?.thenApply {
                                if (result is String && handleJsonResult(
                                        result,
                                        Command.CMD_REGISTER_WEPIN,
                                        completableFuture
                                    )
                                ) {
                                    val state =
                                        JSONObject(result).getJSONObject("body").getString("state")
                                    if (state == "SUCCESS") {
                                        completableFuture.complete(_wepinWidgetManager._wepinSessionManager!!.getWepinUser())
                                    } else {
                                        completableFuture.completeExceptionally(WepinError.FAILED_REGISTER)
                                    }
                                } else {
                                    completableFuture.completeExceptionally(WepinError.UNKNOWN_ERROR)
                                }
                            }
                    }?.exceptionally {
                        completableFuture.completeExceptionally(it)
                        null
                    }
            }
        }?.exceptionally {
            completableFuture.completeExceptionally(it)
        }


        return completableFuture
    }


    fun getAccounts(
        networks: List<String>? = null,
        withEoa: Boolean? = false
    ): CompletableFuture<List<WepinAccount>> {
        Log.i(TAG, "getAccounts")
        val future = CompletableFuture<List<WepinAccount>>()
        if (!_isInitialized) {
            future.completeExceptionally(WepinError.NOT_INITIALIZED_ERROR)
            return future
        }

        getStatus()?.thenApply {
            if (it != WepinLifeCycle.LOGIN) {
                future.completeExceptionally(WepinError.generalIncorrectLifeCycle("The LifeCycle is not login"))
            } else {
                val userInfo = _wepinWidgetManager._wepinSessionManager?.getWepinUser()
                if (userInfo == null) {
                    future.completeExceptionally(WepinError.generalIncorrectLifeCycle("The userInfo is null"))
                    return@thenApply
                }
                val userId = userInfo.userInfo?.userId
                val walletId = userInfo.walletId
                val localeId = _wepinWidgetManager.wepinAttributes?.defaultLanguage

                if (userId == null || walletId == null) {
                    future.completeExceptionally(WepinError.generalIncorrectLifeCycle("The userId or walletId is null"))
                    return@thenApply
                }


                _wepinWidgetManager._wepinNetwork?.getAppAccountList(
                    GetAccountListRequest(
                        walletId,
                        userId,
                        localeId!!
                    )
                )
                    ?.thenApply { accountList ->
                        // ✅ getAppAccountList() 실행 후 처리
                        val detailAccounts = filterAccountList(
                            accounts = accountList.accounts,
                            aaAccounts = accountList.aa_accounts ?: emptyList(),
                            withEoa = withEoa ?: false
                        ).toMutableList()
                        val accountInfo =
                            WepinAccount.fromAppAccountList(detailAccounts).toMutableList()

                        if (!networks.isNullOrEmpty()) {
                            accountInfo.filter { networks.contains(it.network) }
                        }

                        future.complete(accountInfo)
                    }?.exceptionally { error ->
                        future.completeExceptionally(error)
                    }
            }
        }

        return future
    }

    fun getBalance(accounts: List<WepinAccount>? = null): CompletableFuture<List<WepinAccountBalanceInfo>> {
        Log.i(TAG, "getBalance")

        val future = CompletableFuture<List<WepinAccountBalanceInfo>>()

        if (!_isInitialized) {
            future.completeExceptionally(WepinError.NOT_INITIALIZED_ERROR)
            return future
        }

        getStatus()?.thenCompose { status ->
            val statusFuture = CompletableFuture<List<WepinAccountBalanceInfo>>()

            if (status != WepinLifeCycle.LOGIN) {
                statusFuture.completeExceptionally(WepinError.generalIncorrectLifeCycle("The LifeCycle is not login"))
                return@thenCompose statusFuture
            }

            val userInfo = _wepinWidgetManager._wepinSessionManager?.getWepinUser()
            if (userInfo == null) {
                statusFuture.completeExceptionally(WepinError.generalIncorrectLifeCycle("The userInfo is null"))
                return@thenCompose statusFuture
            }

            val userId = userInfo.userInfo?.userId
            val walletId = userInfo.walletId
            val localeId = _wepinWidgetManager.wepinAttributes?.defaultLanguage

            if (userId == null || walletId == null) {
                statusFuture.completeExceptionally(WepinError.generalIncorrectLifeCycle("The userId or walletId is null"))
                return@thenCompose statusFuture
            }

            // ✅ `getAppAccountList` 호출
            val accountFuture = _wepinWidgetManager._wepinNetwork?.getAppAccountList(
                GetAccountListRequest(walletId, userId, localeId!!)
            ) ?: run {
                val networkErrorFuture = CompletableFuture<List<WepinAccountBalanceInfo>>()
                networkErrorFuture.completeExceptionally(WepinError.generalApiRequestError("Network error"))
                return@thenCompose networkErrorFuture
            }

            accountFuture.thenCompose { accountList ->
                val detailAccounts = filterAccountList(
                    accounts = accountList.accounts,
                    aaAccounts = accountList.aa_accounts ?: emptyList(),
                    withEoa = true
                ).toMutableList()

                if (detailAccounts.isEmpty()) {
                    val accountErrorFuture = CompletableFuture<List<WepinAccountBalanceInfo>>()
                    accountErrorFuture.completeExceptionally(WepinError.ACCOUNT_NOT_FOUND)
                    return@thenCompose accountErrorFuture
                }

                val isAllAccounts = accounts.isNullOrEmpty()
                val filteredAccounts = if (isAllAccounts) {
                    detailAccounts
                } else {
                    detailAccounts.filter { dAccount ->
                        accounts?.any { acc ->
                            acc.network == dAccount.network &&
                                    acc.address == dAccount.address &&
                                    dAccount.contract == null
                        } ?: false
                    }
                }

                if (filteredAccounts.isEmpty()) {
                    val noMatchFuture = CompletableFuture<List<WepinAccountBalanceInfo>>()
                    noMatchFuture.completeExceptionally(WepinError.generalAccountNotFound("No matching accounts found"))
                    return@thenCompose noMatchFuture
                }

                // ✅ 병렬로 `getAccountBalance` 호출
                val executor = Executors.newFixedThreadPool(5)
                val balanceFutures = filteredAccounts.map { dAccount ->
                    _wepinWidgetManager._wepinNetwork?.getAccountBalance(dAccount.accountId)
                        ?.thenApplyAsync({ balance ->
                            balance?.let {
                                filterAccountBalance(detailAccounts, dAccount, it)
                            }
                        }, executor)
                }.filterNotNull()

                // ✅ 모든 `getAccountBalance`가 완료될 때까지 대기 후 변환
                CompletableFuture.allOf(*balanceFutures.toTypedArray())
                    .thenApply {
                        val balanceInfo = balanceFutures.mapNotNull { it.getNow(null) }
                        if (balanceInfo.isEmpty()) {
                            throw WepinError.BALANCES_NOT_FOUND
                        }
                        balanceInfo
                    }
            }.whenComplete { result, throwable ->
                if (throwable != null) {
                    future.completeExceptionally(throwable)
                } else {
                    future.complete(result)
                }
            }

            return@thenCompose statusFuture
        } ?: future.completeExceptionally(WepinError.generalApiRequestError("Network error"))

        return future
    }

    fun getNFTs(
        refresh: Boolean,
        networks: List<String>? = null
    ): CompletableFuture<List<WepinNFT>> {
        Log.i(TAG, "getNFTs")
        val future = CompletableFuture<List<WepinNFT>>()
        if (!_isInitialized) {
            future.completeExceptionally(WepinError.NOT_INITIALIZED_ERROR)
            return future
        }

        getStatus()?.thenCompose { status ->
            if (status != WepinLifeCycle.LOGIN) {
                future.completeExceptionally(WepinError.generalIncorrectLifeCycle("The LifeCycle is not login"))
                return@thenCompose future
            }

            val userInfo = _wepinWidgetManager._wepinSessionManager?.getWepinUser()
            if (userInfo == null) {
                future.completeExceptionally(WepinError.generalIncorrectLifeCycle("The userInfo is null"))
                return@thenCompose future
            }

            val userId = userInfo.userInfo?.userId
            val walletId = userInfo.walletId

            if (userId == null || walletId == null) {
                future.completeExceptionally(WepinError.generalIncorrectLifeCycle("The userId or walletId is null"))
                return@thenCompose future
            }

            val accountListFuture = _wepinWidgetManager._wepinNetwork?.getAppAccountList(
                GetAccountListRequest(
                    walletId,
                    userId,
                    _wepinWidgetManager.wepinAttributes?.defaultLanguage!!
                )
            ) ?: CompletableFuture.completedFuture(null)

            accountListFuture.thenCompose { accountList ->
                if (accountList == null) {
                    future.completeExceptionally(WepinError.ACCOUNT_NOT_FOUND)
                    return@thenCompose future
                }

                val detailAccounts = filterAccountList(
                    accounts = accountList.accounts,
                    aaAccounts = accountList.aa_accounts ?: emptyList(),
                    withEoa = true
                ).toMutableList()

                if (detailAccounts.isEmpty()) {
                    future.completeExceptionally(WepinError.ACCOUNT_NOT_FOUND)
                    return@thenCompose future
                }

                val nftRequest = GetNFTListRequest(walletId, userId)

                val nftListFuture = if (refresh) {
                    _wepinWidgetManager._wepinNetwork?.refreshNFTList(nftRequest)
                } else {
                    _wepinWidgetManager._wepinNetwork?.getNFTList(nftRequest)
                } ?: CompletableFuture.completedFuture(null)

                nftListFuture.thenApply { detailNftList ->
                    if (detailNftList == null || detailNftList.nfts.isEmpty()) {
                        return@thenApply emptyList()
                    }

                    val allNetworks = networks.isNullOrEmpty()
                    val availableAccounts = detailAccounts.filter { account ->
                        allNetworks || networks?.contains(account.network) ?: true
                    }

                    detailNftList.nfts.mapNotNull { nft -> filterNft(nft, availableAccounts) }
                }
            }.whenComplete { result, throwable ->
                if (throwable != null) {
                    future.completeExceptionally(throwable)
                } else {
                    future.complete(result)
                }
            }
        } ?: future.completeExceptionally(WepinError.generalApiRequestError("Network error"))

        return future
    }

    fun send(
        context: Context,
        account: WepinAccount,
        txData: WepinTxData? = null
    ): CompletableFuture<WepinSendResponse> {
        Log.i(TAG, "send")
        val future = CompletableFuture<WepinSendResponse>()
        if (!_isInitialized) {
            future.completeExceptionally(WepinError.NOT_INITIALIZED_ERROR)
            return future
        }

        context as? Activity ?: run {
            future.completeExceptionally(WepinError.NOT_ACTIVITY)
            return future
        }

        getStatus()?.thenCompose { status ->
            if (status != WepinLifeCycle.LOGIN) {
                future.completeExceptionally(WepinError.generalIncorrectLifeCycle("The LifeCycle is not login"))
                return@thenCompose future
            }

            val userInfo = _wepinWidgetManager._wepinSessionManager?.getWepinUser()
            if (userInfo == null) {
                future.completeExceptionally(WepinError.generalIncorrectLifeCycle("The userInfo is null"))
                return@thenCompose future
            }

            val userId = userInfo.userInfo?.userId
            val walletId = userInfo.walletId

            if (userId == null || walletId == null) {
                future.completeExceptionally(WepinError.generalIncorrectLifeCycle("The userId or walletId is null"))
                return@thenCompose future
            }

            val accountListFuture = _wepinWidgetManager._wepinNetwork?.getAppAccountList(
                GetAccountListRequest(
                    walletId,
                    userId,
                    _wepinWidgetManager.wepinAttributes?.defaultLanguage!!
                )
            ) ?: CompletableFuture.completedFuture(null)

            accountListFuture.thenCompose { accountList ->
                if (accountList == null) {
                    future.completeExceptionally(WepinError.ACCOUNT_NOT_FOUND)
                    return@thenCompose future
                }

                val detailAccounts = filterAccountList(
                    accounts = accountList.accounts,
                    aaAccounts = accountList.aa_accounts ?: emptyList(),
                    withEoa = true
                ).toMutableList()

                if (detailAccounts.isEmpty()) {
                    future.completeExceptionally(WepinError.ACCOUNT_NOT_FOUND)
                    return@thenCompose future
                }

                if (txData != null && txData.amount.isNotEmpty() && txData.toAddress.isNotEmpty()) {
                    txData.amount = normalizeAmount(txData.amount)
                }

                val paramAccount = mapOf(
                    "address" to account.address,
                    "network" to account.network,
                    "contract" to account.contract,
                )

                val parameter = mapOf(
                    "account" to paramAccount,
                    "from" to account.address,
                    "to" to txData?.toAddress,
                    "value" to txData?.amount,
                )
                val widgetFuture = _wepinWidgetManager._wepinWebViewManager?.openWidgetWithCommand(
                    context,
                    Command.CMD_SEND_TRANSACTION_WITHOUT_PROVIDER,
                    parameter
                )

                widgetFuture?.thenApply { result ->
                    if (result is String && handleJsonResult(
                            result,
                            Command.CMD_SEND_TRANSACTION_WITHOUT_PROVIDER,
                            future
                        )
                    ) {
                        val jsonResponse = JSONObject(result).getJSONObject("body")
                        val txId = jsonResponse.getString("data")

                        if (txId != null) {
                            future.complete(WepinSendResponse(txId = txId))
                        } else {
                            future.completeExceptionally(WepinError.FAILED_SEND)
                        }
                    } else {
                        future.completeExceptionally(WepinError.FAILED_SEND)
                    }
                }?.exceptionally { throwable ->
                    future.completeExceptionally(throwable)
                    null
                }

                return@thenCompose future

            }?.exceptionally { throwable ->
                future.completeExceptionally(throwable)
                null
            }
        } ?: future.completeExceptionally(WepinError.generalApiRequestError("Network error"))

        return future
    }

    fun receive(context: Context, account: WepinAccount): CompletableFuture<WepinReceiveResponse> {
        Log.i(TAG, "send")
        val future = CompletableFuture<WepinReceiveResponse>()
        if (!_isInitialized) {
            future.completeExceptionally(WepinError.NOT_INITIALIZED_ERROR)
            return future
        }

        context as? Activity ?: run {
            future.completeExceptionally(WepinError.NOT_ACTIVITY)
            return future
        }

        getStatus()?.thenCompose { status ->
            if (status != WepinLifeCycle.LOGIN) {
                future.completeExceptionally(WepinError.generalIncorrectLifeCycle("The LifeCycle is not login"))
                return@thenCompose future
            }

            val userInfo = _wepinWidgetManager._wepinSessionManager?.getWepinUser()
            if (userInfo == null) {
                future.completeExceptionally(WepinError.generalIncorrectLifeCycle("The userInfo is null"))
                return@thenCompose future
            }

            val userId = userInfo.userInfo?.userId
            val walletId = userInfo.walletId

            if (userId == null || walletId == null) {
                future.completeExceptionally(WepinError.generalIncorrectLifeCycle("The userId or walletId is null"))
                return@thenCompose future
            }

            val accountListFuture = _wepinWidgetManager._wepinNetwork?.getAppAccountList(
                GetAccountListRequest(
                    walletId,
                    userId,
                    _wepinWidgetManager.wepinAttributes?.defaultLanguage!!
                )
            ) ?: CompletableFuture.completedFuture(null)

            accountListFuture.thenCompose { accountList ->
                if (accountList == null) {
                    future.completeExceptionally(WepinError.ACCOUNT_NOT_FOUND)
                    return@thenCompose future
                }

                val detailAccounts = filterAccountList(
                    accounts = accountList.accounts,
                    aaAccounts = accountList.aa_accounts ?: emptyList(),
                    withEoa = true
                ).toMutableList()

                if (detailAccounts.isEmpty()) {
                    future.completeExceptionally(WepinError.ACCOUNT_NOT_FOUND)
                    return@thenCompose future
                }

                val paramAccount = mapOf(
                    "address" to account.address,
                    "network" to account.network,
                    "contract" to account.contract,
                )

                val parameter = mapOf(
                    "account" to paramAccount,
                )
                val widgetFuture = _wepinWidgetManager._wepinWebViewManager?.openWidgetWithCommand(
                    context,
                    Command.CMD_RECEIVE_ACCOUNT,
                    parameter
                )

                widgetFuture?.thenApply { result ->
                    if (result is String && handleJsonResult(
                            result,
                            Command.CMD_RECEIVE_ACCOUNT,
                            future
                        )
                    ) {
                        val jsonResponse = JSONObject(result).getJSONObject("body")
                        val state = jsonResponse.getString("state")

                        if (state == "SUCCESS") {
                            future.complete(WepinReceiveResponse(account = account))
                        } else {
                            if (jsonResponse.has("data")) {
                                val data = jsonResponse.getString("data")
                                future.completeExceptionally(
                                    WepinError.generalUnKnownEx(
                                        data
                                    )
                                )

                            } else {
                                future.completeExceptionally(WepinError.FAILED_RECEIVE)
                            }
                        }
                    } else {
                        future.completeExceptionally(WepinError.FAILED_RECEIVE)
                    }
                }?.exceptionally { throwable ->
                    if (throwable is CompletionException && throwable.cause is WepinError && throwable.cause == WepinError.USER_CANCELED) {
                        future.complete(WepinReceiveResponse(account = account))
                    }
                    future.completeExceptionally(throwable)
                    null
                }

                return@thenCompose future

            }?.exceptionally { throwable ->
                future.completeExceptionally(throwable)
                null
            }
        } ?: future.completeExceptionally(WepinError.generalApiRequestError("Network error"))

        return future
    }
}