package com.thellex.pay.data.model

import kotlinx.serialization.Serializable

@Serializable
data class UserSecurityEntity(
    val hasPin: Boolean = true,
    val isBiometricEnabled: Boolean = false
)