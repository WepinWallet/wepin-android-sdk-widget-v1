package com.wepin.android.widgetlib.utils

import com.wepin.android.commonlib.error.WepinError

fun normalizeAmount(amount: String): String {
    // 정규식: 소수점 이하 자릿수를 제한하지 않는 숫자 형식
    val regex = Regex("^\\d+(\\.\\d+)?$")

    return if (regex.matches(amount)) {
        amount
    } else {
        throw WepinError.generalInvalidParameter("Invalid amount format: $amount")
    }
}



