package com.thellex.pos.services
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