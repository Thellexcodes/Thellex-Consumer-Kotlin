package com.thellex.payments.data.model

import com.google.gson.annotations.SerializedName
import kotlinx.serialization.Serializable

@Serializable
enum class IdTypeEnum(val label: String) {
    @SerializedName("NIN") NIN("NIN"),
    @SerializedName("BVN") BVN("BVN"),
    @SerializedName("INTERNATIONAL_PASSPORT") INTERNATIONAL_PASSPORT("INTERNATIONAL_PASSPORT"),
    @SerializedName("DRIVER_LICENSE") DRIVER_LICENSE("DRIVER_LICENSE"),
    @SerializedName("VOTER_CARD") VOTER_CARD("VOTER_CARD"),
    @SerializedName("NATIONAL_ID_CARD") NATIONAL_ID_CARD("NATIONAL_ID_CARD"),
    @SerializedName("RESIDENT_PERMIT") RESIDENT_PERMIT("RESIDENT_PERMIT")
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

@Serializable
data class BasicKycFormModelDto(
    @SerializedName("idType") val idType: String,
    @SerializedName("additionalIdType") val additionalIdType: String,
    @SerializedName("firstName") val firstName: String,
    @SerializedName("middleName") val middleName: String? = null,
    @SerializedName("lastName") val lastName: String,
    @SerializedName("phoneNumber") val phoneNumber: String,
    @SerializedName("dob") val dob: String,
    @SerializedName("bvn") val bvn: String,
    @SerializedName("nin") val nin: String,
    @SerializedName("houseNumber") val houseNumber: String? = null,
    @SerializedName("streetName") val streetName: String? = null,
    @SerializedName("state") val state: String? = null,
    @SerializedName("lga") val lga: String? = null
)

@Serializable
data class KycResponseDto(
    @SerializedName("isVerified") val isVerified: Boolean,
    @SerializedName("currentTier") val currentTier: TierInfo,
    @SerializedName("nextTier") val nextTier: TierInfo,
    @SerializedName("outstandingKyc") val outstandingKyc: List<String> = emptyList()
)

data class VerifySelfieWithPhotoIdDto(
    @SerializedName("selfie_image") val selfieImageBase64: String,
    @SerializedName("photoid_image") val photoIdImageBase64: String,
//    @SerializedName("id_type") val idType: String? = "PASSPORT"
)