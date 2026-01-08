package com.thellex.pay.data.model

import com.google.gson.annotations.SerializedName
import com.thellex.pay.settings.SupportedBlockchainEnum
import com.thellex.pay.settings.TokenEnum
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
    @SerializedName("event") val event: String?,
    @SerializedName("transactionId") val transactionId: String,
    @SerializedName("transactionDirection") val transactionDirection: String,
    @SerializedName("transactionType") val transactionType: TransactionTypeEnum,
    @SerializedName("assetCode") val assetCode: TokenEnum,
    @SerializedName("amount") val amount: String,
    @SerializedName("fee") val fee: String,
    @SerializedName("feeLevel") val feeLevel: String,
    @SerializedName("blockchainTxId") val blockchainTxId: String?,
    @SerializedName("reason") val reason: String,
    @SerializedName("paymentStatus") val paymentStatus: PaymentStatusEnum,
    @SerializedName("sourceAddress") val sourceAddress: String,
    @SerializedName("destinationAddress") val destinationAddress: String,
    @SerializedName("paymentNetwork") val paymentNetwork: SupportedBlockchainEnum,
    @SerializedName("createdAt") val createdAt: String,
    @SerializedName("rampID") val rampID: String? = null,
    @SerializedName("mainFiatAmount") val mainFiatAmount: Double,
    @SerializedName("mainAssetAmount") val mainAssetAmount: Double,
    @SerializedName("transactionMessage") val transactionMessage: String? = null,
    val valueInLocal: Double,
    val valueInUsd: Double
)

 enum class TransactionType {
    @SerializedName("DEPOSIT") DEPOSIT,
    @SerializedName("WITHDRAW") WITHDRAW,
}

@Serializable
data class PosTransaction(
    @SerializedName("id") val id: String?,
    @SerializedName("assetIconUrl") val assetIconUrl: String,
    @SerializedName("description") val description: String,
    @SerializedName("time") val time: String,
    @SerializedName("amountWithSymbol") val amountWithSymbol: String,
    @SerializedName("paymentStatus") val paymentStatus:  PaymentStatusEnum,
    @SerializedName("transactionType") val transactionType: TransactionTypeEnum,
    @SerializedName("transactionType") val rampID: String? = null,
    @SerializedName("amount") val amount: String,
    val assetCode: TokenEnum,
    val paymentNetwork: SupportedBlockchainEnum,
    val fundUid: String,
    val sourceAddress: String,
    val reason: String,
    val valueInLocal: Double,
    val valueInUsd: Double
)

data class BlockchainItem(
    @SerializedName("chain") val chain: SupportedBlockchainEnum,
    @SerializedName("iconRes") val iconRes: Int )

data class Crypto(
    @SerializedName("blockchain") val blockchain: TokenEnum,
    @SerializedName("iconRes") val iconRes: Int
) {
    override fun toString(): String = blockchain.name
}

data class TokenListDto(
    @SerializedName("token") val assetCode: TokenEnum,
    @SerializedName("iconRes") val iconRes: Int,
    @SerializedName("chain") val chainName: String?
) {
    override fun toString(): String = assetCode.name
}


@Serializable()
data class ICryptoWithdrawalResponseDto(
    val hash: String,
    @Serializable val status: PaymentStatusEnum,
    val amount: String,
    val assetCode: TokenEnum,
)