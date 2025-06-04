package com.thellex.pos.data.model

import kotlinx.serialization.Serializable

@Serializable
data class ErrorResponse(
    val statusCode: Int,
    val message: String
)
