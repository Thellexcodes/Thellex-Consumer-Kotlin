package com.thellex.payments.data.model
import com.google.gson.annotations.SerializedName
import com.thellex.payments.data.enums.TierEnum
import kotlinx.serialization.Serializable

@Serializable
data class TierInfo(
    @SerializedName("name") val name: TierEnum,
    @SerializedName("target") val target: String,
    @SerializedName("description") val description: String,
    @SerializedName("transactionLimits") val transactionLimits: TransactionLimits,
    @SerializedName("txnFees") val txnFees: List<TxnFee> = emptyList(),
    @SerializedName("requirements") val requirements: List<String> = emptyList(),
)

@Serializable
data class TransactionLimits(
    @SerializedName("dailyCreditLimit") val dailyCreditLimit: Int,
    @SerializedName("dailyDebitLimit") val dailyDebitLimit: Int,
    @SerializedName("singleDebitLimit") val singleDebitLimit: Int,
)

@Serializable
data class TxnFee(
    @SerializedName("type") val type: String,
    @SerializedName("min") val min: Int,
    @SerializedName("max") val max: Int? = null,
    @SerializedName("feePercentage") val feePercentage: Double,
)