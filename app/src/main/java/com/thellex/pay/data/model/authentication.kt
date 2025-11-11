package com.thellex.pay.data.model

import com.google.gson.annotations.SerializedName
import com.thellex.pay.data.enums.RoleEnum
import com.thellex.pay.features.fiat.adapters.NGBankDto
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

data class DeviceRequestDto(
    @SerializedName("fcmToken") val fcmToken: String,
    @SerializedName("platform") val platform: String,
    @SerializedName("deviceModel") val deviceModel: String,
    @SerializedName("osVersion") val osVersion: String,
    @SerializedName("deviceId") val deviceId: String,
)

@Serializable
data class UserEntity(
    @SerializedName("uid") val uid: Int? = null,
    @SerializedName("email") val email: String? = null,
    @SerializedName("emailVerified") val emailVerified: Boolean? = null,
    @SerializedName("suspended") val suspended: Boolean? = null,
    @SerializedName("kyc") val kyc: KycInfoEntity? = null,
    @SerializedName("transactionHistory") val transactionHistory: List<ITransactionHistoryDto>? = emptyList(),
    @SerializedName("notifications") val notifications: List<NotificationEntity>? = emptyList(),
    @SerializedName("settings") val settings: List<IStoreSettingsEntityDto>? = emptyList(),
    @SerializedName("bankAccounts") val bankAccounts: List<IBankAccountDto>? = emptyList(),
    @SerializedName("fiatCryptoRampTransactions") val fiatCryptoRampTransactions: List<IFiatCryptoRampTransactionsDto>? = emptyList(),
    @SerializedName("currentTier") val currentTier: TierInfo? = null,
    @SerializedName("nextTier") val nextTier: TierInfo? = null,
    @SerializedName("remainingTiers") val remainingTiers: List<TierInfo>? = emptyList(),
    @SerializedName("outstandingKyc") val outstandingKyc: List<String>? = emptyList(),
    @SerializedName("transactionSettings") val transactionSettings: ITransactionSettingsDto? = null,
    @SerializedName("banks") val banks: List<NGBankDto>? = emptyList(),
    @SerializedName("role") val role: RoleEnum? = null,
    val security: Security? = null
)

@Serializable
data class Security(
    val hasPin: Boolean,
    val isBiometricEnabled: Boolean
)

@Serializable
data class ChallengeResponse(
    val challenge: String,
    val userId: String,
    val rp: RelyingParty,
    val user: UserInfo?,
    val pubKeyCredParams: List<CredentialParams>,
    val timeout: Long?,
    val attestation: String?,
    val excludeCredentials: List<ExcludeCredential>?,
    val authenticatorSelection: AuthenticatorSelection?,
    val extensions: Extensions?,
    val mode: String?
)

@Serializable
data class UserInfo(
    val id: String,
    val name: String?,
    val displayName: String?
)

@Serializable
data class ExcludeCredential(
    val id: String,
    val type: String
)

@Serializable
data class AuthenticatorSelection(
    val residentKey: String?,
    val userVerification: String?,
    val requireResidentKey: Boolean?,
    val authenticatorAttachment: String?
)


@Serializable
data class Extensions(
    val credProps: Boolean?
)

@Serializable
data class RelyingParty(
    val name: String,
    val id: String
)

@Serializable
data class CredentialParams(
    val type: String,
    val alg: Int
)

data class VerifyRegistrationRequest(
    val challenge: String,
    val attestationResponse: Any
)

data class VerifyAuthenticationRequest(
    val challenge: String,
    val attestationResponse: Any
)