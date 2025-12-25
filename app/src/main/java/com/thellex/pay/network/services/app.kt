package com.thellex.pay.network.services

import com.thellex.pay.core.utils.Constants
import com.thellex.pay.data.model.ApiResponse
import com.thellex.pay.data.model.AppVersionResponseDto
import com.thellex.pay.data.model.BaseSettingsCache
import com.thellex.pay.data.model.BaseSettingsResponse
import com.thellex.pay.data.model.DepositTokenDto
import retrofit2.http.GET
import retrofit2.http.Query

interface AppService {
    @GET(Constants.Endpoints.CHECK_APP_VERSION)
    suspend fun checkAppVersion(
        @Query("platform") platform: String = "android",
        @Query("currentVersion") currentVersion: String
    ): ApiResponse<AppVersionResponseDto>

    @GET(Constants.Endpoints.GET_BASE_APP_SETTINGS)
    suspend fun getBaseAppSettings(): ApiResponse<BaseSettingsResponse>
}


