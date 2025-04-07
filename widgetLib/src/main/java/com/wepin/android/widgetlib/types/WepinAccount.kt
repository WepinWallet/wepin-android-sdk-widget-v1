package com.wepin.android.widgetlib.types

import com.wepin.android.networklib.types.wepin.AppAccount

data class WepinAccount(
    val address: String,
    val network: String,
    val contract: String? = null,
    val isAA: Boolean? = null
) {
    companion object {
        fun fromAppAccount(account: AppAccount): WepinAccount {
            return if (account.contract != null && account.accountTokenId != null) {
                WepinAccount(
                    network = account.network,
                    address = account.address,
                    contract = account.contract
                )
            } else {
                WepinAccount(
                    network = account.network,
                    address = account.address
                )
            }
        }

        fun fromAppAccountList(accountList: List<AppAccount>): List<WepinAccount> {
            return accountList.map { fromAppAccount(it) }
        }
    }
}