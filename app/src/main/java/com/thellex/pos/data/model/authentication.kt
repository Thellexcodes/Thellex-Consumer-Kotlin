package com.thellex.pos.data.model

import com.google.gson.annotations.SerializedName
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class LoginRequestDto(
    @SerialName("email")
    val email: String
)

@Serializable
data class VerifyUserDto(
    @SerialName("code")
    val code: Int
)

data class UserEntity(
    @SerializedName("token") val token: String,
    @SerializedName("isAuthenticated") val isAuthenticated: Boolean,
    @SerializedName("id") val id: String,
    @SerializedName("createdAt") val createdAt: String,
    @SerializedName("updatedAt") val updatedAt: String,
    @SerializedName("uid") val uid: Int,
    @SerializedName("alertID") val alertID: String,
    @SerializedName("email") val email: String,
    @SerializedName("emailVerified") val emailVerified: Boolean,
    @SerializedName("devices") val devices: List<Any>?,
    @SerializedName("electronic_cards") val electronicCards: List<Any>?,
    @SerializedName("qwallet") val qWallet: QWalletEntity?,
    @SerializedName("notifications") val notifications: List<NotificationEntity>?,
    @SerializedName("transactionHistory") val transactionHistory: List<TransactionHistoryEntity>?,
    @SerializedName("account") val account: Any? = null,
    @SerializedName("suspended") val suspended: Any? = null,
    @SerializedName("dkyc") val dkyc: List<DKycEntity>? = null
)