package com.thellex.pos.data.model

import com.google.gson.annotations.SerializedName

data class NotificationPayload(
    @SerializedName("notification")
    val notification: NotificationEntity,

    @SerializedName("transaction")
    val transaction: TransactionHistoryEntity
)

data class NotificationEntity(
    @SerializedName("id") val id: String,
    @SerializedName("user") val user: UserEntity,
    @SerializedName("title") val title: String,
    @SerializedName("message") val message: String,
    @SerializedName("consumed") val consumed: Boolean,
    @SerializedName("createdAt") val createdAt: String,
    @SerializedName("updatedAt") val updatedAt: String,
    @SerializedName("currency") val currency: String,
    @SerializedName("expiresAt") val expiresAt: String,
    @SerializedName("amount") val amount: String?,
    @SerializedName("txID") val txID: String,
    @SerializedName("qwalletID") val qwalletID: String? )
