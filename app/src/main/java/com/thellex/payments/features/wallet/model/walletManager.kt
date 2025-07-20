package com.thellex.payments.features.wallet.model

import com.google.gson.annotations.SerializedName
import com.thellex.payments.data.model.ITransactionHistoryEntity
import com.thellex.payments.settings.SupportedBlockchainEnum
import com.thellex.payments.settings.TokensEnum
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class WalletBalanceDto(
    @SerializedName("totalInUsd") val totalInUsd: Double,
    @SerializedName("wallets") val wallets: Map<String, WalletDto>
)

@Serializable
data class WalletDto(
    @SerializedName("totalBalance") val totalBalance: String,
    @SerializedName("valueInLocal") val valueInLocal: String,
    @SerializedName("network") val network: SupportedBlockchainEnum,
    @SerializedName("address") val address: String,
    @SerializedName("assetCode") val assetCode: TokensEnum,
    @SerializedName("transactionHistory") val transactionHistory: List<ITransactionHistoryEntity>
)

@Serializable
data class FiatRate(
    @SerialName("fiatCode") val fiatCode: String,
    @SerialName("rate") val rate: Double
)

@Serializable
data class RatesResponseDto(
    @SerialName("rates") val rates: FiatRate,
    @SerialName("fee") val fee: Double,
    @SerialName("expiresAt") val expiresAt: String
)