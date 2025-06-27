package com.thellex.payments.data.model

import com.google.gson.annotations.SerializedName
import com.thellex.payments.data.enums.TierEnum
import kotlinx.serialization.Serializable
import java.util.Date

enum class IdTypeEnum(val label: String) {
    NIN("NIN"),
    BVN("BVN"),
    INTERNATIONAL_PASSPORT("INTERNATIONAL_PASSPORT"),
    DRIVER_LICENSE("DRIVER_LICENSE"),
    VOTER_CARD("VOTER_CARD"),
    NATIONAL_ID_CARD("NATIONAL_ID_CARD"),
    RESIDENT_PERMIT("RESIDENT_PERMIT")
}

@Serializable
data class KycInfoEntity(
    @SerializedName("firstName") val firstName: String,
    @SerializedName("middleName") val middleName: String,
    @SerializedName("lastName") val lastName: String,
    @SerializedName("phone") val phone: String,
    @SerializedName("country") val country: String,
    @SerializedName("address") val address: String,
    @SerializedName("businessName") val businessName: String,
    @SerializedName("status") val status: Boolean
)
