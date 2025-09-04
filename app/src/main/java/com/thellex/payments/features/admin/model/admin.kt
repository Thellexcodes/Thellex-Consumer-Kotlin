package com.thellex.payments.features.admin.model

import com.google.gson.annotations.SerializedName
import kotlinx.serialization.Serializable

@Serializable
data class Revenue(
    @SerializedName("title") val title: String,
    @SerializedName("total") val total: String
)

@Serializable
data class RevenueResponseDto(
    @SerializedName("totalRevenue") val totalRevenue: Revenue? = null,
    @SerializedName("fiatRevenue") val fiatRevenue: Revenue,
    @SerializedName("cryptoRevenue") val cryptoRevenue: Revenue
)
