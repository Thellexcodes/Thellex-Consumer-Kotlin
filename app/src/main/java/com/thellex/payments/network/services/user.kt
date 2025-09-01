package com.thellex.payments.network.services

import com.thellex.payments.core.utils.Constants
import com.thellex.payments.data.model.ApiResponse
import com.thellex.payments.data.model.ITransactionHistoryDto
import com.thellex.payments.data.model.NotificationEntity
import com.thellex.payments.data.model.AdminRampTransactionsResponse
import retrofit2.http.GET

interface UserService {
    @GET(Constants.GET_ALL_USER_RAMP_TRANSACTIONS)
    suspend fun fetchRampTransactions(): ApiResponse<AdminRampTransactionsResponse>

    @GET(Constants.GET_ALL_USER_TRANSACTION_HISTORY)
    suspend fun fetchTransactionHistory(): ApiResponse<List<ITransactionHistoryDto>>

    @GET(Constants.GET_ALL_USER_NOTIFICATIONS)
    suspend fun fetchNotifications(): ApiResponse<List<NotificationEntity>>
}

