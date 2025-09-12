package com.thellex.payments.network.services

import com.thellex.payments.core.utils.Constants
import com.thellex.payments.data.model.ApiResponse
import com.thellex.payments.data.model.AdminRampTransactionsResponse
import com.thellex.payments.data.model.ApproveRampRequest
import com.thellex.payments.features.admin.model.RevenueResponseDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PUT
import retrofit2.http.Query

interface AdminService {
    @GET(Constants.Endpoints.ADMIN_RAMP_TRANSACTIONS)
    suspend fun fetchAllRampTransactions(
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 10
    ): ApiResponse<AdminRampTransactionsResponse>

    @GET(Constants.Endpoints.ADMIN_REVENUES)
    suspend fun fetchRevenues(): ApiResponse<RevenueResponseDto>

    @PUT(Constants.Endpoints.ADMIN_APPROVE_RAMP)
    suspend fun  approveTransaction(@Body requestDto: ApproveRampRequest): ApiResponse<Unit>
}

