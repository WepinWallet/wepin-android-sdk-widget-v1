package com.wepin.android.widgetlib.types

import com.fasterxml.jackson.annotation.JsonProperty
import com.wepin.android.commonlib.types.WepinAttribute

data class SetEmailBodyData (
    @JsonProperty("email")
    val email: String,
)