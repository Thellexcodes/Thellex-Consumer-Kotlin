package com.thellex.pay.data.model

import kotlinx.serialization.Serializable

@Serializable
data class DepositTokenDto(
    val name: String,
    val ticker: String,
    val iconUrl: String
)