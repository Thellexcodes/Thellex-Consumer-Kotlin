package com.thellex.payments.network.services

import com.google.gson.annotations.SerializedName
import com.thellex.payments.core.utils.Constants
import com.thellex.payments.data.model.AccessResponse
import com.thellex.payments.data.model.ApiResponse
import kotlinx.serialization.Serializable
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

data class BackendErrorRequestDto(
    @SerializedName("screen") val screen: String,
    @SerializedName("errorType") val errorType: String,
    @SerializedName("message") val message: String,
    @SerializedName("code") val code: String? = null,
    @SerializedName("timestamp") val timestamp: Long = System.currentTimeMillis()
)

interface ErrorService {
    @POST(Constants.Endpoints.ERROR_REPORT)
    suspend fun reportError(@Body request: BackendErrorRequestDto): Response<ApiResponse<AccessResponse>>
}