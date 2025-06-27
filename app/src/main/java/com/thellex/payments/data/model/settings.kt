package com.thellex.payments.data.model

import com.google.gson.annotations.SerializedName
import kotlinx.serialization.Serializable


@Serializable
data class StoreSettingsEntity(
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