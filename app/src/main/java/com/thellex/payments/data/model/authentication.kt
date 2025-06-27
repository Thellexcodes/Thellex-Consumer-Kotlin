package com.thellex.payments.data.model

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

@Serializable
data class AccessResponse(
    @SerializedName("access_token") val accessToken: String
);

@Serializable
data class UserEntity(
    @SerializedName("uid") val uid: Int,
    @SerializedName("email") val email: String,
    @SerializedName("emailVerified") val emailVerified: Boolean,
    @SerializedName("suspended") val suspended: Boolean,
    @SerializedName("alertID") val alertID: String,
    @SerializedName("kyc") val kyc: KycInfoEntity? = null,
    @SerializedName("transactionHistory") val transactionHistory: List<TransactionHistoryEntity>,
    @SerializedName("notifications") val notifications: List<NotificationEntity>,
    @SerializedName("settings") val settings: List<StoreSettingsEntity>,
    @SerializedName("bankAccounts") val bankAccounts: List<BankAccountEntity>,
    @SerializedName("currentTier") val currentTier: TierInfo? = null,
    @SerializedName("nextTier") val nextTier: TierInfo? = null
)

@Serializable
data class TransactionHistoryEntity(
    @SerializedName("event") val event: String,
    @SerializedName("transactionId") val transactionId: String,
    @SerializedName("type") val type: String,
    @SerializedName("assetCode") val assetCode: String,
    @SerializedName("amount") val amount: String,
    @SerializedName("fee") val fee: String,
    @SerializedName("feeLevel") val feeLevel: String,
    @SerializedName("blockchainTxId") val blockchainTxId: String,
    @SerializedName("reason") val reason: String,
    @SerializedName("walletId") val walletId: String,
    @SerializedName("walletName") val walletName: String,
    @SerializedName("paymentStatus") val paymentStatus: String,
    @SerializedName("sourceAddress") val sourceAddress: String,
    @SerializedName("destinationAddress") val destinationAddress: String,
    @SerializedName("paymentNetwork") val paymentNetwork: String,
    @SerializedName("createdAt") val createdAt: String,
)

@Serializable
data class BankAccountEntity(
    @SerializedName("bankName") val bankName: String,
    @SerializedName("accountName") val accountName: String,
    @SerializedName("accountNumber") val accountNumber: String,
    @SerializedName("swiftCode") val swiftCode: String,
    @SerializedName("iban") val iban: String,
    @SerializedName("isPrimary") val isPrimary: Boolean
)