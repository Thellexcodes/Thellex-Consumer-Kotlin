package com.thellex.payments.network.services

import com.thellex.payments.core.utils.Constants
import com.thellex.payments.data.model.ApiResponse
import com.thellex.payments.data.model.ITransactionHistoryDto
import com.thellex.payments.data.model.NotificationEntity
import com.thellex.payments.data.model.AdminRampTransactionsResponse
import com.thellex.payments.data.model.PaginatedNotificationResponse
import com.thellex.payments.data.model.PaginatedRampTransactionsHistoryResponse
import com.thellex.payments.data.model.PaginatedTransactionsHistoryResponse
import retrofit2.http.GET

interface UserService {
    @GET(Constants.GET_ALL_USER_RAMP_TRANSACTIONS)
    suspend fun fetchRampTransactions(): ApiResponse<PaginatedRampTransactionsHistoryResponse>

    @GET(Constants.GET_ALL_USER_TRANSACTION_HISTORY)
    suspend fun fetchTransactionHistory():ApiResponse<PaginatedTransactionsHistoryResponse>

    @GET(Constants.GET_ALL_USER_NOTIFICATIONS)
    suspend fun fetchNotifications(): ApiResponse<PaginatedNotificationResponse>
}

