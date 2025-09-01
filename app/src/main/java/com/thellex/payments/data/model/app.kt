package com.thellex.payments.data.model

import com.google.gson.annotations.SerializedName
import kotlinx.serialization.Serializable

@Serializable
data class PaginatedResponse<T>(
    @SerializedName("data") val data: T,
    @SerializedName("lastPage") val lastPage: Int,
    @SerializedName("pageNumber") val pageNumber: Int,
    @SerializedName("total") val total: Int
)

@Serializable
data class AdminData(
    @SerializedName("rampTransactions") val rampTransactions: AdminRampTransactionsResponse? = null,
)

data class ApproveRampRequest(
    @SerializedName("approved") val approved: Boolean,
    @SerializedName("txId") val txId: String,
    @SerializedName("sequenceId") val sequenceId: String
)
