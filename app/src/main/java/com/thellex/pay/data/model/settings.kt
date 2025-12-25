package com.thellex.pay.data.model

import com.google.gson.annotations.SerializedName
import kotlinx.serialization.Serializable

@Serializable
data class ITransactionSettingsDto(
    @SerializedName("cryptoDepositAllowed") val cryptoDepositAllowed: Boolean,
    @SerializedName("cryptoDepositRequiresKyc") val cryptoDepositRequiresKyc: Boolean,
    @SerializedName("cryptoWithdrawalAllowed") val cryptoWithdrawalAllowed: Boolean,
    @SerializedName("cryptoWithdrawalRequiresKyc") val cryptoWithdrawalRequiresKyc: Boolean,
    @SerializedName("fiatToCryptoDepositAllowed") val fiatToCryptoDepositAllowed: Boolean,
    @SerializedName("fiatToCryptoDepositRequiresKyc") val fiatToCryptoDepositRequiresKyc: Boolean,
    @SerializedName("cryptoToFiatWithdrawalAllowed") val cryptoToFiatWithdrawalAllowed: Boolean,
    @SerializedName("cryptoToFiatWithdrawalRequiresKyc") val cryptoToFiatWithdrawalRequiresKyc: Boolean,
    @SerializedName("fiatToFiatDepositAllowed") val fiatToFiatDepositAllowed: Boolean,
    @SerializedName("fiatToFiatDepositRequiresKyc") val fiatToFiatDepositRequiresKyc: Boolean,
    @SerializedName("fiatToFiatWithdrawalAllowed") val fiatToFiatWithdrawalAllowed: Boolean,
    @SerializedName("fiatToFiatWithdrawalRequiresKyc") val fiatToFiatWithdrawalRequiresKyc: Boolean
)

@Serializable
data class IStoreSettingsEntityDto(
    @SerializedName("storeName") val storeName: String,
    @SerializedName("storeLogoUrl") val storeLogoUrl: String,
    @SerializedName("currency") val currency: String,
    @SerializedName("taxRate") val taxRate: Int,
    @SerializedName("isTaxInclusive") val isTaxInclusive: Boolean,
    @SerializedName("payoutFrequency") val payoutFrequency: String,
    @SerializedName("payoutDay") val payoutDay: String,
    @SerializedName("enableCardPayments") val enableCardPayments: Boolean,
    @SerializedName("enableCashPayments") val enableCashPayments: Boolean,
    @SerializedName("enableCryptoPayments") val enableCryptoPayments: Boolean,
    @SerializedName("notifyOnSale") val notifyOnSale: Boolean,
    @SerializedName("notifyOnPayout") val notifyOnPayout: Boolean,
    @SerializedName("themeColor") val themeColor: String,
    @SerializedName("language") val language: String
)

@Serializable
data class BaseSettingsCache(
    val timestamp: Long,
    val chains: List<ChainInfoDto>,
    val depositTokens: List<DepositTokenDto>
)

@Serializable
data class BaseSettingsResponse(
    val supportedChains: List<ChainInfoDto>,
    val supportedTokens: List<DepositTokenDto>
)

