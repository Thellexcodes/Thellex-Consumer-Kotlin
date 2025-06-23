package com.thellex.payments.data.model

import com.google.gson.annotations.SerializedName
import kotlinx.serialization.Serializable

data class NotificationPayload(
    @SerializedName("notification")
    val notification: NotificationEntity,

    @SerializedName("transaction")
    val transaction: TransactionHistoryEntity
)

@Serializable
data class NotificationEntity(
    @SerializedName("title") val title: String,
    @SerializedName("message") val message: String,
    @SerializedName("consumed") val consumed: Boolean,
    @SerializedName("assetCode") val assetCode: String,
    @SerializedName("expiresAt") val expiresAt: String,
    @SerializedName("amount") val amount: String,
    @SerializedName("txnID") val txnID: String,
    @SerializedName("walletID") val walletID: String
)
