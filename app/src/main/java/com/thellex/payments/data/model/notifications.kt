package com.thellex.payments.data.model

import com.google.gson.annotations.SerializedName
import kotlinx.serialization.Serializable

data class NotificationPayload(
    @SerializedName("notification")
    val notification: NotificationEntity,

    @SerializedName("transaction")
    val transaction: ITransactionHistoryEntity
)

data class INotificationConsumeDto(
    @SerializedName("id") val id: String,
    @SerializedName("consumed") val consumed: Boolean
)

@Serializable
data class NotificationEntity(
    @SerializedName("title") val title: String,
    @SerializedName("message") val message: String,
    @SerializedName("consumed") val consumed: Boolean,
    @SerializedName("assetCode") val assetCode: String,
    @SerializedName("amount") val amount: String,
    @SerializedName("txnID") val txnID: String,
    @SerializedName("kind") val kind: NotificationKindEnum,
    @SerializedName("walletID") val walletID: String,
    @SerializedName("createdAt") val createdAt: String,
)

data class NotificationGroup(
    val date: String,
    val notifications: List<NotificationEntity>
)

enum class NotificationKindEnum(val value: String) {
    Transaction("txn"),
    General("general");

    companion object {
        fun fromValue(value: String): NotificationKindEnum {
            return entries.firstOrNull { it.value.equals(value, ignoreCase = true) } ?: General
        }
    }
}
