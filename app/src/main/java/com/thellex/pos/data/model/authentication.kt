package com.thellex.pos.data.model
import com.google.gson.annotations.SerializedName
import kotlinx.serialization.Serializable


@Serializable
data class LoginRequestDto(
    val email: String,
)

@Serializable
data class LoginResponse(
    val result: String,
    val status: Boolean,
    val path: String,
    val statusCode: Int
)

@Serializable
data class VerifyUserDto(
    val code: Int,
)

@Serializable
data class VerifyCodeResponse(
    val result: String,
    val status: Boolean,
    val path: String,
    val statusCode: Int
)

data class UserEntity(
    @SerializedName("token") val token: String,
    @SerializedName("isAuthenticated") val isAuthenticated: Boolean,
    @SerializedName("id") val id: String,
    @SerializedName("createdAt") val createdAt: String,
    @SerializedName("updatedAt") val updatedAt: String,
    @SerializedName("uid") val uid: Int,
    @SerializedName("account") val account: Any?,
    @SerializedName("email") val email: String,
    @SerializedName("emailVerified") val emailVerified: Boolean,
    @SerializedName("suspended") val suspended: Any?,
    @SerializedName("devices") val devices: List<Any>?,
    @SerializedName("electronic_cards") val electronicCards: List<Any>?,
    @SerializedName("qwallet") val qWallet: QWalletEntity?,
    @SerializedName("dkyc") val dkyc: List<Any>?
)
