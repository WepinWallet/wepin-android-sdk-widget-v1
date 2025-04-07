package com.wepin.android.widgetlib.types

data class WepinTokenBalanceInfo(
    val contract: String,
    val symbol: String,
    val balance: String
)

data class WepinAccountBalanceInfo(
    val network: String,
    val address: String,
    val symbol: String,
    val balance: String,
    val tokens: List<WepinTokenBalanceInfo>
)