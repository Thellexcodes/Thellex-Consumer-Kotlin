package com.thellex.payments.data.model

import com.google.gson.annotations.SerializedName
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class LoginRequestDto(
    @SerialName("email") val email: String
)

@Serializable
data class VerifyUserDto(
    @SerialName("code") val code: Int
)

@Serializable
data class AccessResponse(
    @SerializedName("access_token") val accessToken: String,
    @SerializedName("expires_at") val expiresAt: String
)

@Serializable
data class UserEntity(
    @SerializedName("uid") val uid: Int,
    @SerializedName("email") val email: String,
    @SerializedName("emailVerified") val emailVerified: Boolean,
    @SerializedName("suspended") val suspended: Boolean,
    @SerializedName("alertID") val alertID: String,
    @SerializedName("kyc") val kyc: KycInfoEntity? = null,
    @SerializedName("transactionHistory") val transactionHistory: List<ITransactionHistoryEntity>,
    @SerializedName("notifications") val notifications: List<NotificationEntity>,
    @SerializedName("settings") val settings: List<StoreSettingsEntity>,
    @SerializedName("bankAccounts") val bankAccounts: List<BankAccountEntity>,
    @SerializedName("currentTier") val currentTier: TierInfo? = null,
    @SerializedName("nextTier") val nextTier: TierInfo? = null,
    @SerializedName("outstandingKyc") val outstandingKyc: List<String>? = emptyList()
)