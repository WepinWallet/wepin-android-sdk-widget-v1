package com.wepin.android.widgetlib.types

data class WepinNFT(
    val account: WepinAccount,
    val contract: WepinNFTContract,
    val name: String,
    val description: String,
    val externalLink: String,
    val imageUrl: String,
    val contentUrl: String?,
    val quantity: Int?,
    val contentType: String,
    val state: Int
)

data class WepinNFTContract(
    val name: String,
    val address: String,
    val scheme: String,
    val description: String?,
    val network: String,
    val externalLink: String?,
    val imageUrl: String?
)