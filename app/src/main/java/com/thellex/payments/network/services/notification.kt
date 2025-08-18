package com.thellex.payments.network.services

import com.thellex.payments.core.utils.Constants
import com.thellex.payments.data.model.ApiResponse
import com.thellex.payments.data.model.INotificationConsumeDto
import retrofit2.Response
import retrofit2.http.PATCH
import retrofit2.http.Path

interface NotificationService {
    @PATCH(Constants.NOTIFICATION_CONSUME_ENDPOINT)
    suspend fun consume(@Path("id") id: String): Response<ApiResponse<INotificationConsumeDto>>
}
