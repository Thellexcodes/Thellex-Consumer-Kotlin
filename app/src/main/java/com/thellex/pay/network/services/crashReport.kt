package com.thellex.pay.network.services

import com.thellex.pay.core.utils.Constants
import com.thellex.pay.data.model.CrashReportDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface CrashReportService {
    @POST(Constants.Endpoints.CRASH_REPORT)
    suspend fun sendCrashReport(@Body request: CrashReportDto): Response<Unit>
}