package com.thellex.pay.network.services

import com.thellex.pay.core.utils.Constants
import com.thellex.pay.data.model.ApiResponse
import com.thellex.pay.data.model.PaginatedNotificationResponse
import com.thellex.pay.data.model.PaginatedRampTransactionsHistoryResponse
import com.thellex.pay.data.model.PaginatedTransactionsHistoryResponse
import retrofit2.http.GET

interface UserService {
    @GET(Constants.Endpoints.USER_RAMP_TRANSACTIONS)
    suspend fun fetchRampTransactions(): ApiResponse<PaginatedRampTransactionsHistoryResponse>

    @GET(Constants.Endpoints.USER_TRANSACTIONS)
    suspend fun fetchTransactionHistory():ApiResponse<PaginatedTransactionsHistoryResponse>

    @GET(Constants.Endpoints.USER_NOTIFICATIONS)
    suspend fun fetchNotifications(): ApiResponse<PaginatedNotificationResponse>
}

