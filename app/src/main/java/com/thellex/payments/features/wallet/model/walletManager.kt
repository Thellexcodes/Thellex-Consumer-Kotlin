package com.thellex.payments.features.wallet.model

import com.google.gson.annotations.SerializedName
import com.thellex.payments.data.model.ITransactionHistoryEntity
import com.thellex.payments.settings.SupportedBlockchainEnum
import com.thellex.payments.settings.Token
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
    @SerializedName("assetCode") val assetCode: Token,
    @SerializedName("transactionHistory") val transactionHistory: List<ITransactionHistoryEntity>
)