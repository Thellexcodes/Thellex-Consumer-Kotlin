package com.thellex.pos.services
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

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

@Serializable
data class AuthResponse(
    val result: AuthResult,
    val status: Boolean,
    val path: String,
    val statusCode: Int
)

@Serializable
data class AuthResult(
    val token: String,
    val isAuthenticated: Boolean,
    val id: String,
    val createdAt: String,
    val updatedAt: String,
    val uid: Int,
    val account: JsonElement?,
    val email: String,
    val emailVerified: Boolean,
    val suspended: JsonElement?,
    val devices: List<JsonElement>,
    val electronic_cards: List<JsonElement>
)
