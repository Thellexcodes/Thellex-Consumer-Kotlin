package com.thellex.payments.network.services

import com.thellex.payments.core.utils.Constants
import com.thellex.payments.data.model.ApiResponse
import com.thellex.payments.data.model.AdminRampTransactionsResponse
import com.thellex.payments.features.admin.model.RevenueResponseDto
import retrofit2.http.GET
import retrofit2.http.Query

interface AdminService {
    @GET(Constants.ADMIN_GET_ALL_RAMP_TRANSACTIONS_ENDPOINT)
    suspend fun fetchAllRampTransactions(
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 10
    ): ApiResponse<AdminRampTransactionsResponse>

    @GET(Constants.ADMIN_GET_ALL_REVENUES_ENDPOINT)
    suspend fun fetchRevenues(): ApiResponse<RevenueResponseDto>
}

