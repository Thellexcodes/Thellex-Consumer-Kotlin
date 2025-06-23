package com.thellex.payments.data.model

import com.google.gson.annotations.SerializedName
import com.thellex.payments.data.enums.TierEnum
import java.util.Date

data class DKycEntity(
    @SerializedName("id") val id: String,
    @SerializedName("tier") val tier: TierEnum = TierEnum.NONE,
    @SerializedName("ninVerified") val ninVerified: Boolean = false,
    @SerializedName("bvnVerified") val bvnVerified: Boolean = false,
    @SerializedName("emailVerified") val emailVerified: Boolean = false,
    @SerializedName("phoneVerified") val phoneVerified: Boolean = false,
    @SerializedName("verifiedAt") val verifiedAt: Date? = null
)