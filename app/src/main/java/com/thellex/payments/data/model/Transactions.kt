package com.thellex.payments.data.model

import com.google.gson.annotations.SerializedName
import com.thellex.payments.settings.SupportedBlockchain
import com.thellex.payments.settings.Token

data class Transaction(
    @SerializedName("id") val id: String,
    @SerializedName("type") val type: TransactionType,
    @SerializedName("description") val description: String,
    @SerializedName("amount") val amount: String,
    @SerializedName("timestamp") val timestamp: String,
    @SerializedName("iconResId") val iconResId: Int )

 enum class TransactionType {
    @SerializedName("DEPOSIT") DEPOSIT,
    @SerializedName("WITHDRAW") WITHDRAW,
}

data class PosTransaction(
    @SerializedName("iconResId") val iconResId: Int?,
    @SerializedName("statusIconResId") val statusIconResId: Int?,
    @SerializedName("description") val description: String,
    @SerializedName("time") val time: String,
    @SerializedName("amountWithSymbol") val amountWithSymbol: String,
    @SerializedName("status") val status:  PaymentStatus
)

data class BlockchainItem(
    @SerializedName("chain") val chain: SupportedBlockchain,
    @SerializedName("iconRes") val iconRes: Int )

data class Crypto(
    @SerializedName("blockchain") val blockchain: Token,
    @SerializedName("iconRes") val iconRes: Int
) {
    override fun toString(): String = blockchain.name
}

data class TokenListDto(
    @SerializedName("token") val assetCode: Token,
    @SerializedName("iconRes") val iconRes: Int,
    @SerializedName("chain") val chainName: String?
) {
    override fun toString(): String = assetCode.name
}