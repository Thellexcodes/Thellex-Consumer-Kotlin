package com.thellex.payments.features.wallet.model

import com.google.gson.annotations.SerializedName

data class WalletManagerBalanceResponse(
    @SerializedName("totalBalance") val totalBalance: String,
    @SerializedName("currency") val currency: String,
    @SerializedName("wallets") val wallets: List<WalletEntry>
)

data class WalletEntry(
    @SerializedName("address") val address: String,
    @SerializedName("network") val network: String,
    @SerializedName("balanceInUsd") val balanceInUsd: Double,
    @SerializedName("balanceInNgn") val balanceInNgn: Double,
    @SerializedName("assetCode") val assetCode: String
)
