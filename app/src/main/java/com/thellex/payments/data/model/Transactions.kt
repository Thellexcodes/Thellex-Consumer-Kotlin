package com.thellex.payments.data.model

import com.google.gson.annotations.SerializedName
import com.thellex.payments.settings.SupportedBlockchainEnum
import com.thellex.payments.settings.TokensEnum
import kotlinx.serialization.Serializable

data class Transaction(
    @SerializedName("id") val id: String,
    @SerializedName("type") val type: TransactionType,
    @SerializedName("description") val description: String,
    @SerializedName("amount") val amount: String,
    @SerializedName("timestamp") val timestamp: String,
    @SerializedName("iconResId") val iconResId: Int
)

@Serializable()
data class ITransactionHistoryDto(
    @SerializedName("id") val id: String,
    @SerializedName("event") val event: String,
    @SerializedName("transactionId") val transactionId: String,
    @SerializedName("transactionDirection") val transactionDirection: String,
    @SerializedName("transactionType") val transactionType: TransactionTypeEnum,
    @SerializedName("assetCode") val assetCode: String,
    @SerializedName("amount") val amount: String,
    @SerializedName("fee") val fee: String,
    @SerializedName("feeLevel") val feeLevel: String,
    @SerializedName("blockchainTxId") val blockchainTxId: String,
    @SerializedName("reason") val reason: String,
    @SerializedName("paymentStatus") val paymentStatus: PaymentStatusEnum,
    @SerializedName("sourceAddress") val sourceAddress: String,
    @SerializedName("destinationAddress") val destinationAddress: String,
    @SerializedName("paymentNetwork") val paymentNetwork: String,
    @SerializedName("createdAt") val createdAt: String,
    @SerializedName("rampID") val rampID: String,
    @SerializedName("mainFiatAmount") val mainFiatAmount: Double,
    @SerializedName("mainAssetAmount") val mainAssetAmount: Double,
)

 enum class TransactionType {
    @SerializedName("DEPOSIT") DEPOSIT,
    @SerializedName("WITHDRAW") WITHDRAW,
}

data class PosTransaction(
    @SerializedName("iconResId") val iconResId: Int?,
    @SerializedName("id") val id: String?,
    @SerializedName("statusIconResId") val statusIconResId: Int?,
    @SerializedName("description") val description: String,
    @SerializedName("time") val time: String,
    @SerializedName("amountWithSymbol") val amountWithSymbol: String,
    @SerializedName("paymentStatus") val paymentStatus:  PaymentStatusEnum,
    @SerializedName("transactionType") val transactionType: TransactionTypeEnum,
    @SerializedName("transactionType") val rampID: String? = null,
    @SerializedName("amount") val amount: String,
)

data class BlockchainItem(
    @SerializedName("chain") val chain: SupportedBlockchainEnum,
    @SerializedName("iconRes") val iconRes: Int )

data class Crypto(
    @SerializedName("blockchain") val blockchain: TokensEnum,
    @SerializedName("iconRes") val iconRes: Int
) {
    override fun toString(): String = blockchain.name
}

data class TokenListDto(
    @SerializedName("token") val assetCode: TokensEnum,
    @SerializedName("iconRes") val iconRes: Int,
    @SerializedName("chain") val chainName: String?
) {
    override fun toString(): String = assetCode.name
}