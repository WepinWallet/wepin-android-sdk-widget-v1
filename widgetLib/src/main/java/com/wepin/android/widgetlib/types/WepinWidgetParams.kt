package com.wepin.android.widgetlib.types

import android.content.Context
import com.wepin.android.commonlib.types.WepinAttribute

class WepinWidgetAttribute(defaultLanguage: String? = "en", defaultCurrency: String? = "USD") :
    WepinAttribute(defaultLanguage, defaultCurrency)

class WepinWidgetAttributeWithProviders(
    defaultLanguage: String? = "en",
    defaultCurrency: String? = "USD",
    var loginProviders: List<String> = emptyList()
) : WepinAttribute(defaultLanguage, defaultCurrency)


data class WepinWidgetParams(val context: Context, val appId: String, val appKey: String)