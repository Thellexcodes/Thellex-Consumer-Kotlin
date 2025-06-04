package com.thellex.pos.data.model

import com.google.gson.annotations.SerializedName
import com.thellex.pos.settings.PaymentType
import com.thellex.pos.settings.SupportedBlockchain
import com.thellex.pos.settings.Token

data class CreateRequestPaymentDto(
    val paymentType: PaymentType,
    val assetCode: Token,
    val assetIssuer: String? = null,
    val amount: String? = null,
    val network: SupportedBlockchain
)

//Responses
data class RequestPaymentResponse(
    @SerializedName("address") val address: String
)
