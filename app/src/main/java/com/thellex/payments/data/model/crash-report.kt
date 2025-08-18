package com.thellex.payments.data.model

import com.google.gson.annotations.SerializedName
import kotlinx.serialization.Serializable

@Serializable
data class CrashReportDto(
    @SerializedName("timestamp") val timestamp: Long,
    @SerializedName("device") val device: String,
    @SerializedName("os") val os: String,
    @SerializedName("log") val log: String
)