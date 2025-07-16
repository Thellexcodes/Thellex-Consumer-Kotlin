package com.thellex.payments.data.model

import com.google.gson.annotations.SerializedName
import kotlinx.serialization.Serializable

@Serializable
data class IFiatCryptoRampTransactionsDto(
    @SerializedName("expiresAt") val expiresAt: String,
    @SerializedName("netFiatAmount") val netFiatAmount: Int,
    @SerializedName("netCryptoAmount") val netCryptoAmount: Int,
    @SerializedName("feeLabel") val feeLabel: String,
    @SerializedName("serviceFeeAmountLocal") val serviceFeeAmountLocal: Int,
    @SerializedName("serviceFeeAmountUSD") val serviceFeeAmountUSD: Int,
    @SerializedName("rate") val rate: Int,
    @SerializedName("grossCrypto") val grossCrypto: Int,
    @SerializedName("grossFiat") val grossFiat: Int,
    @SerializedName("recipientInfo") val recipientInfo: RecipientInfo,
    @SerializedName("bankInfo") val bankInfo: BankInfo,
    @SerializedName("blockchainTxId") val blockchainTxId: String? = null
)

@Serializable
data class RecipientInfo(
    @SerializedName("sourceAddress") val sourceAddress: String,
    @SerializedName("destinationAddress") val destinationAddress: String,
    @SerializedName("network") val network: String,
    @SerializedName("assetCode") val assetCode: String
)

@Serializable
data class BankInfo(
    @SerializedName("bankName") val bankName: String,
    @SerializedName("accountNumber") val accountNumber: String,
    @SerializedName("accountName") val accountName: String,
    @SerializedName("accountHolder") val accountHolder: String,
    @SerializedName("networkId") val networkId: String,
    @SerializedName("accountBank") val accountBank: String,
    @SerializedName("networkName") val networkName: String
)