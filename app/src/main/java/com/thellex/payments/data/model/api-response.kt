package com.thellex.payments.data.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName

@Serializable
data class ApiResponse<T>(
    @SerialName("result") val result: T? = null,
    @SerialName("status") val status: Boolean,
    @SerialName("sessionId") val sessionId: String? = null,
    @SerialName("path") val path: String,
    @SerialName("statusCode") val statusCode: Int
)

@Serializable
data class ErrorResponse(
    @SerialName("statusCode") val statusCode: Int,
    @SerialName("message") val message: String
)
