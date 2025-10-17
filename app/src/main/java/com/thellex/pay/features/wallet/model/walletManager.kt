package com.thellex.pay.features.wallet.model

import com.google.gson.annotations.SerializedName
import com.thellex.pay.data.model.ITransactionHistoryDto
import com.thellex.pay.settings.SupportedBlockchainEnum
import com.thellex.pay.settings.TokensEnum
import kotlinx.serialization.Serializable

@Serializable
data class WalletBalanceDto(
    @SerializedName("totalInUsd") val totalInUsd: Double,
    @SerializedName("wallets") val wallets: Map<String, WalletDto>
)

@Serializable
data class WalletDto(
    @SerializedName("totalBalance") val totalBalance: Double,
    @SerializedName("valueInLocal") val valueInLocal: Double,
    @SerializedName("network") val network: SupportedBlockchainEnum,
    @SerializedName("address") val address: String,
    @SerializedName("assetCode") val assetCode: TokensEnum,
    @SerializedName("transactionHistory") val transactionHistory: List<ITransactionHistoryDto>
)

data class IRateDto(
    @SerializedName("buy")
    val buy: Double,

    @SerializedName("sell")
    val sell: Double,

    @SerializedName("fee")
    val fee: Double,

    @SerializedName("feeDivisor")
    val feeDivisor: Double
)

data class IRatesDto(
    @SerializedName("fiatCode")
    val fiatCode: String,

    @SerializedName("rate")
    val rate: IRateDto
)

data class IRatesResponseDto(
    @SerializedName("rates")
    var rates: List<IRatesDto>,

    @SerializedName("expiresAt")
    var expiresAt: String
)