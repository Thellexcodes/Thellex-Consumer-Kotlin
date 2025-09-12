package com.thellex.payments.network.services

import com.thellex.payments.core.utils.Constants
import com.thellex.payments.data.model.ApiResponse
import com.thellex.payments.data.model.INotificationConsumeDto
import retrofit2.Response
import retrofit2.http.PATCH
import retrofit2.http.Path

interface NotificationService {
    @PATCH("api/v1.0.1/notifications/{id}/consume")
    suspend fun consume(@Path("id") id: String): Response<ApiResponse<INotificationConsumeDto>>
}
