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
    @SerializedName("uid") val uid: Int,
    @SerializedName("account") val account: String?,
    @SerializedName("email") val email: String,
    @SerializedName("emailVerified") val emailVerified: Boolean,
    @SerializedName("suspended") val suspended: Boolean?,
    @SerializedName("alertID") val alertID: String,
    @SerializedName("notifications") val notifications: List<NotificationEntity>?,
    @SerializedName("devices") val devices: List<Any>?,
    @SerializedName("electronic_cards") val electronicCards: List<Any>?,
    @SerializedName("transactionHistory") val transactionHistory: List<TransactionHistoryEntity>?,
    @SerializedName("qprofile") val qprofile: QWalletProfileEntity?,
    @SerializedName("dkyc") val dkyc: DKycEntity?
)
