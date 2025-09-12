package com.thellex.payments.network.services

import com.thellex.payments.core.utils.Constants
import com.thellex.payments.data.model.ApiResponse
import com.thellex.payments.data.model.AppVersionDto
import retrofit2.http.GET
import retrofit2.http.Query

interface AppService {
    @GET(Constants.Endpoints.CHECK_APP_VERSION)
    suspend fun checkAppVersion(
        @Query("platform") platform: String = "android",
        @Query("currentVersion") currentVersion: String
    ): ApiResponse<AppVersionDto>
}

