package com.thellex.pay.data.model

import com.thellex.pay.settings.TokenEnum
import kotlinx.serialization.Serializable

@Serializable
data class TokenInfo(
    val symbol: TokenEnum,
    val name: String,
    val decimals: Int,
    val iconDisplay: String
)

