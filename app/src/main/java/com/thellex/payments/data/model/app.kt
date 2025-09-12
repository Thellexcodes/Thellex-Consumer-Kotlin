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

data class AppVersionDto(
    val latestVersion: String,
    val minSupportedVersion: String,
    val forceUpdate: Boolean,
    val updateType: String, // e.g., "major", "minor", "patch"
    val downloadUrl: String? = null
)

data class AppVersionCache(
    val lastCheckTimestamp: Long,
    val latestVersion: String,
    val minSupportedVersion: String,
    val forceUpdate: Boolean,
    val updateType: String,
    val downloadUrl: String?
)

sealed class AppVersionState {
    object Idle : AppVersionState()
    object UpToDate : AppVersionState()
    data class ForceUpdate(
        val latestVersion: String,
        val downloadUrl: String?,
        val updateType: String
    ) : AppVersionState()
    data class OptionalUpdate(
        val latestVersion: String,
        val downloadUrl: String?,
        val updateType: String
    ) : AppVersionState()
    data class Error(val message: String) : AppVersionState()
}