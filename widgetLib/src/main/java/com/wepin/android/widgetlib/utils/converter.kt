package com.wepin.android.widgetlib.utils

import com.wepin.android.commonlib.WepinCommon
import com.wepin.android.commonlib.error.WepinError
import com.wepin.android.core.types.wepin.AppAccount
import com.wepin.android.core.types.wepin.AppNFT
import com.wepin.android.core.types.wepin.GetAccountBalanceResponse
import com.wepin.android.core.types.wepin.NFTContract
import com.wepin.android.widgetlib.types.WepinAccount
import com.wepin.android.widgetlib.types.WepinAccountBalanceInfo
import com.wepin.android.widgetlib.types.WepinNFT
import com.wepin.android.widgetlib.types.WepinNFTContract
import com.wepin.android.widgetlib.types.WepinTokenBalanceInfo
import org.json.JSONObject
import java.util.concurrent.CompletableFuture

internal fun <T> handleJsonResult(
    result: String,
    subCommand: String,
    completableFuture: CompletableFuture<T>
): Boolean {
    val jsonResult = JSONObject(result)
    val command = jsonResult.getJSONObject("body").getString("command")
    val state = jsonResult.getJSONObject("body").getString("state")

    return if (command == subCommand) {
        if (state.equals("SUCCESS", true)) {
            true
        } else {
            val data = jsonResult.getJSONObject("body").getString("data")
            completableFuture.completeExceptionally(WepinError.generalUnKnownEx(data))
            false
        }
    } else {
        val errMsg = "Unexpected command: command=$command, expected=$subCommand"
        completableFuture.completeExceptionally(WepinError.generalUnKnownEx(errMsg))
        false
    }
}

internal fun filterAccountList(
    accounts: List<AppAccount>,
    aaAccounts: List<AppAccount>,
    withEoa: Boolean = false
): List<AppAccount> {
    if (aaAccounts.isEmpty()) return accounts

    return if (withEoa) {
        accounts + aaAccounts
    } else {
        accounts.map { account ->
            aaAccounts.firstOrNull {
                it.coinId == account.coinId &&
                        it.contract == account.contract &&
                        it.eoaAddress == account.address
            } ?: account
        }
    }
}

internal fun filterAccountBalance(
    detailAccounts: List<AppAccount>,
    dAccount: AppAccount,
    balance: GetAccountBalanceResponse
): WepinAccountBalanceInfo {
    val accTokens = detailAccounts.filter { acc ->
        acc.accountId == dAccount.accountId && acc.accountTokenId != null
    }

    val findTokens = if (balance.tokens.isNotEmpty()) {
        balance.tokens.filter { bal ->
            accTokens.any { t -> t.contract == bal.contract }
        }.map { x ->
            WepinTokenBalanceInfo(
                contract = x.contract,
                balance = WepinCommon.getBalanceWithDecimal(x.balance, x.decimals),
                symbol = x.symbol
            )
        }
    } else {
        emptyList()
    }

    return WepinAccountBalanceInfo(
        network = dAccount.network,
        address = dAccount.address,
        balance = WepinCommon.getBalanceWithDecimal(balance.balance, balance.decimals),
        symbol = dAccount.symbol,
        tokens = findTokens
    )
}

internal fun filterNft(nft: AppNFT, dAccounts: List<AppAccount>): WepinNFT? {
    val matchedAccount = dAccounts.firstOrNull { account -> nft.accountId == account.accountId }

    return matchedAccount?.let {
        WepinNFT(
            account = WepinAccount.fromAppAccount(it),
            contract = WepinNFTContract(
                name = nft.contract.name,
                address = nft.contract.address,
                scheme = NFTContract.schemeMapping[nft.contract.scheme]
                    ?: nft.contract.scheme.toString(),
                network = nft.contract.network,
                description = nft.contract.description,
                externalLink = nft.contract.externalLink,
                imageUrl = nft.contract.imageUrl
            ),
            name = nft.name,
            description = nft.description,
            externalLink = nft.externalLink,
            imageUrl = nft.imageUrl,
            contentType = (AppNFT.contentTypeMapping[nft.contentType]
                ?: nft.contentType).toString(),
            state = nft.state,
            contentUrl = nft.contentUrl,
            quantity = nft.quantity
        )
    }
}