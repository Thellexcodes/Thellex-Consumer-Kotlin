package com.thellex.payments.network.services

import com.thellex.payments.core.utils.Constants
import com.thellex.payments.data.model.CrashReportDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface CrashReportService {
    @POST(Constants.CRASH_REPORT_ENDPOINT)
    suspend fun sendCrashReport(@Body request: CrashReportDto): Response<Unit>
}