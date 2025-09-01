package com.thellex.payments.data.model

import com.google.gson.annotations.SerializedName
import kotlinx.serialization.Serializable

@Serializable
data class IFiatCryptoRampTransactionsDto(
    @SerializedName("id") val id: String,
    @SerializedName("expiresAt") val expiresAt: String,
    @SerializedName("netFiatAmount") val netFiatAmount: Double,
    @SerializedName("netCryptoAmount") val netCryptoAmount: Double,
    @SerializedName("mainAssetAmount") val mainAssetAmount: Double,
    @SerializedName("mainFiatAmount") val mainFiatAmount: Double,
    @SerializedName("feeLabel") val feeLabel: String,
    @SerializedName("serviceFeeAmountLocal") val serviceFeeAmountLocal: Double,
    @SerializedName("serviceFeeAmountUSD") val serviceFeeAmountUSD: Double,
    @SerializedName("rate") val rate: Double,
    @SerializedName("grossCrypto") val grossCrypto: Double,
    @SerializedName("grossFiat") val grossFiat: Double,
    @SerializedName("recipientInfo") val recipientInfo: RecipientInfo,
    @SerializedName("bankInfo") val bankInfo: IBankInfoRequestDto,
    @SerializedName("blockchainTxId") val blockchainTxId: String? = null,
    @SerializedName("seen") val seen: Boolean,
    @SerializedName("paymentStatus") val paymentStatus: PaymentStatusEnum,
    @SerializedName("transactionType") val transactionType: TransactionTypeEnum,
    @SerializedName("createdAt") val createdAt: String,
    @SerializedName("paymentReason") val paymentReason: String,
    @SerializedName("transactionMessage") val transactionMessage: String? = null,
    @SerializedName("transaction") val transaction: ITransactionHistoryDto? = null,
)

@Serializable
data class RecipientInfo(
    @SerializedName("sourceAddress") val sourceAddress: String,
    @SerializedName("destinationAddress") val destinationAddress: String,
    @SerializedName("network") val network: String,
    @SerializedName("assetCode") val assetCode: String
)

@Serializable
data class IBankInfoRequestDto(
    @SerializedName("accountHolder") val accountHolder: String,
    @SerializedName("accountNumber") val accountNumber: String,
    @SerializedName("bankName") val bankName: String,
    @SerializedName("networkId") val networkId: String? = null,
    @SerializedName("accountBank") val accountBank: String? = null,
    @SerializedName("networkName") val networkName: String? = null
)

@Serializable
data class AdminRampTransactionDTO(
    @SerializedName("rampId") val rampId: String,
    @SerializedName("txnID") val txnID: String,
    @SerializedName("mainCryptoAmount") val mainCryptoAmount: Double,
    @SerializedName("mainFiatAmount") val mainFiatAmount: Double,
    @SerializedName("transactionType") val transactionType: TransactionTypeEnum,
    @SerializedName("userUID") val userUID: Int,
    @SerializedName("approved") val approved: Boolean,
    @SerializedName("paymentStatus") val paymentStatus: PaymentStatusEnum,
    @SerializedName("sequenceId") val sequenceId: String,
    @SerializedName("createdAt") val createdAt: String
)

typealias AdminRampTransactionsResponse = PaginatedResponse<List<AdminRampTransactionDTO>>
