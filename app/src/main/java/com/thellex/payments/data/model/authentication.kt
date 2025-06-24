package com.thellex.payments.data.model

import com.google.gson.annotations.SerializedName
import com.thellex.payments.data.enums.TierEnum
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
    @SerializedName("tier") val tier: String,
    @SerializedName("kyc") val kyc: KycInfoEntity,
    @SerializedName("transactionHistory") val transactionHistory: List<TransactionHistoryEntity>,
    @SerializedName("notifications") val notifications: List<NotificationEntity>,
    @SerializedName("settings") val settings: List<StoreSettingsEntity>,
    @SerializedName("bankAccounts") val bankAccounts: List<BankAccountEntity>
)

@Serializable
data class KycInfoEntity(
    @SerializedName("firstName") val firstName: String,
    @SerializedName("middleName") val middleName: String,
    @SerializedName("lastName") val lastName: String,
    @SerializedName("phone") val phone: String,
    @SerializedName("country") val country: String,
    @SerializedName("address") val address: String,
    @SerializedName("businessName") val businessName: String
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
data class StoreSettingsEntity(
    @SerializedName("storeName") val storeName: String,
    @SerializedName("storeLogoUrl") val storeLogoUrl: String,
    @SerializedName("currency") val currency: String,
    @SerializedName("taxRate") val taxRate: Int,
    @SerializedName("isTaxInclusive") val isTaxInclusive: Boolean,
    @SerializedName("payoutFrequency") val payoutFrequency: String,
    @SerializedName("payoutDay") val payoutDay: String,
    @SerializedName("enableCardPayments") val enableCardPayments: Boolean,
    @SerializedName("enableCashPayments") val enableCashPayments: Boolean,
    @SerializedName("enableCryptoPayments") val enableCryptoPayments: Boolean,
    @SerializedName("notifyOnSale") val notifyOnSale: Boolean,
    @SerializedName("notifyOnPayout") val notifyOnPayout: Boolean,
    @SerializedName("themeColor") val themeColor: String,
    @SerializedName("language") val language: String
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