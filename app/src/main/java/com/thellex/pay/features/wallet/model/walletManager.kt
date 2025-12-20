package com.thellex.pay.features.wallet.model

import com.google.gson.annotations.SerializedName
import com.thellex.pay.data.model.ITransactionHistoryDto
import com.thellex.pay.settings.SupportedBlockchainEnum
import com.thellex.pay.settings.TokensEnum
import kotlinx.serialization.Serializable

@Serializable
data class WalletBalanceDto(
    val totalInUsd: Double,
    val assetTotals: Map<String, AssetTotalDto>,
    val wallets: Map<String, GroupedWalletDto>
)

@Serializable
data class AssetTotalDto(
    val total: Double,
    val valueInLocal: Double,
    val valueInUsd: Double,
    val logo: String
)

@Serializable
data class GroupedWalletDto(
    val network: String,
    val address: String,
    val assets: List<GroupedWalletAssetDto>
)

@Serializable
data class GroupedWalletAssetDto(
    val assetCode: String,
    val balance: Double,
    val valueInLocal: Double,
    val valueInUsd: Double,
    val transactionHistory: List<String>
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