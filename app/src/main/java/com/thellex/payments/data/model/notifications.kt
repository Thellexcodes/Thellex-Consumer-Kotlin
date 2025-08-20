package com.thellex.payments.data.model

import com.google.gson.annotations.SerializedName
import kotlinx.serialization.Serializable

data class INotificationConsumeDto(
    @SerializedName("id") val id: String,
    @SerializedName("consumed") val consumed: Boolean,
    @SerializedName("kind") val kind: NotificationKindEnum
)

@Serializable
data class NotificationEntity(
    @SerializedName("id") val id: String,
    @SerializedName("title") val title: String,
    @SerializedName("message") val message: String,
    @SerializedName("consumed") val consumed: Boolean,
    @SerializedName("assetCode") val assetCode: String,
    @SerializedName("amount") val amount: String,
    @SerializedName("txnID") val txnID: String,
    @SerializedName("kind") val kind: NotificationKindEnum,
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
