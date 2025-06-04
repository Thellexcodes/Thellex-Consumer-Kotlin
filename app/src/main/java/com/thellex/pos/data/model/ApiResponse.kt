package com.thellex.pos.data.model

import kotlinx.serialization.Serializable

@Serializable
data class ApiResponse<T>(
    val result: T,
    val status: Boolean,
    val sessionId: String? = null,
    val path: String,
    val statusCode: Int
)

@Serializable
data class ErrorResponse(
    val statusCode: Int,
    val message: String
)
